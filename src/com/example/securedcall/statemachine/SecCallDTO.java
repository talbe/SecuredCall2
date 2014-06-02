package com.example.securedcall.statemachine;

public class SecCallDTO {
	public enum EOpcode {
		WaitingForCalls,
		ConnectedToTevet,
		ConnectedToServer,
		CallInProgress,
		Dailing
	}
	
	public EOpcode Opcode;
	
	public SecCallDTO(EOpcode eOpcode) {
		Opcode = eOpcode;
	}
}
