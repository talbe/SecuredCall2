package com.example.securedcall;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Hashtable;

import com.example.securedcall.gateway.GatewayPacketSender;
import com.example.securedcall.general.MessageQueueManager;
import com.example.securedcall.taskcmds.ITaskCommandHandler;
import com.example.securedcall.taskcmds.SendPacketCommand;
import com.example.securedcall.taskcmds.TaskCommand;
import com.example.securedcall.taskcmds.TaskCommand.ECommands;

public abstract class BaseSenderThread extends MsgQueuedThread {
	/**
	 * A general interface to handle sender events.
	 */
	public interface OnSenderEventHandler {
		public void handleEvent(Object cData);
	}
	
	private int m_nPort;
	private String m_strIp;
	private OnSenderEventHandler m_cConnectionEventHandler;
	private OnSenderEventHandler m_cSentPacketHandler;
	private int m_nErrorMsg;
	private DatagramSocket m_cSocket;
	private GatewayPacketSender m_cSender;
	
	public BaseSenderThread(MessageQueueManager<EThreads, TaskCommand> cQueueMgr,
							   String strIp,
							   int nPort) {
		super(cQueueMgr);
		m_nPort = nPort;
		m_strIp = strIp;
		m_cConnectionEventHandler = null;
		m_cSentPacketHandler = null;
		
		setPriority(MAX_PRIORITY);
	}
	
	public void setConnectionEventHandler(OnSenderEventHandler cHandler) {
		m_cConnectionEventHandler = cHandler;
	}
	
	public void setSentPacketHandler(OnSenderEventHandler cHandler) {
		m_cSentPacketHandler = cHandler;
	}
	
	@Override
	protected boolean initialize() {
		m_nErrorMsg = -1;
		m_cSocket = null;
		m_cSender = null;
		
		try {
			// Create a socket to communicate with the gateway 
			m_cSocket = new DatagramSocket();
			m_cSocket.connect(new InetSocketAddress(Inet4Address.getByName(m_strIp), m_nPort));
			
			// Connect the socket to our sender
			m_cSender = new GatewayPacketSender(m_cSocket);
		} catch (UnknownHostException e) {
			m_nErrorMsg = R.string.gateway_host_unknown;
		} catch (IOException e) {
			m_nErrorMsg = R.string.gateway_socket_creation_failed;
		} 
		
		// Check if we have a connection handler to inform
		if (null != m_cConnectionEventHandler) {
			// Inform the handler about our current status
			m_cConnectionEventHandler.handleEvent(m_nErrorMsg);
		}
		
		return (-1 == m_nErrorMsg);
	}
	
	@Override
	protected void cleanUp() {
		// Check if we need to close the sender
		if (null != m_cSender) {
			m_cSender.close();
		}
		// Check if we need to close the socket
		else if (null != m_cSocket) {
			m_cSocket.close();
		}
	}

	@Override
	protected Hashtable<ECommands, ITaskCommandHandler> getCmdsHandlersTable() {
		Hashtable<ECommands, ITaskCommandHandler> tblCmds = 
				new Hashtable<ECommands, ITaskCommandHandler>();
		
		tblCmds.put(TaskCommand.ECommands.SendPacket, m_cSendPacketHandler);
		
		return tblCmds;
	}
	
	private ITaskCommandHandler m_cSendPacketHandler = new ITaskCommandHandler() {
		
		@Override
		public void handleCommand(TaskCommand cCmd) {
			try {
				// Send the packet
				m_cSender.sendPacket(((SendPacketCommand)cCmd).Packet);
				
				// Check if we need to inform a handler that we just sent a packet
				if (null != m_cSentPacketHandler) {
					m_cSentPacketHandler.handleEvent(((SendPacketCommand)cCmd).Packet);
				}
			} catch (Exception e) {
				handleException(e);
			}
		}
	};
}
