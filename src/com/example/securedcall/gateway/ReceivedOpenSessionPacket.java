package com.example.securedcall.gateway;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.example.securedcall.gateway.ToGatewayPacket.MessageType;

public class ReceivedOpenSessionPacket extends  FromGatewayPacket{
	private short m_nDstPhone;
	public final int PACKET_SIZE = 2;
	
	public ReceivedOpenSessionPacket(short shDstPhone, short shSrcPhoneNumber)
	{
		super(MessageType.SessionStart);
		m_nDstPhone = shDstPhone;
	}
	
	/**
	 * Write the packet to a provided stream in binary format.
	 * 
	 * @param cOutStream	The stream to which the data will be written
	 * @throws IOException	Raised from the stream
	 */
	//@Override
	//public void deserialize(InputStream cOutStream) throws IOException {		
		
		
	//}
	
	
	
}
