package com.example.securedcall.statemachine;

import android.util.Log;

import com.example.securedcall.EThreads;
import com.example.securedcall.Globals;
import com.example.securedcall.general.IObserver;
import com.example.securedcall.general.MessageQueueManagerException;
import com.example.securedcall.taskcmds.TaskCommand;
import com.example.securedcall.tevet.TevetStatusDTO;

public abstract class SecCallState implements IState, IObserver {
	/**
	 * A tag used for log cat
	 */
	private static final String LOG_TAG = "SecCallState";
	
	/**
	 * A helper variable used to access the globals singleton
	 */
	protected Globals m_cGlobals;
	
	/**
	 * The wrapping context.
	 * Note that you can use the context only between the start
	 * and the end of the handleState method
	 */
	protected SecCallContext m_cCtx;
	
	/**
	 * Ctor, initialize data members
	 */
	public SecCallState() {
		m_cGlobals = Globals.getInstance();
	}
	
	@Override
	public StateResult handleState(IContext cCtx) {
		// Save the context
		m_cCtx = (SecCallContext)cCtx;
		
		m_cCtx.observeTevetServer(this);
		
		// Let the active state do his thing
		StateResult eResult = handleState(m_cCtx);
		
		m_cCtx.unregisterTeverServerObserver(this);
		
		// Clear the context member
		m_cCtx = null;
		
		// Return the result to our caller
		return eResult;
	}
	
	protected abstract StateResult handleState(SecCallContext cCtx);
	
	/**
	 * Send a task command to a given target.
	 * 
	 * @param cCmd		The command to send
	 * @param eTarget	The target to which the command will be sent
	 */
	protected void sendTaskCmd(TaskCommand cCmd, EThreads eTarget) {
		 try {
			 m_cGlobals.getMQ().sendMessage(eTarget, cCmd);
		 } catch (MessageQueueManagerException e) {
			 Log.d(LOG_TAG, "Failed to send a message to thread " + eTarget);
		 }
	}
	
	@Override
	public void observeCommand(Object cCmd) {
		// Make sure that we are dealing with tevet status objects
		if (!(cCmd instanceof TevetStatusDTO)) {
			Globals.DbgLog(LOG_TAG, "Got a non tevet object when observing..");
			
			return;
		}
		
		// Convert the command to a usable format
		TevetStatusDTO cTevetCmd = (TevetStatusDTO)cCmd;
		
		SecCallDTO cSecCallDto = null;
		
		// Handle the opcode that we just got
		switch (cTevetCmd.Opcode) {
			case GotATX:
			{
				m_cCtx.logString("Got ATX, ready for calls!");
				cSecCallDto = new SecCallDTO(SecCallDTO.EOpcode.WaitingForCalls);
				
				break;
			}
			
			default:
			{
				
			}
		}
		
		// Check if we got something to tell the world
		if (null != cSecCallDto) {
			m_cCtx.informObservers(cCmd);
		}
	}
}
