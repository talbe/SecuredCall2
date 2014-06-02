package com.example.securedcall.statemachine;

import java.util.concurrent.Semaphore;

import com.example.securedcall.EThreads;
import com.example.securedcall.Globals;
import com.example.securedcall.general.IObserver;
import com.example.securedcall.general.MessageQueueManagerException;
import com.example.securedcall.taskcmds.TaskCommand;
import com.example.securedcall.taskcmds.TaskCommand.ECommands;
import com.example.securedcall.tevet.TevetStatusDTO;

public class ConnectToTevetState extends SecCallState implements IObserver {

	public static String LOG_TAG = "Connect to tevet state";
	
	private Semaphore m_cWaitForConnection;
	
	public ConnectToTevetState() {
		m_cWaitForConnection = new Semaphore(0);
	}
	
	@Override
	protected StateResult handleState(SecCallContext cCtx) {
		cCtx.logString("Got to ConnectToTevetState");
		
		try {
			Globals.sendMsg(EThreads.TevetBluetoothServer, new TaskCommand(ECommands.WaitForBtClients));
		} catch (MessageQueueManagerException e1) {
			Globals.DbgLog("ConnectToTevet", "Failed to send cyclic wait for clients request");
			
			return StateResult.Error;
		}
		
		try {
			m_cWaitForConnection.acquire();
		} catch (InterruptedException e) {
			Globals.DbgLog(LOG_TAG, "Failed to connect to tevet");
			
			cCtx.logString("Failed to connect to tevet");
			
			return StateResult.Error;
		}
		
		// Inform the world that we are now connected to tevet
		cCtx.informObservers(new SecCallDTO(SecCallDTO.EOpcode.ConnectedToTevet));
		
		// And now we wait
		cCtx.setState(new ConnectToGwState());
		
		return StateResult.Working;
	}

	@Override
	public void observeCommand(Object cCmd) {
		// Convert the command to a usable format
		TevetStatusDTO cTevetCmd = (TevetStatusDTO)cCmd;
		
		switch (cTevetCmd.Opcode) {
			case ConnectedToClient:
			{
				Globals.DbgLog(LOG_TAG, "Connection to tevet established!");
				
				m_cWaitForConnection.release();
			}
			
			default:
			{
				super.observeCommand(cCmd);
			}
		}
	}
}
