/**
 * @file GatewayReceiverThread.java
 * 
 * This file contains the implementation of the GatewayReceiverThread class
 * 
 * @author Kfir Gollan
 * @since 18/05/2014
 */
package com.example.securedcall;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.Hashtable;

import com.example.securedcall.gateway.FromGatewayPacket;
import com.example.securedcall.gateway.GatewayPacketReceiver;
import com.example.securedcall.general.MessageQueueManager;
import com.example.securedcall.general.MessageQueueManagerException;
import com.example.securedcall.taskcmds.ITaskCommandHandler;
import com.example.securedcall.taskcmds.RecievedPacketCommand;
import com.example.securedcall.taskcmds.TaskCommand;
import com.example.securedcall.taskcmds.TaskCommand.ECommands;

/**
 * The gateway receiver thread is used to accept incoming messages from the gateway
 * 
 * @author Kfir Gollan
 * @since 18/05/2014
 */
public class GatewayReceiverThread extends MsgQueuedThread {
	public interface OnReceiverEventHandler {
		void handleEvent(boolean fWasSuccesful);
	}
	
	/**
	 * An interface use to be informed when a new packet is received
	 */
	public interface OnReceiveNewMsgHandler {
		void handlePacket(FromGatewayPacket cPacket);
	}
	
	private int m_nPort;
	private Thread m_thrdRawReceiver;
	private DatagramSocket m_cSocket;
	private GatewayPacketReceiver m_cReceiver;
	private OnReceiveNewMsgHandler m_cNewMsgHandler;
	private OnReceiverEventHandler m_cInitializationDone;
	
	public GatewayReceiverThread(MessageQueueManager<EThreads, TaskCommand> cQueueMgr,
								 int nPort) {
		super(cQueueMgr);
		
		m_nPort = nPort;
		m_thrdRawReceiver = null;
		m_cNewMsgHandler = null;
		m_cInitializationDone = null;
		setPriority(MAX_PRIORITY);
	}
	
	public void setOnReceiverNewMsgHandler(OnReceiveNewMsgHandler cHandler) {
		m_cNewMsgHandler = cHandler;
	}
	
	public void setOnInitDoneHandler(OnReceiverEventHandler cHandler) {
		m_cInitializationDone = cHandler;
	}
	
	@Override
	protected boolean initialize() {
		try {
			// Create a socket to accept incoming messages
			m_cSocket = new DatagramSocket(m_nPort);
			m_cReceiver = new GatewayPacketReceiver(m_cSocket);
		} catch (Exception e) {
			// Check if we need to trigger a handler
			if (null != m_cInitializationDone) {
				m_cInitializationDone.handleEvent(false);
			}
			
			return false;
		}
		
		// Create & start our raw receiver thread
		m_thrdRawReceiver = new Thread(m_cRawReceiveHandler);
		m_thrdRawReceiver.start();
		
		// Check if we need to trigger a handler
		if (null != m_cInitializationDone) {
			m_cInitializationDone.handleEvent(true);
		}
		
		return true;
	}
	
	@Override
	protected void cleanUp() {
		// Check if the raw receiver is alive
		if ((null != m_thrdRawReceiver) &&
			(m_thrdRawReceiver.isAlive())) {
			m_thrdRawReceiver.interrupt();
		} 
		else {
			if (null != m_cReceiver) {
				m_cReceiver.close();
			} else if (null != m_cSocket) {
				m_cSocket.close();
			}
		}
	}
	
	@Override
	protected EThreads getQueueKey() {
		return EThreads.GatewayReceiverThread;
	}

	@Override
	protected Hashtable<ECommands, ITaskCommandHandler> getCmdsHandlersTable() {
		// Create a table to hold all of our handlers
		Hashtable<ECommands, ITaskCommandHandler> tblCmds = 
				new Hashtable<TaskCommand.ECommands, ITaskCommandHandler>();
		
		// Fill the table with the required handlers
		tblCmds.put(ECommands.RecievePacket, m_cRecvTaskCmd);
		
		return tblCmds;
	}
	
	private Runnable m_cRawReceiveHandler = new Runnable() {
		
		@Override
		public void run() {
			FromGatewayPacket cPacket;
			
			while(true) {
				try {
					cPacket = m_cReceiver.receivePacket();
					
					Globals.getInstance().getMQ().sendMessage(EThreads.GatewayReceiverThread,
														      new RecievedPacketCommand(cPacket));
				} 
				catch (MessageQueueManagerException e) 
				{
						break;

				}
				catch (IOException e) 
				{
						break;

				}
			} 
		}
	};
	
	private ITaskCommandHandler m_cRecvTaskCmd = new ITaskCommandHandler() {
		
		@Override
		public void handleCommand(TaskCommand cCmd) {
			// Check if we have any observers
			if (null != m_cNewMsgHandler) {
				m_cNewMsgHandler.handlePacket(((RecievedPacketCommand)cCmd).Packet);
			}
		}
	};
}
