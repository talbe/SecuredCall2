package com.example.securedcall.statemachine;

import java.util.concurrent.Semaphore;

import com.example.securedcall.EThreads;
import com.example.securedcall.Globals;
import com.example.securedcall.MobileSenderThread;
import com.example.securedcall.GatewayReceiverThread.OnReceiveNewMsgHandler;
import com.example.securedcall.gateway.FromGatewayPacket;
import com.example.securedcall.gateway.Session;
import com.example.securedcall.gateway.TevetRxPacket;
import com.example.securedcall.gateway.FromGatewayPacket.MessageType;
import com.example.securedcall.gateway.TevetTxPacket;
import com.example.securedcall.general.IObserver;
import com.example.securedcall.general.Utils;
import com.example.securedcall.taskcmds.SendBufferCmd;
import com.example.securedcall.taskcmds.SendPacketCommand;
import com.example.securedcall.taskcmds.TaskCommand;
import com.example.securedcall.taskcmds.TaskCommand.ECommands;
import com.example.securedcall.tevet.TevetStatusDTO;

public class ActiveCallState extends SecCallState implements OnReceiveNewMsgHandler, IObserver {

	private static final String LOG_TAG = "ActiveCallState";
	//Active!
	private Semaphore m_semEndCall;
	private MobileSenderThread m_thrdMobileSender;
	
	
	public ActiveCallState() {
		m_semEndCall = new Semaphore(0);
		m_thrdMobileSender = null;
	}
	
	@Override
	protected StateResult handleState(SecCallContext cCtx) {
		cCtx.logString("Got to ActiveCallState!!");
		
		// Get the current call session
		Session cSession = cCtx.getCallSession();
		
		// Check if there is an active call (should always be the case)
		if (null == cSession) {
			Globals.DbgLog(LOG_TAG, "Got to active call state without an active session!");
			
			return StateResult.Error;
		}
		
		Globals.DbgLog(LOG_TAG, "Call details: caller phone - " + cSession.getCallerPhone() +
								" ip - " + cSession.getCallerIp() + 
								" port - " + cSession.getCallerPort());
		
		// Check if the caller is connected to us directly
		if ((!(cSession.getCallerIp().toString().equals("/" + m_cGlobals.getStringSetting(Globals.PK_GATEWAY_IP, "")))) || 
			(cSession.getCallerPort() != (short)m_cGlobals.getIntSetting(Globals.PK_GATEWAY_PORT, -1))) {
			Globals.DbgLog(LOG_TAG, "Got call with M2M device!");
			
			// Create a thread which we will use to send data to the other end
			m_thrdMobileSender = new MobileSenderThread(m_cGlobals.getMQ(),
														cSession.getCallerIp().toString(),
														cSession.getCallerPort());
			m_thrdMobileSender.start();
		}
		
		cCtx.setReceiveMsgHandler(this);
		
		StateResult eResult = StateResult.Working;
		
		try {
			m_semEndCall.acquire();
		} catch (InterruptedException e) {
			Globals.DbgLog(LOG_TAG, "Failed to acquire the end call semaphore");
			
			eResult = StateResult.Error;
		}
		
		cCtx.setReceiveMsgHandler(null);
		
		if (null != m_thrdMobileSender) {
			sendTaskCmd(new TaskCommand(ECommands.StopTask), EThreads.MobileSenderThread);
			try {
				m_thrdMobileSender.join();
			} catch (InterruptedException e) {
				Globals.DbgLog(LOG_TAG, "Failed to wait for the mobile sender thread to finish!");
				m_thrdMobileSender.interrupt();
			}
		}
		
		return eResult;
	}

	@Override
	public void handlePacket(FromGatewayPacket cPacket) {
		// Handle the received packet
		switch (cPacket.getType()) {
			case Tevet:
			{
				Globals.DbgLog(LOG_TAG, "Got buffer from gateway!!");
				
				TevetRxPacket cDataPacket = (TevetRxPacket)cPacket;
				SendBufferCmd cSendCmd = new SendBufferCmd(cDataPacket.getPayload());
				
				sendTaskCmd(cSendCmd, EThreads.TevetBluetoothClient);
				
				break;
			}
			
			default:
			{
				Globals.DbgLog(LOG_TAG, "Got an unexpected message in active call! " + cPacket.getType());
			}
		}
	}

	@Override
	public void observeCommand(Object cCmd) {
		TevetStatusDTO cDTO = (TevetStatusDTO)cCmd;
		
		switch(cDTO.Opcode) {
			case GotBuffer:
			{
				Globals.DbgLog(LOG_TAG, "Sending data buffer to gateway!");
				byte arData[] = Utils.convertBytesToPrimitive((Byte[])cDTO.Extra);
				
				// Get the current call session
				Session cSession = m_cCtx.getCallSession();
				
				TevetTxPacket cPacket = 
						new TevetTxPacket((short)m_cGlobals.getIntSetting(Globals.PK_PHONE_NUMBER, -1),
										  (short)cSession.getCallerPort(),
										  (byte)0,
										  arData);
				SendPacketCommand cSendCmd = new SendPacketCommand(cPacket);
				
				if (null == m_thrdMobileSender) {
					sendTaskCmd(cSendCmd, EThreads.GatewaySenderThread);
				} else {
					sendTaskCmd(cSendCmd, EThreads.MobileSenderThread);
				}
				break;
			}
			
			default:
			{
				super.observeCommand(cCmd);
			}
		}
	}

}
