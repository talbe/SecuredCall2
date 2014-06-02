package com.example.securedcall.statemachine;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Semaphore;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.example.securedcall.Constants;
import com.example.securedcall.GatewayReceiverThread.OnReceiveNewMsgHandler;
import com.example.securedcall.Globals;
import com.example.securedcall.gateway.FromGatewayPacket;
import com.example.securedcall.gateway.FromGatewayPacket.MessageType;
import com.example.securedcall.gateway.IncomingCallPacket;
import com.example.securedcall.general.IObserver;
import com.example.securedcall.taskcmds.SendPacketCommand;
import com.example.securedcall.tevet.TevetStatusDTO;

public class WaitForCallState extends SecCallState implements IObserver, OnReceiveNewMsgHandler {

	public static String LOG_TAG = "WaitForCallState";
	
	private Semaphore m_cWaitForInstructionsSem;
	private IState m_cNextState;
	private SecCallContext m_cCtx;
	
	public WaitForCallState() {
		m_cWaitForInstructionsSem = new Semaphore(0);
		m_cNextState = null;
	}
	
	@Override
	protected StateResult handleState(SecCallContext cCtx) {
		cCtx.logString("Got to WaitForCall state");
		m_cCtx = cCtx;
		cCtx.setReceiveMsgHandler(this);
		
		try {
			m_cWaitForInstructionsSem.acquire();
		} catch (InterruptedException e) {
			Globals.DbgLog(LOG_TAG, "Failed to connect to tevet");
			
			cCtx.logString("Failed to connect to tevet");
			
			return StateResult.Error;
		}
		
		cCtx.setReceiveMsgHandler(null);
		
		// Move to the next state
		cCtx.setState(m_cNextState);
		m_cCtx = null;
		return StateResult.Working;
	}
	
	@Override
	public void observeCommand(Object cCmd) {
		// Convert the command to a usable format
		TevetStatusDTO cTevetCmd = (TevetStatusDTO)cCmd;
		
		switch (cTevetCmd.Opcode) {
			case GotDialCommand:
			{
				m_cNextState = new DialState((Integer)cTevetCmd.Extra);
				m_cWaitForInstructionsSem.release();
				
			}
			
			default:
			{
				super.observeCommand(cCmd);
			}
		}
	}

	@Override
	public void handlePacket(FromGatewayPacket cPacket) {
		// Check if we got an incoming call request from the gateway
		if(MessageType.IncomingCall == cPacket.getType()) {
			int nCaller = ((IncomingCallPacket)cPacket).getCallerPhone();	
			//m_cCtx.setCallerPhone(nCaller);
			m_cNextState = new IncomingCallState();
			m_cWaitForInstructionsSem.release();
		}
	}
}
