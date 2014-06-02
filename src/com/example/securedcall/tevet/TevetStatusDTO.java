package com.example.securedcall.tevet;


public class TevetStatusDTO {
	public enum TevetStatusOpcode
	{
		ConnectedToClient,
		DisconnectedFromClient,
		GotDialCommand,
		GotATA,
		GotBuffer,
		GotUnsupportedCommand,
		GotATX
	}
	
	public TevetStatusOpcode Opcode;
	public Object Extra;
	
	public TevetStatusDTO(TevetStatusOpcode eOpcode)
	{
		Opcode = eOpcode;
		Extra = null;
	}
	
	public TevetStatusDTO(TevetStatusOpcode eOpcode, Object cExtra)
	{
		Opcode = eOpcode;
		Extra = cExtra;
	}
}

