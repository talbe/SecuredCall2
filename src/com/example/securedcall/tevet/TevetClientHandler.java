package com.example.securedcall.tevet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Hashtable;

import com.example.securedcall.Constants;
import com.example.securedcall.EThreads;
import com.example.securedcall.Globals;
import com.example.securedcall.MsgQueuedThread;
import com.example.securedcall.general.BoundedQueue;
import com.example.securedcall.general.MessageQueueManager;
import com.example.securedcall.general.Utils;
import com.example.securedcall.taskcmds.ITaskCommandHandler;
import com.example.securedcall.taskcmds.SendBluetoothCmd;
import com.example.securedcall.taskcmds.SendBufferCmd;
import com.example.securedcall.taskcmds.TaskCommand;
import com.example.securedcall.taskcmds.TaskCommand.ECommands;
import com.example.securedcall.tevet.TevetStatusDTO.TevetStatusOpcode;
import com.example.securedcall.tevet.atcmds.ATAnswerCmd;
import com.example.securedcall.tevet.atcmds.ATDialCmd;
import com.example.securedcall.tevet.atcmds.ATHangCmd;
import com.example.securedcall.tevet.atcmds.ATSetRegisterCmd;
import com.example.securedcall.tevet.atcmds.ATSmartModemCmd;
import com.example.securedcall.tevet.atcmds.IATCommandHandler;

import android.bluetooth.BluetoothSocket;

public class TevetClientHandler extends MsgQueuedThread {
	
	public static final String TAG = "ClientHandler";
	public static final EThreads TASK_ID = EThreads.TevetBluetoothClient;
	
	public static final int BT_CMD_SIZE = 27;
	public static final byte BT_CMD_SYNC = (byte)0xAA;
	
	private enum EClientState
	{
		Idle,
		WaitForRingAnswer
	}
	
	private BluetoothSocket m_cClient;
	
	private InputStream m_cClientInStream;
	private OutputStream m_cClientOutStream;
	private ArrayList<IATCommandHandler> m_arHandlers;
	private ATConnection m_cConnection;
	private boolean m_fEnterDataMode;
	
	/**
	 * Ctor, initialize data members
	 * 
	 * @param cClient The client socket which will be used to communicate with client
	 */
	public TevetClientHandler(MessageQueueManager<EThreads, TaskCommand> cQueueMgr,
							  BluetoothSocket cClient) throws IOException
	{
		super(cQueueMgr);
		
		m_cClient = cClient;
		
		m_cClientInStream = m_cClient.getInputStream();
		m_cClientOutStream = m_cClient.getOutputStream();
		
		// Create the handlers list
		m_arHandlers = new ArrayList<IATCommandHandler>();
		m_arHandlers.add(new ATSetRegisterCmd());
		m_arHandlers.add(new ATDialCmd());
		m_arHandlers.add(new ATAnswerCmd());
		m_arHandlers.add(new ATSmartModemCmd());
		m_arHandlers.add(new ATHangCmd());
		setPriority(MAX_PRIORITY);
		
	}
	
	@Override
	public boolean initialize() {
		Globals.DbgLog(TAG, "TevetClient handler task started");
		
		// Start the receiver thread
		m_thrdMsgRcv.start();
		
		return true;
	}
	
	@Override
	public void cleanUp() {
		// Stop the receiver thread
		m_thrdMsgRcv.interrupt();
		while (m_thrdMsgRcv.isAlive())
		{
			try {
				m_thrdMsgRcv.join();
			} catch (InterruptedException e) {
				Globals.DbgLog(TAG, "Got interrupted while waiting for the msg rcvr. " + e.getMessage());
			}
		}
		
		try {
			m_cClient.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Globals.DbgLog(TAG, "BluetoothClient handler task terminated");
	}
	
	private void processAtCmd(String strCmd)
	{
		Globals.DbgLog(TAG, "Processing command " + strCmd);
		
		Object cResult = null;
		
		// Activate all the matching handlers
		for (IATCommandHandler cHandler : m_arHandlers)
		{
			// Check if the current handler can handle the command
			if (cHandler.canHandleCmd(strCmd))
			{
				cResult = cHandler.handleCmd(strCmd, m_cConnection);
				
				// Check if we got a result and if we need to inform the observers
				if (null != cResult) {
					informObservers(cResult);
				}
			}
		}
	}
	
	private ITaskCommandHandler m_cSendBTCmdHandler = new ITaskCommandHandler() {
		
		private EClientState m_eState = EClientState.Idle;
		
		@Override
		public void handleCommand(TaskCommand cCmd) {
			SendBluetoothCmd cBtCmd = (SendBluetoothCmd)cCmd;
			Globals.DbgLog(TAG, "Get send bt command. cmd: " + cBtCmd.BtCmd.toString());
			
			boolean fSendCmd = true;
			
			switch (cBtCmd.BtCmd)
			{
				case RING:
				{
					m_eState = EClientState.WaitForRingAnswer;
					
					break;
				}
				
				case NO_CARRIER:
				case OK:
				{
					break;
				}
				
				case CONNECT:
				{
					/*
					Globals.DbgLog(TAG, "Got request to send connect");
					
					TaskCommand cClientCmd = null;
					
					switch (m_eState)
					{
						case WaitForRingAnswer:
						{
							cClientCmd = new SendTevetIpCmd(new SimpleIPCmd(TevetIPCmd.EOpcodes.StartSessionAck));
							
							break;
						}
					}
					
					if (null != cClientCmd)
					{
						try {
							Globals.MQ.sendMessage(Globals.ETasks.IpGateway, cClientCmd);
						} catch (MessageQueueManagerException e) {
							Globals.DbgLog(TAG, "Failed to send command to the gateway. :-( " + e.getMessage());
						}
					}
					*/
					break;
				}
				
				default:
				{
					Globals.DbgLog(TAG, "Got an unexpected BT command. " + cBtCmd.Cmd.toString());
					fSendCmd = false;
					
					break;
				}
			}
			
			if (fSendCmd)
			{
				sendSimpleCmd(cBtCmd);
			}
		}
		
		private void sendSimpleCmd(SendBluetoothCmd cBtCmd)
		{
			// Note: I added the replace to support NO CARRIER
			String strCmd = "\r\n" + cBtCmd.BtCmd.toString().replace('_', ' ') + "\r\n";
			
			try {
				m_cClientOutStream.write(strCmd.getBytes());
				Globals.DbgLog(TAG, "Sent the following BT command: " + strCmd);
			} catch (IOException e) {
				Globals.DbgLog(TAG, "Failed to send BT command." + e.getMessage());
			}
		}
	};
	
	private Thread m_thrdMsgRcv = new Thread(new Runnable() {
		
		@Override
		public void run() {
			StringBuilder cCmdBuilder = new StringBuilder();
			
			Globals.DbgLog(TAG, "BT rcv thread started");
			
			while (!isInterrupted())
			{
				try
				{
					if (m_fEnterDataMode)
					{
						handleDataMode(cCmdBuilder.toString().getBytes());
					}
					
					int nData = m_cClientInStream.read();
					
					if (nData != -1)
					{
						// Add the current char to our AT command
						cCmdBuilder.append((char)nData);
						
						// Check if this is the end of the command
						if (cCmdBuilder.toString().endsWith("\r\n"))
						{
							processAtCmd(cCmdBuilder.substring(0, cCmdBuilder.length() - 2));
							
							cCmdBuilder.setLength(0);
						}
						
					}
				}
				catch (Exception e)
				{
					Globals.DbgLog(TAG, "Exception in TevetClientHandler: " + e.getMessage());
				}
			}
		}
	});
	
	private ITaskCommandHandler m_cEnterDataModeHandler = new ITaskCommandHandler() {
		
		@Override
		public void handleCommand(TaskCommand cCmd) {
			Globals.DbgLog(TAG, "Got enter data mode message!");
			m_fEnterDataMode = true;
		}
	};
	
	private void handleDataMode(byte arData[])
	{
		Globals.DbgLog(TAG, "Entered data mode");
		
		Byte arDataBuff[] = new Byte[BT_CMD_SIZE];
		BoundedQueue<Byte> cPacketBuff = new BoundedQueue<Byte>(BT_CMD_SIZE);
		int nData = 0;
		int nTerminatorCnt = 0;
		int nGivenDataBuffIndex = 0; 
		boolean fGotFullBuffer = false;
		
		for (int i = 0; i < arDataBuff.length; i++) {
			arDataBuff[i] = 0;
		}
		
		while(!isInterrupted())
		{
			fGotFullBuffer = false;
			
			// Read a single data buffer
			while (!fGotFullBuffer)
			{
				// Check if we have more data in the provided data buffer
				if (null != arData &&
					nGivenDataBuffIndex < arData.length)
				{
					nData = arData[nGivenDataBuffIndex];
					nGivenDataBuffIndex++;
				}
				else
				{
					nData = -1;
					
					while (-1 == nData)
					{
						// Read the current data byte
						try {
							nData = m_cClientInStream.read();
						} catch (SocketTimeoutException e) {
						} catch (IOException e) {
							Globals.DbgLog(TAG, "Failed to read data from the input stream! " + e.getMessage());
						}
					}
				}
				
				// Check if this is a terminator char
				if ('+' != (char)nData)
				{
					nTerminatorCnt = 0;
					
					
				}
				// Check if we need to terminate the data mode
				else if ((++nTerminatorCnt) == 3)
				{
					m_fEnterDataMode = false;
					return;
				}
				
				cPacketBuff.enqueue((byte)nData);
				
				// Check if this is a command sync
				if (BT_CMD_SYNC == (byte)nData)
				{
					// Check if we have enough data
					if (cPacketBuff.isFull())
					{
						fGotFullBuffer = true;
					}
					// If we got here then this means that we are out of sync
					else
					{
						// TODO: this little bit throws an exception, something with Null bytes, check this later.
						//arDataBuff = cPacketBuff.toArray(arDataBuff);
						//Globals.DbgLog(Constants.TRAFFIC_TAG, "Dropping buffer due to sync mismatch. buffer: " + Utils.bytesToHex(arDataBuff));
						//Byte[] arBuff = new Byte[cPacketBuff.size()];
						//cPacketBuff.toArray(arBuff);
						//Globals.DbgLog(TAG, "Throwing packet due to sync mismatch! size: " + cPacketBuff.size() + " data: " + Utils.bytesToHex(arBuff));
						//cPacketBuff.clear();
					}
				}
				
			}
//			try {
//				Thread.sleep(500);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			arDataBuff = cPacketBuff.toArray(arDataBuff);
			Globals.DbgLog(Constants.TRAFFIC_TAG, "BT-GotBuff: " + Utils.bytesToHex(arDataBuff));
			
			informObservers(new TevetStatusDTO(TevetStatusOpcode.GotBuffer, arDataBuff));
		}
		
		if (nTerminatorCnt >= 3)
		{
			Globals.DbgLog(TAG, "Exited data mode due to terminator");
			
			// Inform the connected device that we are out of data mode
//			try
//			{
//				Globals.MQ.sendMessage(Globals.ETasks.BluetoothClient,
//									   new SendBluetoothCmd(SendBluetoothCmd.EBTCommands.OK));
//			} catch (MessageQueueManagerException e) {
//				Globals.DbgLog(TAG, "Failed to send end: " + e.getMessage());
//			}
		}
		else
		{
			Globals.DbgLog(TAG, "Exited data mode, terminator cnt: " + nTerminatorCnt);
		}
		
		m_fEnterDataMode = false;
	}
	
	/**
	 * Send command custom handler.
	 */
	private ITaskCommandHandler m_cSendHandler = new ITaskCommandHandler() {
		
		@Override
		public void handleCommand(TaskCommand cCmd) {
			try {
				m_cClientOutStream.write(((SendBufferCmd)cCmd).Buffer);
				Globals.DbgLog(Constants.TRAFFIC_TAG, "BT-SendBuff: " + Utils.bytesToHex(((SendBufferCmd)cCmd).Buffer));
			} catch (IOException e) {
				Globals.DbgLog(TAG, "Failed to send BT buffer. " + e.getMessage());
			}
		}
	};

	@Override
	protected EThreads getQueueKey() {
		return EThreads.TevetBluetoothClient;
	}

	@Override
	protected Hashtable<ECommands, ITaskCommandHandler> getCmdsHandlersTable() {
		Hashtable<TaskCommand.ECommands, ITaskCommandHandler> tblCmdHandlers =
				new Hashtable<TaskCommand.ECommands, ITaskCommandHandler>();
		tblCmdHandlers.put(TaskCommand.ECommands.SendBluetoothCmd, m_cSendBTCmdHandler);
		tblCmdHandlers.put(TaskCommand.ECommands.SendBuffer, m_cSendHandler);
		tblCmdHandlers.put(TaskCommand.ECommands.EnterDataMode, m_cEnterDataModeHandler);
		return tblCmdHandlers;
	}
	
}
