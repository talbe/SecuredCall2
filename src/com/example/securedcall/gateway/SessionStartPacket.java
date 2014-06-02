package com.example.securedcall.gateway;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.example.securedcall.gateway.FromGatewayPacket.MessageType;
import com.example.securedcall.general.Utils;

public class SessionStartPacket extends FromGatewayPacket {

	public final int PACKET_SIZE = 8;
	
	protected short m_nCallerPhone;
	protected int m_nDestIp;
	protected short m_nDestPort; 
	
	public SessionStartPacket(int nDestIp, short nDestPort) {
		super(MessageType.SessionStart);
		
		m_nDestIp = nDestIp;
		m_nDestPort = nDestPort;
	}
	
	@Override
	public int size() {
		return super.size() + PACKET_SIZE;
	}
	
	public short getCallerPhone() {
		return m_nCallerPhone;
	}
	
	public int getDestIp() {
		return m_nDestIp;
	}
	
	public short getDestPort() {
		return m_nDestPort;
	}
	
	@Override
	public void serialize(OutputStream cOutStream) throws IOException {
		// Allocate a byte buffer to write our data to
		ByteBuffer cRawBuffer = ByteBuffer.allocate(PACKET_SIZE);
		
		// Write all the data to our buffer
		cRawBuffer.order(ByteOrder.BIG_ENDIAN);
		cRawBuffer.putShort((short)m_nCallerPhone);
		cRawBuffer.putInt(m_nDestIp);
		cRawBuffer.putShort((short)m_nDestPort);
		
		// Write our bytes buffer to the stream
		cOutStream.write(cRawBuffer.array());
	}

	@Override
	public void deserialize(InputStream cInStream) throws IOException, IllegalArgumentException {
		super.deserialize(cInStream);
		
		// Read the wanted buffer from the stream
		ByteBuffer cRawBuffer = Utils.readByteBuffer(cInStream, PACKET_SIZE);
		cRawBuffer.order(ByteOrder.BIG_ENDIAN);
		
		// Read all the members from the buffer
		m_nCallerPhone = cRawBuffer.getShort();
		m_nDestIp = cRawBuffer.getInt();
		m_nDestPort = cRawBuffer.getShort();
	}
}
