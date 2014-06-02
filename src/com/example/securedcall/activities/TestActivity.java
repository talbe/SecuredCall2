package com.example.securedcall.activities;

import com.example.securedcall.Constants;
import com.example.securedcall.EThreads;
import com.example.securedcall.GatewayReceiverThread;
import com.example.securedcall.GatewaySenderThread;
import com.example.securedcall.Globals;
import com.example.securedcall.R;
import com.example.securedcall.gateway.AskForSessionPacket;
import com.example.securedcall.gateway.FromGatewayPacket;
import com.example.securedcall.gateway.ToGatewayPacket;
import com.example.securedcall.gateway.ToGatewayPacket.MessageType;
import com.example.securedcall.general.MessageQueueManagerException;
import com.example.securedcall.taskcmds.SendPacketCommand;
import com.example.securedcall.taskcmds.TaskCommand;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class TestActivity extends FragmentActivity {
	
	private int m_nPhone;
	private ProgressDialog m_cProgressDialog;
	private GatewaySenderThread m_cSenderThread;
	private GatewayReceiverThread m_cReceiverThread;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		
		m_cSenderThread = null;
		m_cProgressDialog = null;
		m_cReceiverThread = null;
		int nErrorMsg = -1;
		
		// Get the required gateway info from the global storage
		Globals cGlobals = Globals.getInstance();
		
		// Check if we are currently connected to the gateway
		if (cGlobals.isConnectedToGateway()) {
			Toast.makeText(this, R.string.already_connected_to_gateway, Toast.LENGTH_LONG).show();
			
			finish();
		}
		
		int nPort = cGlobals.getIntSetting(Globals.PK_GATEWAY_PORT, -1);
		String strIp = cGlobals.getStringSetting(Globals.PK_GATEWAY_IP, "");
		m_nPhone = cGlobals.getIntSetting(Globals.PK_PHONE_NUMBER, 0);
		
		// Check if the port or the ip are not valid
		if ((-1 == nPort) || ("" == strIp)) {
			nErrorMsg = R.string.gateway_details_missing;
		}
		else {
			m_cProgressDialog = 
					ProgressDialog.show(this,
										"",
										getString(R.string.initialization_in_progress),
										true);
			
			// Initialize the receiver thread
			m_cReceiverThread = new GatewayReceiverThread(cGlobals.getMQ(), Constants.INBOUND_PORT);
			m_cReceiverThread.setOnReceiverNewMsgHandler(new GatewayReceiverThread.OnReceiveNewMsgHandler() {
				@Override
				public void handlePacket(FromGatewayPacket cPacket) {
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							Toast.makeText(TestActivity.this,
									"Why is there a camel in my selfie?!",
									Toast.LENGTH_LONG).show();
						}
					});
				}
			});
			m_cReceiverThread.start();
			
			// Create a sender thread to connect to the gateway
			m_cSenderThread = new GatewaySenderThread(cGlobals.getMQ(), strIp, nPort);
			
			// Set a handler to catch the end of the connection phase
			m_cSenderThread.setConnectionEventHandler(new GatewaySenderThread.OnSenderEventHandler() {
				@Override
				public void handleEvent(Object cData) {
					Globals.getInstance().setIsConnectedToGateway(true);
					
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							m_cProgressDialog.cancel();
						}
					});
				}
			});
			
			// Start the sender thread!
			m_cSenderThread.start();
		}
		
		// Check if we had any errors.
		if (-1 != nErrorMsg) {
			// Let the user know that he wont be able to use this activity
			Toast.makeText(this, nErrorMsg, Toast.LENGTH_SHORT).show();
			
			// Close the activity
			finish();
		} else if (savedInstanceState == null) {
			// Create the arguments for the fragment
			Bundle cArgs = new Bundle();
			
			// Populate the arguments for the fragment
			cArgs.putString(PlaceholderFragment.IP_KEY, strIp);
			cArgs.putInt(PlaceholderFragment.PORT_KEY, nPort);
			
			// Create a fragment and set its arguments
			PlaceholderFragment cFrag = new PlaceholderFragment();
			cFrag.setArguments(cArgs);
			
			// Load the fragment to the manager
			getSupportFragmentManager().beginTransaction().add(R.id.container, cFrag).commit();
		}
	}

	@Override
	protected void onDestroy() {
		Globals cGlobals = Globals.getInstance();
		try {
			if ((null != m_cSenderThread) &&
				(m_cSenderThread.isAlive())) {
				cGlobals.getMQ().sendMessage(EThreads.GatewaySenderThread, new TaskCommand(TaskCommand.ECommands.StopTask));
			}
			if ((null != m_cReceiverThread) &&
				(m_cReceiverThread.isAlive())) {
				cGlobals.getMQ().sendMessage(EThreads.GatewayReceiverThread, new TaskCommand(TaskCommand.ECommands.StopTask));
			}
		} catch (Exception e) {
		}
		
		cGlobals.setIsConnectedToGateway(false);
		
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.test, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void sendPacket(ToGatewayPacket cPacket) {
		// Create a command to wrap our packet
		SendPacketCommand cCmd = new SendPacketCommand(cPacket);
		
		// Set a send packet handler to catch the result from the thread
		m_cSenderThread.setSentPacketHandler(new GatewaySenderThread.OnSenderEventHandler() {
			
			@Override
			public void handleEvent(Object cData) {
				// Unregister this handler
				m_cSenderThread.setSentPacketHandler(null);
				
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						m_cProgressDialog.cancel();
					}
				});
			}
		});
		
		try {
			// Send our packet to the sender thread
			Globals.getInstance().getMQ().sendMessage(EThreads.GatewaySenderThread, cCmd);
			
			// Display the progress dialog to the user
			m_cProgressDialog = 
					ProgressDialog.show(this, "", getString(R.string.operation_in_progress), true);
		} catch (MessageQueueManagerException e) {
			// Inform the user that we failed to send the message
			Toast.makeText(this, R.string.failed_to_send_mq_message, Toast.LENGTH_SHORT).show();
			
			// Unregister the handler since our packet wont be sent
			m_cSenderThread.setSentPacketHandler(null);
		}
	}
	
	public void onBtnTestKeepAliveClick(View view) {
		// Send the keep alive packet
		sendPacket(new ToGatewayPacket(MessageType.KA, (short)m_nPhone));
	}
	
	public void onBtnTestRegistrationClick(View view) {
		// Send the register packet
		sendPacket(new ToGatewayPacket(MessageType.Register, (short)m_nPhone));
	}
	
	public void onBtnTestRemoveMeClick(View view) {
		// Send the remove me packet
		sendPacket(new ToGatewayPacket(MessageType.RemoveMe, (short)m_nPhone));
	}
	
	public void onBtnTestAskForSessionClick(View view) {
		// Send the ask for session packet
		sendPacket(new AskForSessionPacket((short)m_nPhone,
										   (short)Constants.INBOUND_PORT, 
										   (short)0x3456));
	}
	
	public void onBtnTestTevetClick(View view) {
		// Send the ask for session packet
		sendPacket(new ToGatewayPacket(MessageType.Tevet, (short)m_nPhone));
	}
	
	public void onBtnTestEndCallClick(View view) {
		// Send the ask for session packet
		sendPacket(new ToGatewayPacket(MessageType.EndCall, (short)m_nPhone));
	}
	
	public void onBtnTestAcceptCallClick(View view) {
		// Send the ask for session packet
		sendPacket(new ToGatewayPacket(MessageType.AcceptCall, (short)m_nPhone));
	}
	
	public void onBtnTestDeclineCallClick(View view) {
		// Send the ask for session packet
		sendPacket(new ToGatewayPacket(MessageType.DeclineCall, (short)m_nPhone));
	}
	
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		public static final String PORT_KEY = "Port";
		public static final String IP_KEY = "IP";
		
		public PlaceholderFragment() {
		}
		

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_test, container,
					false);
			
			// Get the required attributes for the fragment
			Bundle cArgs = getArguments();
			
			// Get the required fields
			TextView txtGatewayIp = (TextView)rootView.findViewById(R.id.lblGatewayIpAddr);
			TextView txtGatewayPort = (TextView)rootView.findViewById(R.id.lblGatewayPortNumber);
			
			// Set the proper values
			txtGatewayIp.setText(cArgs.getString(IP_KEY));
			txtGatewayPort.setText(Integer.toString(cArgs.getInt(PORT_KEY)));
			
			return rootView;
		}
	}

}
