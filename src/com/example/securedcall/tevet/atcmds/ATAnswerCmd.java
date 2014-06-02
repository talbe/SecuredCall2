package com.example.securedcall.tevet.atcmds;

import com.example.securedcall.Globals;
import com.example.securedcall.tevet.ATConnection;
import com.example.securedcall.tevet.TevetStatusDTO;
import com.example.securedcall.tevet.TevetStatusDTO.TevetStatusOpcode;

public class ATAnswerCmd implements IATCommandHandler
{
	/**
	 * Regex pattern used to identify set register commands
	 */
	public static final String CMD_PATTERN = "^ATA$";
	
	public boolean canHandleCmd(String strCmd)
	{
		return strCmd.matches(CMD_PATTERN);
	}
	
	public Object handleCmd(String strCmd, ATConnection cConnection)
	{
		Globals.DbgLog("ATAnswerCommand", "Got answer command");
		
		return new TevetStatusDTO(TevetStatusOpcode.GotATA, null);
	}

}
