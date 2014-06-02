package com.example.securedcall.statemachine;

import java.util.concurrent.Semaphore;

import android.util.Log;

import com.example.securedcall.Constants;
import com.example.securedcall.EThreads;
import com.example.securedcall.GatewayReceiverThread;
import com.example.securedcall.GatewaySenderThread;
import com.example.securedcall.Globals;
import com.example.securedcall.MobileSenderThread;
import com.example.securedcall.gateway.Session;
import com.example.securedcall.general.IObserver;
import com.example.securedcall.general.ObservedObject;
import com.example.securedcall.taskcmds.TaskCommand;
import com.example.securedcall.tevet.TevetServerTask;

public class SecCallContext extends ObservedObject implements IContext, Runnable {

	public interface OnLogMsgHandler {
		public void logMsg(String strMsg);
	}
	
	/**
	 * The number of threads managed by the context
	 */
	private final int NUM_OF_THREADS = 2;
	
	/**
	 * A tag used for log cat
	 */
	private final String LOG_TAG = "SecCallContext";
	
	/**
	 * The currently active state
	 */
	private IState m_cActiveState;
	
	/**
	 * A thread used to send messages to the gateway
	 */
	private GatewaySenderThread m_cSenderThread;
	
	/**
	 * A thread used to receive messages from the gateway
	 */
	private GatewayReceiverThread m_cReceiverThread;
	
	private TevetServerTask m_cTeverServerThread;
	
	private OnLogMsgHandler m_cMsgLogger;
	
	
	/**
	 * A message to display in error state
	 */
	private String m_strErrorMsg;
	
	
	private Session m_cCallSession;
	
	/**
	 * Ctor, initialize data members
	 * 
	 * @param cInitialState	The initial state to set for the context
	 * @throws ContextException
	 */
	public SecCallContext(IState cInitialState) throws ContextException {
		// Set the initial state
		m_cActiveState = cInitialState;
		
		// Initialize the message logger
		m_cMsgLogger = null;
		
		m_cCallSession = null;
		
		// Get the required gateway info from the global storage
		Globals cGlobals = Globals.getInstance();
		
		// Check if we are currently connected to the gateway
		if (cGlobals.isConnectedToGateway()) {
			throw new ContextException("The gateway threads are already running");
		}
		
		// Get the ip and port from the global storage
		int nPort = cGlobals.getIntSetting(Globals.PK_GATEWAY_PORT, -1);
		String strIp = cGlobals.getStringSetting(Globals.PK_GATEWAY_IP, "");
		
		// Check if the port or the ip are not valid
		if ((-1 == nPort) || ("" == strIp)) {
			throw new ContextException("Required gateway details were not provided");
		}
		
		// Initiate the context threads
		m_cSenderThread = new GatewaySenderThread(cGlobals.getMQ(), strIp, nPort);
		m_cReceiverThread = new GatewayReceiverThread(cGlobals.getMQ(), Constants.INBOUND_PORT);
		m_cTeverServerThread = new TevetServerTask(cGlobals.getMQ());
	}
	
	public void setMsgLogger(OnLogMsgHandler cHandler) {
		m_cMsgLogger = cHandler;
	}
	
	public void logString(String strMsg) {
		m_cMsgLogger.logMsg(strMsg);
	}
	
	public void setErrorMsg(String strErr) {
		m_strErrorMsg = strErr;
	}
	
	public void observeTevetServer(IObserver cObserver) {
		m_cTeverServerThread.registerObserver(cObserver);
	}
	
	public void unregisterTeverServerObserver(IObserver cObserver) {
		m_cTeverServerThread.unregisterObserver(cObserver);
	}
	
	/**
	 * Perform required initialization before we can start executing the context
	 */
	private void initialize() throws InterruptedException, ContextException {
		// Create a semaphore which we will use to wait for our thread to finish initialization
		// We want all the threads to release the semaphore and once all of done will it we want to continue
		// to achieve this behavior we need to have a single permit when they are all done.
		final Semaphore cWaitFotInitSem = new Semaphore(-1 * NUM_OF_THREADS + 1);
		
		// A flag that indicates whether the threads were properly initialized
		final boolean[] fWasInitialized = new boolean[] {true};
		
		// Set an initialization handlers for each thread
		m_cSenderThread.setConnectionEventHandler(new GatewaySenderThread.OnSenderEventHandler() {
			@Override
			public void handleEvent(Object cData) {
				// Make sure that the initialization was successful
				if (-1 != (Integer)cData) {
					fWasInitialized[0] = false;
				}
				
				// The thread initialization is over, release a permit
				cWaitFotInitSem.release();
			}
		});
		
		m_cReceiverThread.setOnInitDoneHandler(new GatewayReceiverThread.OnReceiverEventHandler() {
			@Override
			public void handleEvent(boolean fWasSuccesful) {
				if (!fWasSuccesful) {
					fWasInitialized[0] = false;
				}
				
				// The thread initialization is over, release a permit
				cWaitFotInitSem.release();
			}
		});
		
		// Start the context threads and let them do their own initialization
		m_cSenderThread.start();
		m_cReceiverThread.start();
		m_cTeverServerThread.start();
		
		// Wait for the initialization process
		cWaitFotInitSem.acquire();
		
		// Check if the initialization went on properly
		if (!fWasInitialized[0]) {
			throw new ContextException("Failed to initialized the context");
		} else {
			logString("context initialization completed");
		}
	}
	
	/**
	 * Release all the taken resources that were in use by the context
	 */
	private void cleanUp() {
		Globals cGlobals = Globals.getInstance();
		
		if ((null != m_cSenderThread) &&
			(m_cSenderThread.isAlive())) {
			try {
				cGlobals.getMQ().sendMessage(EThreads.GatewaySenderThread, new TaskCommand(TaskCommand.ECommands.StopTask));
			} catch (Exception e) {
				
			}
		}
		if ((null != m_cReceiverThread) &&
			(m_cReceiverThread.isAlive())) {
			try {
				cGlobals.getMQ().sendMessage(EThreads.GatewayReceiverThread, new TaskCommand(TaskCommand.ECommands.StopTask));
			} catch (Exception e) {
				
			}
		}
	}
	
	/**
	 * Execute the context, this will handle the entire call process.
	 * This is a blocking method and should be invoked on a thread different then the main thread.
	 */
	@Override
	public void run() {
		boolean fCanContinue = false;
		
		// Try to initialize our object
		try {
			initialize();
			fCanContinue = true;
		} catch (InterruptedException e) {
			// TODO: add a way to report the error to the holder of the context
			Log.d(LOG_TAG, "Initialization error: " + e.getMessage());
		}
		catch (ContextException e) {
			// TODO: add a way to report the error to the holder of the context
			Log.d(LOG_TAG, "Initialization error: " + e.getMessage());
		}
		
		// Make sure that we can continue the operation
		if (fCanContinue) {
			StateResult eResult = StateResult.Error;
			
			// Let the states do their magic
			while ((null != m_cActiveState) &&
				   (StateResult.Working == (eResult = m_cActiveState.handleState(this))));
			
			// Check if we stopped due to an error
			if (StateResult.Error == eResult) {
				Globals.DbgLog(LOG_TAG, "Got a fatal error in SecCallContext!");
			}
		}
		
		// Make sure that all the open resources are cleared.
		cleanUp();
	}
	
	@Override
	public void setState(IState cCurState) {
		m_cActiveState = cCurState;
	}
	
	public void setReceiveMsgHandler(GatewayReceiverThread.OnReceiveNewMsgHandler cHandler) {
		m_cReceiverThread.setOnReceiverNewMsgHandler(cHandler);
	}
	
	public Session getCallSession() {
		return m_cCallSession;
	}
	
	public void setCallSession(Session cCallSession) {
		m_cCallSession = cCallSession;
	}
	
	@Override
	public void informObservers(Object cCmd) {
		super.informObservers(cCmd);
	}
}
