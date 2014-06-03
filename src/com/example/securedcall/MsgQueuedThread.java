package com.example.securedcall;

import java.util.Hashtable;

import android.util.Log;

import com.example.securedcall.general.MessageQueueManager;
import com.example.securedcall.general.ObservedThread;
import com.example.securedcall.taskcmds.ITaskCommandHandler;
import com.example.securedcall.taskcmds.TaskCommand;

public abstract class MsgQueuedThread extends ObservedThread {
	
	/**
	 * A queue manager which will be used to read messages for the queue
	 */
	protected MessageQueueManager<EThreads, TaskCommand> m_cQueueMgr;
	
	/**
	 * A flag that indicates when the thread should stop executing.
	 * Note that it is protected to allow sub threads to stop in an unexpected manner.
	 */
	protected boolean m_fShouldStop;
	
	/**
	 * Ctor, initialize data members
	 * 
	 * @param cQueueMgr	The queue manager which will be used to read messages
	 */
	public MsgQueuedThread(MessageQueueManager<EThreads, TaskCommand> cQueueMgr) {
		m_cQueueMgr = cQueueMgr;
	}
	
	/**
	 * Get the key to use when reading from the queue manager
	 * 
	 * @return The key to use
	 */
	protected abstract EThreads getQueueKey();
	
	/**
	 * Get a table of commands handlers which will be used to handle received commands
	 * 
	 * @return The handlers table
	 */
	protected abstract Hashtable<TaskCommand.ECommands, ITaskCommandHandler> getCmdsHandlersTable();
	
	/**
	 * Handle exceptions in the thread.
	 * This is an empty default implementation, override if needed.
	 * 
	 * @param e The exception that was raised
	 */
	protected void handleException(Exception e) {
		Log.d(Globals.LOG_TAG, "Got exception! task: " + getQueueKey() + ", msg: " + e.getMessage());
	}
	
	/**
	 * Handle an unknown command that was received by the task
	 * 
	 * @param cmd The received command
	 */
	protected void handleUnknownCommand(TaskCommand cmd) {
		Log.d(Globals.LOG_TAG, "Got unknown command! task: " + getQueueKey() + ", cmd: " + cmd.Cmd);
	}
	
	/**
	 * Initialize the thread prior to start the run.
	 * @return true if the initialization process was successfull, false otherwise
	 */
	protected boolean initialize() {
		return true;
	}
	
	/**
	 * A method which is invoked prior to the end of the run operation.
	 * It is meant to perform any clean up operation that is required
	 * for the task.
	 */
	protected void cleanUp() {
	}
	
	@Override
	public void run() {
		TaskCommand cCmd = null;
		m_fShouldStop = false;
		EThreads cKey = getQueueKey();
		Hashtable<TaskCommand.ECommands, ITaskCommandHandler> tblCmds = getCmdsHandlersTable();
		
		// Perform required initialization
		m_fShouldStop = !initialize();
		
		while (!m_fShouldStop) {
			try {
				Globals.DbgLog("MsgQuedThread", "MsgQuedThread:: enter while");
				
				// Read the next message from the queue
				cCmd = m_cQueueMgr.readMessage(cKey);
				
				// Check if this is a stop command
				if (TaskCommand.ECommands.StopTask == cCmd.Cmd) {
					m_fShouldStop = true;
				}
				// Check if we have a handler for the provided command
				else if (!tblCmds.containsKey(cCmd.Cmd)) {
					handleUnknownCommand(cCmd);
				} else {
					tblCmds.get(cCmd.Cmd).handleCommand(cCmd);
				}
			} catch (InterruptedException e) {
				m_fShouldStop = true;
			} catch (Exception e) {
				handleException(e);
			}
		}
		
		cleanUp();
	}
}
