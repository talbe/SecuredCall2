package com.example.securedcall.taskcmds;

public class SendBufferCmd extends TaskCommand {
	public byte Buffer[];
	
	public SendBufferCmd(byte arData[]) {
		super(TaskCommand.ECommands.SendBuffer);
		Buffer = arData;
	}
}
