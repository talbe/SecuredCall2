package com.example.securedcall.tevet.atcmds;

import com.example.securedcall.EThreads;
import com.example.securedcall.Globals;
import com.example.securedcall.gateway.ToGatewayPacket;
import com.example.securedcall.gateway.ToGatewayPacket.MessageType;
import com.example.securedcall.general.MessageQueueManagerException;
import com.example.securedcall.taskcmds.SendBluetoothCmd;
import com.example.securedcall.taskcmds.SendPacketCommand;
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
		
		try {
			Globals.getInstance().getMQ().sendMessage(EThreads.TevetBluetoothClient,
					   new SendBluetoothCmd(SendBluetoothCmd.EBTCommands.OK));
			Globals.getInstance().getMQ().sendMessage(EThreads.GatewaySenderThread,
					   new SendPacketCommand(new ToGatewayPacket(MessageType.EndCall, (short)Globals.getInstance().getIntSetting(Globals.PK_PHONE_NUMBER, -1))));
		} catch (MessageQueueManagerException e) {
			Globals.DbgLog("ATCommand", "Failed to send the start session command. " + e.getMessage());
		}
		
		
		return null;
	}
}
