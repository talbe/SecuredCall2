package com.example.securedcall.taskcmds;

public class SendBluetoothCmd extends TaskCommand {

	public enum EBTCommands
	{
		RING,
		CONNECT,
		OK,
		NO_CARRIER
	}
	
	public EBTCommands BtCmd;
	
	public SendBluetoothCmd(EBTCommands eCmd) {
		super(ECommands.SendBluetoothCmd);
		
		BtCmd = eCmd;
	}

}
