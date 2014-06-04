package com.example.securedcall.tevet.atcmds;

import com.example.securedcall.Globals;
import com.example.securedcall.tevet.ATConnection;

public class ATHangCmd implements IATCommandHandler {
	/**
	 * Regex pattern used to identify set register commands
	 */
	public static final String CMD_PATTERN = "^ATH$";
	
	public boolean canHandleCmd(String strCmd)
	{
		return strCmd.matches(CMD_PATTERN);
	}
	
	
	// TODO: Check if reach here, uncomment the code!!
	public Object handleCmd(String strCmd, ATConnection cConnection)
	{
		Globals.DbgLog("ATHangCmd", "Got hang command");
		/*
		try {
			Globals.MQ.sendMessage(Globals.ETasks.BluetoothClient,
					   new SendBluetoothCmd(SendBluetoothCmd.EBTCommands.OK));
			Globals.MQ.sendMessage(Globals.ETasks.IpGateway,
					   new SendTevetIpCmd(new SimpleIPCmd(TevetIPCmd.EOpcodes.EndSession)));
		} catch (MessageQueueManagerException e) {
			Globals.DbgLog("ATCommand", "Failed to send the start session command. " + e.getMessage());
		}
		*/
		
		return null;
	}
}
