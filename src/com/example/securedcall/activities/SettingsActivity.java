package com.example.securedcall.activities;

import com.example.securedcall.Constants;
import com.example.securedcall.Globals;
import com.example.securedcall.R;
import com.example.securedcall.general.Utils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}
	
	public void onSaveBtnClick(View view) {
		// Get all the fields that we need
		EditText txtPhoneNumber = (EditText)findViewById(R.id.txtPhoneNumber);
		EditText txtGatewayPort = (EditText)findViewById(R.id.txtGatewayPort);
		EditText txtGatewayIp = (EditText)findViewById(R.id.txtGatewayIp);
		CheckBox chkDevelopersMode = (CheckBox)findViewById(R.id.chkDeveloperOptions);
		CheckBox chkM2M = (CheckBox)findViewById(R.id.chkM2MMode);
		
		// Variables to hold formatted results
		int nPhoneNumber = 0;
		int nPortNumber = 0;
		
		// Helper variables to allow reuse
		String strErrMsg = "";
		EditText txtErrornousField = null;
		
		// Make sure that a phone number was provided
		if (0 == txtPhoneNumber.getText().length()) {
			txtErrornousField = txtPhoneNumber;
			strErrMsg = getString(R.string.empty_phone_number);
		}
		// Make sure that the provided number is a valid phone number
		else {
			try {
				// Get the gateway port as a number
				nPhoneNumber = Integer.parseInt(txtPhoneNumber.getText().toString());
				
				// Make sure that the provided number is a valid port number
				if ((nPhoneNumber < Constants.MIN_PHONE_NUMBER) ||
					(nPhoneNumber > Constants.MAX_PHONE_NUMBER)) {
					txtErrornousField = txtPhoneNumber;
					strErrMsg = getString(R.string.phone_number_is_not_valid);
				}
					
			} catch (NumberFormatException e) {
				txtErrornousField = txtPhoneNumber;
				strErrMsg = getString(R.string.phone_number_is_not_a_number);
			}
		}
		
		// Make sure that we didn't have any errors so far
		if (null == txtErrornousField) {
			// Make sure that the ip number was provided
			if (0 == txtGatewayIp.getText().length()) {
				txtErrornousField = txtGatewayIp;
				strErrMsg = getString(R.string.empty_gateway_ip);
			}
			// Make sure that the provided ip is a valid one
			else if (!Utils.isValidIp(txtGatewayIp.getText().toString())) {
				txtErrornousField = txtGatewayIp;
				strErrMsg = getString(R.string.gateway_ip_invalid);
			}
		}
		
		// Make sure that we didn't have any errors so far
		if (null == txtErrornousField) {
			// Make sure that the port number was provided
			if (0 == txtGatewayPort.getText().length()) {
				txtErrornousField = txtGatewayPort;
				strErrMsg = getString(R.string.empty_gateway_port);
			} else {
				try {
					// Get the gateway port as a number
					nPortNumber = Integer.parseInt(txtGatewayPort.getText().toString());
					
					// Make sure that the provided number is a valid port number
					if ((nPortNumber < Constants.MIN_PORT) ||
						(nPortNumber > Constants.MAX_PORT)) {
						txtErrornousField = txtGatewayPort;
						strErrMsg = getString(R.string.gateway_port_is_not_a_valid_port);
					}
						
				} catch (NumberFormatException e) {
					txtErrornousField = txtGatewayPort;
					strErrMsg = getString(R.string.gateway_port_is_not_a_number);
				}
			}
		}
		
		// Check if we got an error
		if (null != txtErrornousField) {
			// Display the error message
			Toast.makeText(this, strErrMsg, Toast.LENGTH_SHORT).show();
			
			// Focus on the problematic field
			txtErrornousField.requestFocus();
		} else {
			// Save the settings to the global instance
			Globals cGlobals = Globals.getInstance();
			cGlobals.setIntSetting(Globals.PK_PHONE_NUMBER, nPhoneNumber);
			cGlobals.setIntSetting(Globals.PK_GATEWAY_PORT, nPortNumber);
			cGlobals.setStringSetting(Globals.PK_GATEWAY_IP, txtGatewayIp.getText().toString());
			cGlobals.setBooleanSetting(Globals.PK_DEVELOPER_MODE, chkDevelopersMode.isChecked());
			cGlobals.setBooleanSetting(Globals.PK_ENABLE_M2M, chkM2M.isChecked());
			
			// Display a message to the user
			Toast.makeText(this, R.string.the_settings_were_saved, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			// Inflate the root view with the wanted layout
			View rootView = inflater.inflate(R.layout.fragment_settings,
					container, false);
			
			// Get all the fields that we need
			EditText txtPhoneNumber = (EditText)rootView.findViewById(R.id.txtPhoneNumber);
			EditText txtGatewayPort = (EditText)rootView.findViewById(R.id.txtGatewayPort);
			EditText txtGatewayIp = (EditText)rootView.findViewById(R.id.txtGatewayIp);
			CheckBox chkDevelopersMode = (CheckBox)rootView.findViewById(R.id.chkDeveloperOptions);
			CheckBox chkM2M = (CheckBox)rootView.findViewById(R.id.chkM2MMode);
			
			// Populate the fields according to the settings
			int nPortNumber = Globals.getInstance().getIntSetting(Globals.PK_GATEWAY_PORT, -1);
			if (-1 != nPortNumber) {
				txtGatewayPort.setText(Integer.toString(nPortNumber));
			} else {
				txtGatewayPort.setText("");
			}
			int nPhoneNumber = Globals.getInstance().getIntSetting(Globals.PK_PHONE_NUMBER, -1);
			if (-1 != nPhoneNumber) {
				txtPhoneNumber.setText(Integer.toString(nPhoneNumber));
			} else {
				txtPhoneNumber.setText("");
			}
			txtGatewayIp.setText(Globals.getInstance().getStringSetting(Globals.PK_GATEWAY_IP, ""));
			chkDevelopersMode.setChecked(Globals.getInstance().getBooleanSetting(Globals.PK_DEVELOPER_MODE, false));
			chkM2M.setChecked(Globals.getInstance().getBooleanSetting(Globals.PK_ENABLE_M2M, false));
			
			
			return rootView;
		}
	}

}
