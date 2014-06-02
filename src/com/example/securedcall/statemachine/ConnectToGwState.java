/**
 * @file ConnectToGwState.java
 * 
 * This file contains the implementation of the ConnectToGwState class.
 * 
 * @author Kfir Gollan
 * @since 19/05/2014
 */
package com.example.securedcall.statemachine;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import android.util.Log;

import com.example.securedcall.Constants;
import com.example.securedcall.EThreads;
import com.example.securedcall.GatewayReceiverThread;
import com.example.securedcall.Globals;
import com.example.securedcall.gateway.FromGatewayPacket;
import com.example.securedcall.gateway.RegisterPacket;
import com.example.securedcall.gateway.ToGatewayPacket;
import com.example.securedcall.taskcmds.SendPacketCommand;

/**
 * The connect to gateway state is the initial state of the application.
 * In this state the application is not yet connected to anything.
 * The state contains the operation of trying to connect to the GW.
 * On error we will go to the error state and display a proper message.
 * On success we will go to the tevet connection state to establish that connection
 * 
 * @author Kfir Gollan
 * @since 19/05/2014
 */
public class ConnectToGwState extends SecCallState {
	/**
	 * A tag used for log cat
	 */
	private static final String LOG_TAG = "ConnectToGwState";
	
	/**
	 * The number of attempts to register with the server to be made
	 */
	private static final int REGISTRATION_ATTEMPTS = 50000;
	
	/**
	 * Timeout for a single attempt in mili-seconds
	 */
	private static final int REGISTRATION_ATTEMPT_TIMEOUT = 10000;
	
	@Override
	protected StateResult handleState(SecCallContext cCtx) {
		// A flag which we will use to indicate whether we got an ack or not
		boolean fGotAck = false;
		
		// Create a semaphore which we will use to wait for responses from the gateway
		final Semaphore cWaitForRegistrationAckSem = new Semaphore(0);
		
		// Prepare a registration request
		SendPacketCommand cCmd = 
				new SendPacketCommand(new RegisterPacket((short)m_cGlobals.getIntSetting(Globals.PK_PHONE_NUMBER, -1),
														 (short)Constants.INBOUND_PORT,
														 m_cGlobals.getBooleanSetting(Globals.PK_ENABLE_M2M, false)));//true));
		
		// Set a receive handler to catch the result
		cCtx.setReceiveMsgHandler(new GatewayReceiverThread.OnReceiveNewMsgHandler() {
			
			@Override
			public void handlePacket(FromGatewayPacket cPacket) {
				// Make sure that this is an ack packet
				if (FromGatewayPacket.MessageType.AckRegisterMsg  == cPacket.getType()) {
					cWaitForRegistrationAckSem.release();
				}
			}
		});
		
		// Try getting an ack a few times
		for (int nAttempt = 0; !fGotAck && nAttempt < REGISTRATION_ATTEMPTS; nAttempt++) {
			// Send a request to the gateway
			sendTaskCmd(cCmd, EThreads.GatewaySenderThread);
			
			cCtx.logString("Sent registraion request");
			
			try
			{
				fGotAck = 
						cWaitForRegistrationAckSem.tryAcquire(REGISTRATION_ATTEMPT_TIMEOUT,
															  TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				// Clear the receive handler
				cCtx.setReceiveMsgHandler(null);
				
				// Log the error for debug purposes
				Log.d(LOG_TAG, "Got interrupted while waiting for registration ack.");
				
				// Report the context that we had an error
				return StateResult.Error;
			}
		}
		
		// Clear the receive handler
		cCtx.setReceiveMsgHandler(null);
		
		// Check if we got an ack on the registraion
		if (!fGotAck) {
			return StateResult.Error;
		}
		
		cCtx.logString("Got ack on the registraion request");
		cCtx.setState(new WaitForCallState());
		return StateResult.Working;
	}

}
