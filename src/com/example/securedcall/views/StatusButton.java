package com.example.securedcall.views;

import com.example.securedcall.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class StatusButton extends LinearLayout {

	private boolean m_fIsOn;
	private ImageButton m_btnStatusButton;
	
	public StatusButton (Context context, AttributeSet attrs) {
		super(context, attrs);
		
		// TODO: read the attributes
		m_fIsOn = true;
		
		setOrientation(LinearLayout.HORIZONTAL);
	    setGravity(Gravity.CENTER_VERTICAL);
		
		LayoutInflater inflater = 
				(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.view_status_button, this, true);
		
		// Get the status button
		m_btnStatusButton = (ImageButton)findViewById(R.id.btnStatusImage);
		m_btnStatusButton.setOnClickListener(m_cStatusButtonClick);
	}
	
	public StatusButton (Context context) {
		this(context, null);
	}
	
	private OnClickListener m_cStatusButtonClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (m_fIsOn) {
				m_btnStatusButton.setImageResource(R.drawable.ic_status_button_off);
			} else {
				m_btnStatusButton.setImageResource(R.drawable.ic_status_button_on);
			}
			
			m_fIsOn = !m_fIsOn;

		}
	};
	
}
