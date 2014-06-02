package com.example.securedcall.statemachine;

import java.util.concurrent.Semaphore;

import com.example.securedcall.EThreads;
import com.example.securedcall.Globals;
import com.example.securedcall.general.IObserver;
import com.example.securedcall.taskcmds.SendBluetoothCmd;
import com.example.securedcall.taskcmds.TaskCommand;
import com.example.securedcall.taskcmds.SendBluetoothCmd.EBTCommands;
import com.example.securedcall.taskcmds.TaskCommand.ECommands;
import com.example.securedcall.tevet.TevetStatusDTO;
import com.example.securedcall.tevet.TevetStatusDTO.TevetStatusOpcode;

public class TevetRingState extends SecCallState implements IObserver {

	public static final String LOG_TAG = "TevetRingState";
	
	private Semaphore m_semWaitForAnswer;
	
	public TevetRingState() {
		m_semWaitForAnswer = new Semaphore(0);
	}
	
	@Override
	protected StateResult handleState(SecCallContext cCtx) {
		cCtx.logString("Got to TevetRingState!");
		
		sendTaskCmd(new SendBluetoothCmd(EBTCommands.RING),
										 EThreads.TevetBluetoothClient);
		
		StateResult eResult = StateResult.Working;
		
		try {
			m_semWaitForAnswer.acquire();
			
			// Finally, onward to call state!
			cCtx.setState(new ActiveCallState());
			
			// Let the tevet connect
			sendTaskCmd(new SendBluetoothCmd(EBTCommands.CONNECT), EThreads.TevetBluetoothClient);
			sendTaskCmd(new TaskCommand(ECommands.EnterDataMode), EThreads.TevetBluetoothClient);
		} catch (InterruptedException e) {
			Globals.DbgLog(LOG_TAG, "Failed to acquire the ATA semaphore");
			
			eResult = StateResult.Error;
		}
		
		return eResult;
	}

	@Override
	public void observeCommand(Object cCmd) {
		TevetStatusDTO cDTO = (TevetStatusDTO)cCmd;
		
		if (TevetStatusOpcode.GotATA == cDTO.Opcode) {
			m_semWaitForAnswer.release();
		} else {
			super.observeCommand(cCmd);
		}
	}

}
