package com.example.securedcall.gateway;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Session {
	
	private short m_nCallerPhone;
	private Inet4Address m_cCallerIp;
	private short m_nCallerPort;
	
	public Session(short nCallerPhone, int nCallerIp, short nCallerPort) throws UnknownHostException 
	{
		m_nCallerPhone = nCallerPhone;
		m_cCallerIp = (Inet4Address)Inet4Address.getByAddress(BigInteger.valueOf(nCallerIp).toByteArray());
		m_nCallerPort = nCallerPort;
	}
	
	public short getCallerPhone() {
		return m_nCallerPhone;
	}
	
	public Inet4Address getCallerIp() {
		return m_cCallerIp;
	}
	
	public short getCallerPort() {
		return m_nCallerPort;
	}
	
	public void setCallerPhone(short nPhone) {
		m_nCallerPhone = nPhone;
	}
	
	public void setCallerIp(Inet4Address cAddr) {
		m_cCallerIp = cAddr;
	}
	
	public void setCallerPort(short nPort) {
		m_nCallerPort = nPort;
	}
	
}
