package com.example.securedcall.statemachine;

public enum StateResult {
	/**
	 * This state indicates that the state machine is done and the process is over
	 */
	Done,
	
	/**
	 * This state indicates that an error has occurred and should be handled
	 */
	Error,
	
	/**
	 * This state indicates that the machine is still working
	 */
	Working
}
