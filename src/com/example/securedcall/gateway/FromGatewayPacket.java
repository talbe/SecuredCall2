package com.example.securedcall.gateway;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.example.securedcall.general.Utils;

public class FromGatewayPacket implements IGatewayPacket {
	/**
	 * An enumeration of all the different packets that can be received from the gateway.
	 */
	public enum MessageType {
		SubscriberAlreadyExists,
		ReceiverAvailable,
		ReceiverNotExist,
		ReceiverBusy,
		Tevet,
		IncomingCall,
		SessionStart,
		SessionClosed,
		ReceiverDeclineTheSession,
		RegisterAgain,
		AckRegisterMsg;
		
		private static  MessageType[] values = MessageType.values();
		public static MessageType fromByte(byte bType) throws IllegalArgumentException {
			
			if (bType >= values.length) {
				throw new IllegalArgumentException("The provided type is invalid");
			}
			
			return values[bType];
		}
	}
	
	public final int PACKET_SIZE = 4;
	
	/**
	 * The type of the message
	 */
	protected MessageType m_eType;
	
	protected short m_nSyncNum;
	
	/**
	 * The size of the data section (not including the type and size fields)
	 */
	protected byte m_nDataSize;
	
	/**
	 * Ctor, initialize data members
	 */
	public FromGatewayPacket(MessageType eType) {
		m_eType = eType;
		m_nDataSize = 0;
		m_nSyncNum = 0;
	}

	@Override
	public int size() {
		return PACKET_SIZE;
	}

	public MessageType getType() {
		return m_eType;
	}
	
	@Override
	public void serialize(OutputStream cOutStream) throws IOException {
		// Allocate a byte buffer to write our data to
		ByteBuffer cRawBuffer = ByteBuffer.allocate(PACKET_SIZE);
		
		// Write all the data to our buffer
		cRawBuffer.order(ByteOrder.BIG_ENDIAN);
		cRawBuffer.put((byte)m_eType.ordinal());
		cRawBuffer.putShort(m_nSyncNum);
		cRawBuffer.put(m_nDataSize);
		
		// Write our bytes buffer to the stream
		cOutStream.write(cRawBuffer.array());
		
	}

	@Override
	public void deserialize(InputStream cInStream) throws IOException,
			IllegalArgumentException {
		// Read the wanted buffer from the stream
		ByteBuffer cRawBuffer = Utils.readByteBuffer(cInStream, PACKET_SIZE);
		cRawBuffer.order(ByteOrder.BIG_ENDIAN);
		
		// Read all the members from the buffer
		byte nType = cRawBuffer.get();
		m_nSyncNum = cRawBuffer.getShort();
		m_nDataSize = cRawBuffer.get();
		
		// Make sure that the type is valid
		try {
			m_eType = MessageType.fromByte(nType);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("The provided buffer contained an invalid message type " + nType);
		}
		
		// Make sure that the data size is valid
		//if (size() != m_nDataSize) {
		//	throw new IllegalArgumentException("The provided data size is invalid! expected: " + size() + " got: " + m_nDataSize);
		//}
	}
	
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
