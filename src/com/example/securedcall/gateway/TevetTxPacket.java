package com.example.securedcall.gateway;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.example.securedcall.gateway.ToGatewayPacket.MessageType;
import com.example.securedcall.general.Utils;

public class TevetTxPacket extends ToGatewayPacket {

	private final int PACKET_SIZE = 30;
	private final int DATA_SIZE = 27;
	
	private short m_nDestPhone;
	private byte m_nM2M;
	private byte m_arData[];
	
	private static short m_staticSyncNum = 0;	
	
	public TevetTxPacket(short nSrcPhone, short nDestPhone, byte nM2M, byte arData[]) {
		super(MessageType.Tevet, nSrcPhone, m_staticSyncNum);
		
		m_nDestPhone = nDestPhone;
		m_nM2M = nM2M;
		m_arData = arData;
		
		m_staticSyncNum++;
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
		cRawBuffer.putShort(m_nDestPhone);
		cRawBuffer.put(m_nM2M);
		cRawBuffer.put(m_arData);
		
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
		m_nDestPhone = cRawBuffer.getShort();
		m_nM2M = cRawBuffer.get();
		m_arData = new byte[DATA_SIZE];
		cRawBuffer.get(m_arData);
	}

}
