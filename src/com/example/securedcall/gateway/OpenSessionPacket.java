package com.example.securedcall.gateway;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.example.securedcall.Globals;
import com.example.securedcall.gateway.FromGatewayPacket.MessageType;

public class OpenSessionPacket extends ToGatewayPacket 
{
	private short m_nDstPhone;
	public final int PACKET_SIZE = 2;
	
	public OpenSessionPacket(short shDstPhone, short shSrcPhoneNumber)
	{
		super(MessageType.AskForSession, shSrcPhoneNumber);
		m_nDstPhone = shDstPhone;
	}
	
	/**
	 * Write the packet to a provided stream in binary format.
	 * 
	 * @param cOutStream	The stream to which the data will be written
	 * @throws IOException	Raised from the stream
	 */
	@Override
	public void serialize(OutputStream cOutStream) throws IOException {
		
		// Allocate a byte buffer to write our data to
		ByteBuffer cRawBuffer = ByteBuffer.allocate(PACKET_SIZE);
		super.serialize(cOutStream);
	
		// Write all the data to our buffer
		cRawBuffer.order(ByteOrder.BIG_ENDIAN);
		cRawBuffer.putShort(m_nDstPhone);
		
		// Write our bytes buffer to the stream
		cOutStream.write(cRawBuffer.array());
		
		
	}
	
	
	
}
