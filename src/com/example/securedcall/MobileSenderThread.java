package com.example.securedcall;

import com.example.securedcall.general.MessageQueueManager;
import com.example.securedcall.taskcmds.TaskCommand;

public class MobileSenderThread extends BaseSenderThread{

	public MobileSenderThread(MessageQueueManager<EThreads, TaskCommand> cQueueMgr,
			   String strIp,
			   int nPort) {
		super(cQueueMgr, strIp.substring(1), nPort);
	}
	
	@Override
	protected EThreads getQueueKey() {
		return EThreads.MobileSenderThread;
	}

}
