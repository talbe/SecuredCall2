package com.example.securedcall.tevet;

import java.io.IOException;
import java.util.Hashtable;
import java.util.UUID;

import org.apache.http.conn.ClientConnectionManager;

import com.example.securedcall.EThreads;
import com.example.securedcall.Globals;
import com.example.securedcall.MsgQueuedThread;
import com.example.securedcall.SecuredCallApplication;
import com.example.securedcall.general.IObserver;
import com.example.securedcall.general.MessageQueueManager;
import com.example.securedcall.general.MessageQueueManagerException;
import com.example.securedcall.statemachine.SecCallState;
import com.example.securedcall.taskcmds.ITaskCommandHandler;
import com.example.securedcall.taskcmds.TaskCommand;
import com.example.securedcall.taskcmds.TaskCommand.ECommands;
import com.example.securedcall.tevet.TevetStatusDTO.TevetStatusOpcode;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Looper;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
/**
 * This class is a simple thread that handles bluetooth server related logic.
 * Note: This class assumes that the bluetooth is active and enabled.
 * 		 You should verify it before activiting this class
 * 
 * @author Kfir Gollan
 * @since 03/04/2014
 */
public class TevetServerTask extends MsgQueuedThread implements IObserver
{	
	private static final String SERVER_NAME = "BTServer";
    //private static final UUID MY_UUID_SECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	//private static final UUID MY_UUID_SECURE = UUID.fromString("00001103-0000-1000-8000-00805F9B34FB");
	private static final UUID MY_UUID_SECURE = UUID.fromString("00001103-0000-1000-8000-00805F9B34FB");
    private static final String TAG = "TevetServerTask";
    private static final int ACCEPT_TIMEOUT = 10000;
	
	/**
	 * The bluetooth adapter used to control the bluetooth unit
	 */
	private BluetoothAdapter m_cActiveAdapter;
	
	/**
	 * The bluetooth server, used for getting new clients
	 */
	private BluetoothServerSocket m_cServer;
	
	private Exception m_cLastError;
	
	private TevetClientHandler m_cHandler;
	
	/**
	 * Ctor, initialize data member
	 */
	public TevetServerTask(MessageQueueManager<EThreads, TaskCommand> cQueueMgr)
	{
		super(cQueueMgr);
		
		// Get the default bluetooth adapter
		m_cActiveAdapter = null;
		m_cLastError = null;
		m_cHandler = null;
	}
	
	@SuppressLint("NewApi")
	@Override
	protected boolean initialize() {
		m_cServer = null;
		
		m_cActiveAdapter = null;
		
		
		try
		{
			// get BluetoothAdapter
			if(VERSION.SDK_INT <= VERSION_CODES.JELLY_BEAN_MR1) {
				// Due to a bug in android we must invoke the looper prior to getting the 
				// default adapter
				Looper.prepare();
				
				// sdk <= 17 use getDefaultAdapter
				m_cActiveAdapter = BluetoothAdapter.getDefaultAdapter();
			 } else {
				 m_cActiveAdapter = 
			    		 ((BluetoothManager)SecuredCallApplication.getContext().getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
			 }
		} catch (Exception e) {
			Globals.DbgLog(TAG, "Failed to get bluetooth adapter. " + e.getMessage());
		}
		
		try
		{
			// Acquire a server to listen to incoming connections
			m_cServer = 
					m_cActiveAdapter.listenUsingRfcommWithServiceRecord(SERVER_NAME, MY_UUID_SECURE);
					//m_cActiveAdapter.listenUsingInsecureRfcommWithServiceRecord(SERVER_NAME, MY_UUID_SECURE);
		} 
		catch (Exception e)
		{
			Globals.DbgLog(TAG, "Unable to get the BTServer: " + e.getMessage());
			
			// Save the error for later interrogation
			m_cLastError = e;
			
			// Since we were unable to get the server we need to kill the thread
			return false;
		}
		
		return true;
	}
	
	@Override
	protected void cleanUp() {
		// Check if the server is open
		if (null != m_cServer) {
			try {
				m_cServer.close();
			} catch (IOException e) {
				Globals.DbgLog(TAG, "Failed to close the BT server. " + e.getMessage());
			}
			m_cServer = null;
		}
	}
	
	public Exception getLastError()
	{
		return m_cLastError;
	}

	@Override
	protected EThreads getQueueKey() {
		return EThreads.TevetBluetoothServer;
	}

	@Override
	protected Hashtable<ECommands, ITaskCommandHandler> getCmdsHandlersTable() {
		Hashtable<ECommands, ITaskCommandHandler> tblCmds = 
				new Hashtable<ECommands, ITaskCommandHandler>();
		
		tblCmds.put(ECommands.WaitForBtClients, m_cWaitForConnections);
		
		return tblCmds;
	}
	
	private ITaskCommandHandler m_cWaitForConnections = new ITaskCommandHandler() {
		
		@Override
		public void handleCommand(TaskCommand cCmd) {
			BluetoothSocket cClient = null;
			boolean fWaitForAnotherConnection = true;
			
			try {
				// Wait for our socket
				cClient = m_cServer.accept(ACCEPT_TIMEOUT);
				
				fWaitForAnotherConnection = false;
			} catch (IOException e) {
				Globals.DbgLog(TAG, "Failed to accept socket. " + e.getMessage());
			}
			
			// Make sure that we got an active connection
			if (null != cClient) {
				// Create a handler to handle our session
				try {
					m_cHandler = new TevetClientHandler(m_cQueueMgr, cClient);
					m_cHandler.registerObserver(TevetServerTask.this);
					m_cHandler.start();
					Globals.DbgLog(TAG, "BT connection up and running!");
					informObservers(new TevetStatusDTO(TevetStatusOpcode.ConnectedToClient));
				} catch (IOException e) {
					Globals.DbgLog(TAG, "Failed to open connection with tevet. " + e.getMessage());
					m_cHandler = null;
					try {
						cClient.close();
					} catch (IOException e1) {
						Globals.DbgLog(TAG, "Failed to close the connection to the client. " + e1.getMessage());
					}
					
					fWaitForAnotherConnection = true;
				}
				
			}
			
			// Check if we need to wait for another connection
			if (fWaitForAnotherConnection) {
				// Send the task a request to wait for more connections.
				// This is done to allow the regular task loop to operate
				// and wait for a connection again
				try {
					Globals.sendMsg(EThreads.TevetBluetoothServer, new TaskCommand(ECommands.WaitForBtClients));
				} catch (MessageQueueManagerException e1) {
					Globals.DbgLog(TAG, "Failed to send cyclic wait for clients request");
				}
			}
		}
	};

	@Override
	public void observeCommand(Object cCmd) {
		informObservers(cCmd);
	}
}
