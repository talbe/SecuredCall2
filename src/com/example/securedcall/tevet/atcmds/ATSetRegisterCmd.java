package com.example.securedcall.tevet.atcmds;

import com.example.securedcall.Constants;
import com.example.securedcall.Globals;
import com.example.securedcall.tevet.ATConnection;
import com.example.securedcall.tevet.TevetStatusDTO;
import com.example.securedcall.tevet.TevetStatusDTO.TevetStatusOpcode;


public class ATSetRegisterCmd implements IATCommandHandler
{
	/**
	 * Regex pattern used to identify set register commands
	 */
	public static final String CMD_PATTERN = "^ATS(\\d+)=(\\d+)$";
	
	public boolean canHandleCmd(String strCmd)
	{
		return strCmd.matches(CMD_PATTERN);
	}
	
	public Object handleCmd(String strCmd, ATConnection cConnection)
	{
		Globals.DbgLog(Constants.AT_CMD_TAG, "Get set register cmd!");
	
		//return new TevetStatusDTO(TevetStatusOpcode.GotUnsupportedCommand);
		return null;
	}
}
