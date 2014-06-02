package com.example.securedcall.taskcmds;

import com.example.securedcall.gateway.FromGatewayPacket;

public class RecievedPacketCommand extends TaskCommand {

	public FromGatewayPacket Packet;
	
	public RecievedPacketCommand(FromGatewayPacket cPacket) {
		super(ECommands.RecievePacket);
		Packet = cPacket;
	}

}
