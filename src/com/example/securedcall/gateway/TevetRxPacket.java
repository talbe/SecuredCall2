package com.example.securedcall.gateway;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.example.securedcall.general.Utils;

public class TevetRxPacket extends FromGatewayPacket {

	private final int PACKET_SIZE = 35;
	private final int DATA_SIZE = 27;
	
	private short m_nCallerPhone;
	private int m_nDestIp;
	private short m_nDestPort;
	private byte m_arData[];
	
	public TevetRxPacket(short nCallerPhone,
						 int nDestIp,
						 byte arData[]) {
		super(MessageType.Tevet);
		
		m_nCallerPhone = nCallerPhone;
		m_nDestIp = nDestIp;
		m_arData = arData;
	}
	
	/**
	 * Get the size of the packet.
	 * Note that this is predefined and not the value from the data size member.
	 * 
	 * @return The size of the packet
	 */
	public int size() {
		return super.size() + PACKET_SIZE; 
	}
	
	public byte[] getPayload() {
		return m_arData;
	}
	
	/**
	 * Write the packet to a provided stream in binary format.
	 * 
	 * @param cOutStream	The stream to which the data will be written
	 * @throws IOException	Raised from the stream
	 */
	public void serialize(OutputStream cOutStream) throws IOException {
		super.serialize(cOutStream);
		
		// Allocate a byte buffer to write our data to
		ByteBuffer cRawBuffer = ByteBuffer.allocate(PACKET_SIZE);
		
		// Write all the data to our buffer
		cRawBuffer.order(ByteOrder.BIG_ENDIAN);
		cRawBuffer.putShort(m_nCallerPhone);
		cRawBuffer.putInt(m_nDestIp);
		cRawBuffer.putShort(m_nDestPort);
		
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
		super.deserialize(cInStream);
		
		// Read the wanted buffer from the stream
		ByteBuffer cRawBuffer = Utils.readByteBuffer(cInStream, PACKET_SIZE);
		cRawBuffer.order(ByteOrder.BIG_ENDIAN);
		
		// Read all the members from the buffer
		m_nCallerPhone = cRawBuffer.getShort();
		m_nDestIp = cRawBuffer.getInt();
		m_nDestPort = cRawBuffer.getShort();
		m_arData = new byte[DATA_SIZE];
		cRawBuffer.get(m_arData);
	}


}
