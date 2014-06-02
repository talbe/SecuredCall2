package com.example.securedcall.tevet.atcmds;

import com.example.securedcall.tevet.ATConnection;


/**
 * An interface used to handle incoming AT commands parsing.
 * 
 * @author Kfir Gollan
 * @since 03/04/2014
 */
public interface IATCommandHandler {
	/**
	 * Check if the current handler can handle the provided command
	 * 
	 * @param strCmd	The command to check
	 * @return true if the handler can handle the command, false otherwise
	 */
	public boolean canHandleCmd(String strCmd);
	
	/**
	 * Handle a given command
	 * 
	 * @param strCmd		The command to handle
	 * @param cConnection 	The connection to be updated
	 * @return An object result for the caller. null if no action is required
	 */
	public Object handleCmd(String strCmd,
						  	ATConnection cConnection);
}
