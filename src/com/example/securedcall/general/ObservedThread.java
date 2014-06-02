package com.example.securedcall.general;

public abstract class ObservedThread extends Thread {

	private ObservedObject m_cObserversMgr;
	
	public ObservedThread()
	{
		m_cObserversMgr = new ObservedObject();
	}
	
	protected void informObservers(Object cCmd)
	{
		m_cObserversMgr.informObservers(cCmd);
	}
	
	public void registerObserver(IObserver cObsever)
	{
		m_cObserversMgr.registerObserver(cObsever);
	}
	
	public void unregisterObserver(IObserver cObsever)
	{
		m_cObserversMgr.unregisterObserver(cObsever);
	}
	
}
