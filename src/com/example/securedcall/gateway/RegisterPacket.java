package com.example.securedcall.gateway;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.example.securedcall.gateway.ToGatewayPacket.MessageType;
import com.example.securedcall.general.Utils;

public class RegisterPacket extends ToGatewayPacket {
private static final int PACKET_SIZE = 3; 
	
	private short m_nDestPhone;
	private boolean m_fM2MFlag;
	
	public RegisterPacket(short nSrcPhone, short nSrcPort, boolean fM2MFlag) {
		super(MessageType.Register, nSrcPhone, nSrcPort, (short)0);
		
		m_nDestPhone = 0;
		m_fM2MFlag = fM2MFlag;
		
	}
	
	@Override
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
		// Write the parent data
		super.serialize(cOutStream);
		
		// Allocate a byte buffer to write our data to
		ByteBuffer cRawBuffer = ByteBuffer.allocate(PACKET_SIZE);
		
		// Write all the data to our buffer
		cRawBuffer.order(ByteOrder.BIG_ENDIAN);
		cRawBuffer.putShort(m_nDestPhone);
		if (m_fM2MFlag) {
			cRawBuffer.put((byte)1);
		} else {
			cRawBuffer.put((byte)0);
		}
		
		// Write our bytes buffer to the stream
		cOutStream.write(cRawBuffer.array());
	}
	
	/**
	 * Read a packet from a given stream
	 * 
	 * @param cInStream					The stream from which the data will be loaded
	 * @throws IOException				Raised from the stream
	 * @throws IllegalArgumentException	If the buffer contains malformed information
	 */
	public void deserialize(InputStream cInStream) throws IOException, IllegalArgumentException {
		// Load the parent data
		super.deserialize(cInStream);
		
		// Read the wanted buffer from the stream
		ByteBuffer cRawBuffer = Utils.readByteBuffer(cInStream, PACKET_SIZE);
		cRawBuffer.order(ByteOrder.BIG_ENDIAN);
		
		// Read all the members from the buffer
		m_nDestPhone = cRawBuffer.getShort();
		byte bM2M = cRawBuffer.get();
		m_fM2MFlag = 1 == bM2M;
	}
}
