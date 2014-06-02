package com.example.securedcall.tevet.atcmds;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.securedcall.Constants;
import com.example.securedcall.Globals;
import com.example.securedcall.tevet.ATConnection;
import com.example.securedcall.tevet.TevetStatusDTO;
import com.example.securedcall.tevet.TevetStatusDTO.TevetStatusOpcode;

public class ATDialCmd implements IATCommandHandler
{
	/**
	 * Regex pattern used to identify set register commands
	 */
	public static final String CMD_PATTERN = "^ATD(\\d+)$";
	
	public boolean canHandleCmd(String strCmd)
	{
		return strCmd.matches(CMD_PATTERN);
	}
	
	public Object handleCmd(String strCmd, ATConnection cConnection)
	{
		Pattern cPat = Pattern.compile(CMD_PATTERN);
		Matcher m = cPat.matcher(strCmd);
		int nPhoneNumber = -1;
		
		if (!m.find()) {
			Globals.DbgLog(Constants.AT_CMD_TAG, "Parsing of the dial command failed");
		} else {
			Globals.DbgLog(Constants.AT_CMD_TAG, "Got Dial command! number is: " + m.group(1));
			
			nPhoneNumber = Integer.parseInt(m.group(1));
		}
		
		return new TevetStatusDTO(TevetStatusOpcode.GotDialCommand,
								  (Integer)nPhoneNumber);
	}
}
