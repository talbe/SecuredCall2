package com.example.securedcall.statemachine;

import java.net.UnknownHostException;
import java.util.concurrent.Semaphore;

import com.example.securedcall.EThreads;
import com.example.securedcall.GatewayReceiverThread.OnReceiveNewMsgHandler;
import com.example.securedcall.Globals;
import com.example.securedcall.gateway.FromGatewayPacket;
import com.example.securedcall.gateway.Session;
import com.example.securedcall.gateway.SessionStartPacket;
import com.example.securedcall.gateway.ToGatewayPacket;
import com.example.securedcall.gateway.ToGatewayPacket.MessageType;
import com.example.securedcall.taskcmds.SendPacketCommand;

public class IncomingCallState extends SecCallState implements OnReceiveNewMsgHandler {

	public static final String LOG_TAG = "IncomingCallState";
	
	private Semaphore m_semWaitForSessionDetails;
	
	public IncomingCallState() {
		m_semWaitForSessionDetails = new Semaphore(0);
	}
	
	@Override
	protected StateResult handleState(SecCallContext cCtx) {
		cCtx.logString("Got to IncomingCallState");
		
		ToGatewayPacket cPacket = 
				new ToGatewayPacket(MessageType.AcceptCall,
									(short)m_cGlobals.getIntSetting(Globals.PK_PHONE_NUMBER, -1));
		
		sendTaskCmd(new SendPacketCommand(cPacket), EThreads.GatewaySenderThread);
		
		cCtx.setReceiveMsgHandler(this);
		
		StateResult eResult = StateResult.Working;
		
		try 
		{
			m_semWaitForSessionDetails.acquire();
			
			cCtx.setState(new TevetRingState());
		} catch (InterruptedException e) {
			Globals.DbgLog(LOG_TAG, "Failed to acquire the wait for session details semaphore");
			eResult = StateResult.Error;
		}
		
		cCtx.setReceiveMsgHandler(null);
		
		return eResult;
	}

	@Override
	public void handlePacket(FromGatewayPacket cPacket) {
		// Check if we got a response for call request
		if (FromGatewayPacket.MessageType.SessionStart == cPacket.getType()) {
			SessionStartPacket cSessionDetails = (SessionStartPacket)cPacket;
			
			Session cCallSession = null;
			try {
				cCallSession = new Session(cSessionDetails.getCallerPhone(),
										   cSessionDetails.getDestIp(),
										   cSessionDetails.getDestPort());
				
				m_cCtx.setCallSession(cCallSession);
			} catch (UnknownHostException e) {
				Globals.DbgLog(LOG_TAG, "Got an invalid destination IP! " + cSessionDetails.getDestIp());
			}
			
			Globals.DbgLog(LOG_TAG, "Got session details message!");
			m_semWaitForSessionDetails.release();
		}
	}

}
