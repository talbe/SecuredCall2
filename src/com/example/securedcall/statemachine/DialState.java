package com.example.securedcall.statemachine;

import java.net.UnknownHostException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import android.util.Log;

import com.example.securedcall.EThreads;
import com.example.securedcall.GatewayReceiverThread;
import com.example.securedcall.GatewayReceiverThread.OnReceiveNewMsgHandler;
import com.example.securedcall.Globals;
import com.example.securedcall.gateway.FromGatewayPacket;
import com.example.securedcall.gateway.IncomingCallPacket;
import com.example.securedcall.gateway.Session;
import com.example.securedcall.gateway.SessionStartPacket;
import com.example.securedcall.gateway.ToGatewayPacket;
import com.example.securedcall.gateway.OpenSessionPacket;
import com.example.securedcall.taskcmds.SendBluetoothCmd;
import com.example.securedcall.taskcmds.SendBluetoothCmd.EBTCommands;
import com.example.securedcall.taskcmds.TaskCommand.ECommands;
import com.example.securedcall.taskcmds.SendPacketCommand;
import com.example.securedcall.taskcmds.TaskCommand;

public class DialState extends SecCallState implements OnReceiveNewMsgHandler {
	/**
	 * A tag used for log cat
	 */
	private static final String LOG_TAG = "DialState";
	
	/**
	 * The number of attempts to dial
	 */
	private static final int DIAL_ATTEMPTS = 3;
	
	/**
	 * Timeout for a single attempt in mili-seconds
	 */
	private static final int DIAL_ATTEMPT_TIMEOUT = 60000;
	private int mDestenationPhone;
	private Semaphore m_cWaitForOpenSessionSem;
	private Semaphore m_cWaitForSessionStartSem;
	
	public DialState(int nDestPhone) {
		m_cWaitForOpenSessionSem = new Semaphore(0);
		m_cWaitForSessionStartSem = new Semaphore(0);
		mDestenationPhone = nDestPhone;
	}
	
	@Override
	protected StateResult handleState(SecCallContext cCtx) {
		cCtx.logString("Enter Dial Mode");
		
		// A flag which we will use to indicate whether the other mobile available
		boolean fGotAck = false;
		boolean fGotSessionStart = false;
		
		// Prepare a dial request
		SendPacketCommand cCmd = 
				new SendPacketCommand(new OpenSessionPacket((short)mDestenationPhone,															
														   (short)m_cGlobals.getIntSetting(Globals.PK_PHONE_NUMBER, -1)));
		
		// Set a receive handler to catch the result
		cCtx.setReceiveMsgHandler(this);
		
		// Send the request and wait for the answer
		for (int nAttempt = 0; !fGotAck && nAttempt < DIAL_ATTEMPTS; nAttempt++) {
			// Send a request to the gateway
			sendTaskCmd(cCmd, EThreads.GatewaySenderThread);
			
			Globals.DbgLog(LOG_TAG, "Sent open session session");
			cCtx.logString("Sending open session request");
			
			try
			{
				fGotAck = 
						m_cWaitForOpenSessionSem.tryAcquire(DIAL_ATTEMPT_TIMEOUT,
															TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				// Clear the receive handler
				cCtx.setReceiveMsgHandler(null);
				
				// Log the error for debug purposes
				Globals.DbgLog(LOG_TAG, "Got interrupted while waiting for open session.");
				
				// Report the context that we had an error
				return StateResult.Error;
			}
		}
		
		if (!fGotAck)
		{
			cCtx.logString("Failed to get ack on open session!");
		} else {
			cCtx.logString("Got ack on open session");
			
			// Waiting for session start details
			for (int nAttempt = 0; !fGotSessionStart && nAttempt < DIAL_ATTEMPTS; nAttempt++) {
				try
				{
					fGotSessionStart = 
							m_cWaitForSessionStartSem.tryAcquire(DIAL_ATTEMPT_TIMEOUT,
																  TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					// Clear the receive handler
					cCtx.setReceiveMsgHandler(null);
					
					// Log the error for debug purposes
					Globals.DbgLog(LOG_TAG, "Got interrupted while waiting for session start");
					
					// Report the context that we had an error
					return StateResult.Error;
				}
			}
		}
				
		// Clear the receive handler
		cCtx.setReceiveMsgHandler(null);
		
		// Check if we got an ack on the registraion
		if (!fGotAck || !fGotSessionStart) {
			return StateResult.Error;
		}
		
		cCtx.logString("Got ack on the dial request");
		sendTaskCmd(new SendBluetoothCmd(EBTCommands.CONNECT), EThreads.TevetBluetoothClient);
		sendTaskCmd(new TaskCommand(ECommands.EnterDataMode), EThreads.TevetBluetoothClient);
		cCtx.setState(new ActiveCallState());
		
		return StateResult.Working;
	}

	@Override
	public void handlePacket(FromGatewayPacket cPacket) {
		Globals.DbgLog(LOG_TAG, "Got packet : " + cPacket.getType());
		
		switch (cPacket.getType()) {
			case ReceiverAvailable:
			{
				Globals.DbgLog(LOG_TAG, "Got ReceiverAvailable !");
				m_cWaitForOpenSessionSem.release();
				
				break;
			}
			
			case ReceiverDeclineTheSession:
			case ReceiverBusy:
			{
				Globals.DbgLog(LOG_TAG, "Unable to talk with the target! !" + cPacket.getType());
				m_cWaitForOpenSessionSem.release();
				
				break;
			}
			
			case SessionStart:
			{
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
				
				m_cWaitForSessionStartSem.release();
				
				break;
			}
			
			default:
			{
				
			}
		}
	}

}
