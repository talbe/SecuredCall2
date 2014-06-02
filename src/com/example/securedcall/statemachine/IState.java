package com.example.securedcall.statemachine;

public interface IState {
	public StateResult handleState(IContext cCtx);
}
