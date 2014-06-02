/**
 * @file ToGatewayPacket.java
 * 
 * This file contains the implementation of the GatewayPacket class.
 * 
 * @author Kfir Gollan
 * @since 18/05/2014
 */
package com.example.securedcall.gateway;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.example.securedcall.Constants;
import com.example.securedcall.general.Utils;

/**
 * The ToGatewayPacket class is the base class for all the communication
 * to the gateway.
 * 
 * @author Kfir Gollan
 * @since 18/05/2014
 */
public class ToGatewayPacket implements IGatewayPacket {
	
	/**
	 * An enumeration of all the different packets that can be sent to the gateway.
	 */
	public enum MessageType {
		
		Register,
		KA,
		RemoveMe,
		AskForSession,
		Tevet,
		EndCall,
		AcceptCall,
		DeclineCall;
		
		private static  MessageType[] values = MessageType.values();
		public static MessageType fromByte(byte bType) throws IllegalArgumentException {
			
			if (bType >= values.length) {
				throw new IllegalArgumentException("The provided type is invalid");
			}
			
			return values[bType];
		}
	}
	
	public final int PACKET_SIZE = 8;
	
	/**
	 * The type of the packet
	 */
	protected MessageType m_eType;
	
	/**
	 * The synchronization number of the current message
	 */
	protected short m_nSyncNum;
	
	/**
	 * The number of the source phone
	 */
	protected short m_nSrcPhone;
	
	/**
	 * The port to which the gateway will reply
	 */
	protected short m_nSrcPort;
	
	/**
	 * The data size of the 
	 */
	protected byte m_nDataSize;
	
	/**
	 * Ctor, initialize data members.
	 * 
	 * @param eType		The type of packet to set
	 * @param nSrcPhone	The source phone number to set
	 */
	public ToGatewayPacket(MessageType eType, short nSrcPhone, short nSyncNum) {
		m_eType = eType;
		m_nSyncNum = nSyncNum;
		m_nSrcPhone = nSrcPhone;
		m_nSrcPort = Constants.INBOUND_PORT;
		m_nDataSize = PACKET_SIZE;
	}
	
	/**
	 * Ctor, initialize data members.
	 * 
	 * @param eType		The type of packet to set
	 * @param nSrcPhone	The source phone number to set
	 */
	public ToGatewayPacket(MessageType eType, short nSrcPhone) {
		m_eType = eType;
		m_nSyncNum = (short)0xcafe;
		m_nSrcPhone = nSrcPhone;
		m_nSrcPort = Constants.INBOUND_PORT;
		m_nDataSize = PACKET_SIZE;
	}
	
	/**
	 * Ctor, initialize data members.
	 * 
	 * @param eType		The type of packet to set
	 * @param nSrcPhone	The source phone number to set
	 */
	public ToGatewayPacket(MessageType eType, short nSrcPhone, short nSrcPort, short nSyncNum) {
		m_eType = eType;
		m_nSrcPhone = nSrcPhone;
		m_nSrcPort = nSrcPort;
		m_nSyncNum = 0;
		m_nDataSize = PACKET_SIZE;
	}
	
	/**
	 * Get the size of the packet.
	 * Note that this is predefined and not the value from the data size member.
	 * 
	 * @return The size of the packet
	 */
	public int size() {
		return PACKET_SIZE; 
	}
	
	/**
	 * Write the packet to a provided stream in binary format.
	 * 
	 * @param cOutStream	The stream to which the data will be written
	 * @throws IOException	Raised from the stream
	 */
	public void serialize(OutputStream cOutStream) throws IOException {
		// Allocate a byte buffer to write our data to
		ByteBuffer cRawBuffer = ByteBuffer.allocate(PACKET_SIZE);
		
		// Write all the data to our buffer
		cRawBuffer.order(ByteOrder.BIG_ENDIAN);
		cRawBuffer.put((byte)m_eType.ordinal());
		cRawBuffer.putShort(m_nSyncNum);
		cRawBuffer.putShort(m_nSrcPhone);
		cRawBuffer.putShort(m_nSrcPort);
		cRawBuffer.put(m_nDataSize);
		
		// Write our bytes buffer to the stream
		cOutStream.write(cRawBuffer.array());
	}
	
	/**
	 * Read a packet from a given stream
	 * 
	 * @param cInStream	The stream from which the data will be loaded
	 * @throws IOException				Raised from the stream
	 * @throws IllegalArgumentException	If the buffer contains malformed information
	 */
	public void deserialize(InputStream cInStream) throws IOException, IllegalArgumentException {
		// Read the wanted buffer from the stream
		ByteBuffer cRawBuffer = Utils.readByteBuffer(cInStream, PACKET_SIZE);
		cRawBuffer.order(ByteOrder.BIG_ENDIAN);
		
		// Read all the members from the buffer
		byte nType = cRawBuffer.get();
		m_nSyncNum = cRawBuffer.getShort();
		m_nSrcPhone = cRawBuffer.getShort();
		m_nSrcPort = cRawBuffer.getShort();
		m_nDataSize = cRawBuffer.get();
		
		// Make sure that the type is valid
		try {
			m_eType = MessageType.fromByte(nType);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("The provided buffer contained an invalid message type " + nType);
		}
		
		// Make sure that the data size is valid
		if (size() != m_nDataSize) {
			throw new IllegalArgumentException("The provided data size is invalid! expected: " + size() + " got: " + m_nDataSize);
		}
	}
}
