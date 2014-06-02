package com.example.securedcall.tevet;

import com.example.securedcall.general.TevetException;

/**
 * This class contains all the settings of the connection
 * that can be set using AT commands.
 * 
 * @author Kfir Gollan
 * @since 03/04/2014
 * @see http://en.wikipedia.org/wiki/Hayes_command_set
 */
public class ATConnection
{
	/**
	 * The total number of register codes.
	 */
	public static final int NUMBER_OF_REGISTER_CODES = 39;
	
	/**
	 * An enumeration of all the possible register codes
	 */
	public enum ERegisterCode
	{
		NumberOfRingsBeforeAutoAnswer(0,0,0),
		RingCounter(1,0,255),
		EscapeCharacter(2,0,255),
		CarriageReturnCharacter(3,0,127),
		LineFeedCharacter(4,0,127),
		BackspaceCharacter(5,0,32),
		WaitTimeBeforeBlindDialing(6,2,255),
		WaitForCarrierAfterDial(7,1,255),
		PauseTimeForComma(8,0,255),
		CarrierDetectResponseTime(9,1,255),
		DelayBetweenLossOfCarrierAndHangUp(10,1,255),
		DTMFToneDuration(11,50,255),
		EscapeCodeGuardTime(12,0,255),
		/* 13 - 17 missing */
		TestTimer(18,0,255),
		/* 19 - 24 missing */
		DelayToDTR(25,0,255),
		RTSToCTSDelayInterval(26,0,255),
		/* 27 - 29 missing */
		InactivityDisconnectTimer(30,0,255),
		/* 31 - 36 missing */
		DesiredTelcoLineSpeed(37,0,10),
		DelayBeforeForceDisconnect(38,0,255);
		
		private final int m_nCode;
		private final int m_nMinVal;
		private final int m_nMaxVal;
		
		private ERegisterCode(int nCode,
							  int nMinVal,
							  int nMaxVal)
		{
			m_nCode = nCode;
			m_nMinVal = nMinVal;
			m_nMaxVal = nMaxVal;
		}
	    
		public int getCode()
	    {
	    	return m_nCode;
	    }
		
		public int getMinVal()
		{
			return m_nMinVal;
		}
		
		public int getMaxVal()
		{
			return m_nMaxVal;
		}
	}
	
	/**
	 * An array that holds all the register values of the connection
	 */
	private int m_arSRegisters[];
	
	/**
	 * Ctor, initialize data members
	 */
	public ATConnection()
	{
		m_arSRegisters = new int[NUMBER_OF_REGISTER_CODES];
	}
	
	/**
	 * Set the value of the relevant register 
	 * 
	 * @param eCode 			The register identifier code
	 * @param nValue			The value to set
	 * @throws TevetException	If the provided value is invalid
	 */
	public void setRegister(ERegisterCode eCode, int nValue) throws TevetException
	{
		// Make sure that the provided value is valid
		if ((nValue < eCode.getMinVal()) ||
			(nValue > eCode.getMaxVal()))
		{
			throw new TevetException("Got invalid value for register! Register code: " + eCode.getCode() + " Value: " + nValue);
		}
		
		// Set the value of the wanted register
		m_arSRegisters[eCode.getCode()] = nValue;
	}
	
	/**
	 * Get the value of a given register
	 * 
	 * @param eCode The code of the register to read
	 * @return The value of the register
	 */
	public int getRegister(ERegisterCode eCode)
	{
		return m_arSRegisters[eCode.getCode()];
	}
	
}

