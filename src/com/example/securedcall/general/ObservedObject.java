package com.example.securedcall.general;

import java.util.ArrayList;

public class ObservedObject {
	private ArrayList<IObserver> m_arObservers;
	
	public ObservedObject()
	{
		m_arObservers = new ArrayList<IObserver>();
	}
	
	protected void informObservers(Object cCmd)
	{
		for (IObserver cObserver : m_arObservers)
		{
			cObserver.observeCommand(cCmd);
		}
	}
	
	public void registerObserver(IObserver cObsever)
	{
		m_arObservers.add(cObsever);
	}
	
	public void unregisterObserver(IObserver cObsever)
	{
		m_arObservers.remove(cObsever);
	}
}
