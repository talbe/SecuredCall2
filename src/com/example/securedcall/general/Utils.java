/**
 * @file Utils.java
 * 
 * This file contains the implementation of the Utils class.
 * 
 * @author Kfir Gollan
 * @since 18/05/2014
 */
package com.example.securedcall.general;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;

/**
 * The Utils class contains various utilities for the application.
 * 
 * @author Kfir Gollan
 * @since 18/05/2014
 */
public class Utils {
	/**
	 * Check if a given string is a valid IPv4 address
	 * 
	 * @param strIp	The string to check
	 * @return true if the string is a valid ip, false otherwise
	 */
	public static boolean isValidIp(String strIp) {
		return strIp.matches("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(.|$)){4}$");
	}
	
	/**
	 * Read a byte buffer from a stream of bytes.
	 * 
	 * @param cInStream		The stream from which we will read the data
	 * @param nBufferSize	The size of the buffer to read
	 * @return The buffer that we just read
	 * @throws IOException	If we failed to read the buffer an exception will be raised
	 */
	public static ByteBuffer readByteBuffer(InputStream cInStream, int nBufferSize) throws IOException {
		// Allocate a buffer to which we will copy the provided data
		ByteBuffer cRawBuffer = ByteBuffer.allocate(nBufferSize);
		cRawBuffer.order(ByteOrder.BIG_ENDIAN);
		
		// Read the data to our buffer
		for (int nCurByte = 0; nCurByte < nBufferSize; nCurByte++) {
			int nData = cInStream.read();
			
			// Check if we reached the end of the stream before we expect it
			if (-1 == nData) {
				throw new IOException("Unable to read the full packet from the stream");
			}
			
			cRawBuffer.put((byte)nData);
		}
		
		// Reset the inner position in the buffer
		cRawBuffer.position(0);
		
		return cRawBuffer;
	}
	
	/**
     * Convert byte array to hex string
     * @param bytes
     * @return
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sbuf = new StringBuilder();
        for(int idx=0; idx < bytes.length; idx++) {
            int intVal = bytes[idx] & 0xff;
            if (intVal < 0x10) sbuf.append("0");
            sbuf.append(Integer.toHexString(intVal).toUpperCase());
        }
        return sbuf.toString();
    }
    
    public static String bytesToHex(Byte[] bytes)
    {
    	byte[] arData = new byte[bytes.length];
    	for (int i = 0; i < bytes.length; i++)
    	{
    		arData[i] = bytes[i];
    	}
    	return bytesToHex(arData);
    }
    
    public static byte[] convertBytesToPrimitive(Byte[] arData) {
    	byte arMyData[] = new byte[arData.length];
		for (int i = 0; i < arData.length; i++) {
			arMyData[i] = arData[i];
		}
		return arMyData;
    }
}
