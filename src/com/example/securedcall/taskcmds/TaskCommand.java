package com.example.securedcall.taskcmds;

public class TaskCommand {
	public enum ECommands
	{
		StopTask,
		SendPacket,
		RecievePacket,
		SendBuffer,
		WaitForBtClients,
		SendTevetIpCmd,
		SendBluetoothCmd,
		EnterDataMode
	}
	
	/**
	 * The actual command
	 */
	public ECommands Cmd;
	
	/**
	 * Ctor, initialize data members
	 * 
	 * @param eCmd The command opcode
	 */
	public TaskCommand(ECommands eCmd)
	{
		Cmd = eCmd;
	}
}

