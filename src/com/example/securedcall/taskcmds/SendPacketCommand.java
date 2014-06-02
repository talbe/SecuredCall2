package com.example.securedcall.taskcmds;

import com.example.securedcall.gateway.ToGatewayPacket;

public class SendPacketCommand extends TaskCommand {
	public ToGatewayPacket Packet;
	
	public SendPacketCommand(ToGatewayPacket cPacket) {
		super(ECommands.SendPacket);
		Packet = cPacket;
	}

}
