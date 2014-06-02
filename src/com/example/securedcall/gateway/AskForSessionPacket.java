package com.example.securedcall.gateway;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.example.securedcall.general.Utils;

public class AskForSessionPacket extends ToGatewayPacket {

	private static final int PACKET_SIZE = 2; 
	
	private short m_nDestPhone;
	
	public AskForSessionPacket(short nSrcPhone, short nSrcPort, short nDestPhone) {
		super(MessageType.AskForSession, nSrcPhone, nSrcPort, (short)0);
		
		m_nDestPhone = nDestPhone;		
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
		// Load the parent data
		super.deserialize(cInStream);
		
		// Read the wanted buffer from the stream
		ByteBuffer cRawBuffer = Utils.readByteBuffer(cInStream, PACKET_SIZE);
		cRawBuffer.order(ByteOrder.BIG_ENDIAN);
		
		// Read all the members from the buffer
		m_nDestPhone = cRawBuffer.getShort();
	}

}
