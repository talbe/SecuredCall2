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

public class GatewaySenderThread extends BaseSenderThread {

	public GatewaySenderThread(MessageQueueManager<EThreads, TaskCommand> cQueueMgr,
			   String strIp,
			   int nPort) {
		super(cQueueMgr, strIp, nPort);
	}
	
	@Override
	protected EThreads getQueueKey() {
		return EThreads.GatewaySenderThread;
	}
}
