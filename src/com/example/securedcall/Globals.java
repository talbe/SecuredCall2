/**
 * @file Globals.java
 * 
 * This file contains the implementation of the Globals calls.
 * 
 * @author Kfir Gollan
 * @since 17/05/2014
 */
package com.example.securedcall;

import java.io.IOException;

import com.example.securedcall.general.MessageQueueManager;
import com.example.securedcall.general.MessageQueueManagerException;
import com.example.securedcall.taskcmds.TaskCommand;

import android.content.SharedPreferences;
import android.util.Log;

/**
 * The globals class is used to store data that should be accessed throughout
 * the entire application.
 * This class is a singleton.
 * Note: The implementation is not thread safe, use with caution.
 * 
 * @author Kfir Gollan
 * @since 17/05/2014
 */
public class Globals {
	/**
	 * The key used to access the shared preferences managed by android.
	 */
	private static final String PREF_KEY = "AppSettings"; 
	
	/**
	 * A preference key used to access the phone number
	 */
	public static final String PK_PHONE_NUMBER = "PhoneNumber";
	
	/**
	 * A preference key used to access the gateway IP
	 */
	public static final String PK_GATEWAY_IP = "GatewayIp";
	
	/**
	 * A preference key used to access the gateway port
	 */
	public static final String PK_GATEWAY_PORT = "GatewayPort";
	
	/**
	 * A preference key used to access the developers mode
	 */
	public static final String PK_DEVELOPER_MODE = "DeveloperMode";
	
	/**
	 * A preference key used to indicate whether P2P communication is allowed
	 */
	public static final String PK_ENABLE_M2M = "Peer2Peer";
	
	public static final String LOG_TAG = "SecuredCallGlobal";
	
	/**
	 * A flag that indicates whether we are in debug mode or not
	 */
	public static final boolean DBG = true;
	
	/**
	 * The sole instance of the class.
	 */
	private static Globals s_cInstance = null;
	
	/**
	 * A shared preferences object used to store the settings
	 * of the application.
	 */
	private SharedPreferences m_cSettings;
	
	/**
	 * An editor used to edit the settings saved for the application
	 */
	private SharedPreferences.Editor m_cSettingsEditor;
	
	/**
	 * Message queue manager for simple communication between tasks in the system
	 */
	private MessageQueueManager<EThreads, TaskCommand> m_cMQ;
	
	/**
	 * A boolean that indicates whether one of the activities is currently connected to the
	 * gateway. This is required since both the main activity and the test activity can do this.
	 */
	private boolean m_fIsConnectedToGateway;
	
	/**
	 * Private ctor, since this is a singleton.
	 */
	private Globals() {
		m_cSettings = SecuredCallApplication.getContext().getSharedPreferences(PREF_KEY, 0);
		m_cSettingsEditor = m_cSettings.edit();
		m_fIsConnectedToGateway = false;
		
		// Create and initialize the queues manager
		m_cMQ = new MessageQueueManager<EThreads, TaskCommand>();
		try {
			m_cMQ.registerQueue(EThreads.GatewaySenderThread);
			m_cMQ.registerQueue(EThreads.GatewayReceiverThread);
			m_cMQ.registerQueue(EThreads.TevetBluetoothClient);
			m_cMQ.registerQueue(EThreads.TevetBluetoothServer);
			m_cMQ.registerQueue(EThreads.MobileSenderThread);
		} catch (MessageQueueManagerException e) {
			Log.d(LOG_TAG, "Failed to register queue. " + e.getMessage());
		}
	}
	
	/**
	 * Get the instance of the class.
	 * If the instance wasn't previously created then this call will create it.
	 * 
	 * @return The instance of the class
	 */
	public static Globals getInstance() {
		if (null == s_cInstance) {
			s_cInstance = new Globals();
		}
		
		return s_cInstance;
	}
	
	/**
	 * Get the message queue manager instance.
	 * 
	 * @return The message queue manager instance in use
	 */
	public MessageQueueManager<EThreads, TaskCommand> getMQ() {
		return m_cMQ;
	}
	
	/**
	 * Get a string setting 
	 * 
	 * @param strKey		The key of the setting
	 * @param strDefValue	The default value to get if the string is not set
	 * @return The found value
	 */
	public String getStringSetting(String strKey, String strDefValue) {
		return m_cSettings.getString(strKey, strDefValue);
	}
	
	/**
	 * Save a string setting
	 * 
	 * @param strKey	The key to use for the setting
	 * @param strValue	The value to set
	 */
	public void setStringSetting(String strKey, String strValue) {
		m_cSettingsEditor.putString(strKey, strValue);
		m_cSettingsEditor.commit();
	}
	
	public int getIntSetting(String strKey, int nDefValue) {
		return m_cSettings.getInt(strKey, nDefValue);
	}
	
	public void setIntSetting(String strKey, int nValue) {
		m_cSettingsEditor.putInt(strKey, nValue);
		m_cSettingsEditor.commit();
	}
	
	public boolean getBooleanSetting(String strKey, boolean fDefValue) {
		return m_cSettings.getBoolean(strKey, fDefValue);
	}
	
	public void setBooleanSetting(String strKey, boolean fValue) {
		m_cSettingsEditor.putBoolean(strKey, fValue);
		m_cSettingsEditor.commit();
	}
	
	public boolean isConnectedToGateway() {
		return m_fIsConnectedToGateway;
	}
	
	public void setIsConnectedToGateway(boolean fIsConnected) {
		m_fIsConnectedToGateway = fIsConnected;
	}
	
	/**
	 * Log a message to logcat.
	 * The message will be logged only if we are in debug mode
	 * 
	 * @param strTag	The tag to use for the log
	 * @param strMsg	The actual message to log
	 */
	public static void DbgLog(String strTag, String strMsg)
	{
		if (DBG) 
		{
			Log.d(strTag, strMsg);
		}
	}
	
	public static void sendMsg(EThreads eTarget, TaskCommand cMsg) throws MessageQueueManagerException {
		Globals cInstance = getInstance();
		cInstance.m_cMQ.sendMessage(eTarget, cMsg);
	}
}
