/**
 * @file GatewayPacketSender.java
 * 
 * This file contains the implementation of the GatewayPacketSender class.
 * 
 * @author Kfir Gollan
 * @since 18/05/2014
 */
package com.example.securedcall.gateway;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import com.example.securedcall.Constants;
import com.example.securedcall.Globals;
import com.example.securedcall.general.Utils;

/**
 * This class is in charge of sending packets to the gateway.
 * 
 * @author Kfir Gollan
 * @since 18/05/2014
 */
public class GatewayPacketSender {
	/**
	 * The full size of a single packet in bytes.
	 */
	private static final int FULL_PACKET_SIZE = 38; 
	
	/**
	 * A byte to use for padding the packet to full size
	 */
	private static final byte PACKET_EMPTY_BYTE = 0;
	
	/**
	 * A socket which is used to send the outgoing packets
	 */
	private DatagramSocket m_cSocket;
	
	/**
	 * Ctor, initialize data members.
	 * 
	 * @param cSocket	The socket which will be used by the sender to send
	 * 					packets.
	 */
	public GatewayPacketSender(DatagramSocket cSocket) {
		m_cSocket = cSocket;
	}
	
	/**
	 * Terminate the sender.
	 * Note that after this method is called you shouldn't use this instance anymore.
	 */
	public void close() {
		m_cSocket.close();
	}
	
	/**
	 * Send a given packet to the gateway.
	 * 
	 * @param cPacket	The packet to send
	 * @throws IOException If some error will occur
	 */
	public void sendPacket(ToGatewayPacket cPacket) throws IOException {
		// Allocate a buffer to which the packet will be written
		ByteArrayOutputStream cOutStream = new ByteArrayOutputStream(FULL_PACKET_SIZE);
		
		// Write the packet to our allocated bytes buffer
		cPacket.serialize(cOutStream);
		
		// Calculate the number of extra bytes to write
		int nExtraBytes = FULL_PACKET_SIZE - cPacket.size();
		
		// Fill the rest of the packet
		for (int nByteIndex = 0; nByteIndex < nExtraBytes; nByteIndex++) {
			cOutStream.write(PACKET_EMPTY_BYTE);
		}
		
		// Send the data to the gateway
		m_cSocket.send(new DatagramPacket(cOutStream.toByteArray(), FULL_PACKET_SIZE));
		
		Globals.DbgLog(Constants.TRAFFIC_TAG, "GW-SendBuff: " + Utils.bytesToHex(cOutStream.toByteArray()));
	}
}
