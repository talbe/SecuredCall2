package com.example.securedcall.tevet.atcmds;

import com.example.securedcall.Constants;
import com.example.securedcall.Globals;
import com.example.securedcall.tevet.ATConnection;
import com.example.securedcall.tevet.TevetStatusDTO;
import com.example.securedcall.tevet.TevetStatusDTO.TevetStatusOpcode;

public class ATSmartModemCmd  implements IATCommandHandler
{
	/**
	 * Regex pattern used to identify set register commands
	 */
	public static final String CMD_PATTERN = "^AT(?:X|X0|X1|X2|X3|X4)$";
	
	public boolean canHandleCmd(String strCmd)
	{
		return strCmd.matches(CMD_PATTERN);
	}
	
	public Object handleCmd(String strCmd, ATConnection cConnection)
	{
		return new TevetStatusDTO(TevetStatusOpcode.GotATX, null);
	}
}
