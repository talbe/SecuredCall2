package com.example.securedcall.gateway;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface IGatewayPacket extends Cloneable {
	/**
	 * Get the size of the packet.
	 * Note that this is predefined and not the value from the data size member.
	 * 
	 * @return The size of the packet
	 */
	public int size();
	
	/**
	 * Write the packet to a provided stream in binary format.
	 * 
	 * @param cOutStream	The stream to which the data will be written
	 * @throws IOException	Raised from the stream
	 */
	public void serialize(OutputStream cOutStream) throws IOException;
	
	/**
	 * Read a packet from a given stream
	 * 
	 * @param cInStream	The stream from which the data will be loaded
	 * @throws IOException				Raised from the stream
	 * @throws IllegalArgumentException	If the buffer contains malformed information
	 */
	public void deserialize(InputStream cInStream) throws IOException, IllegalArgumentException;
}
