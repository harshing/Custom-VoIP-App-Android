package com.example.voipcall;

import android.app.Activity;
import android.content.Intent;
import android.net.sip.SipRegistrationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final int RESULT_SETTINGS = 1;

	private View head;
	private SipApplication mApplication;
	private TextView tvStatus;
	private EditText editNumber;
	private Button btnCall;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		mApplication = (SipApplication)this.getApplication();
        setContentView(R.layout.activity_main);
        initViews();

		startRegistration();	
	}


	/*@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mApplication.onTerminate();
	}*/






	private void startRegistration() {
		if (!mApplication.isSipAvailable()) {
			tvStatus.setText("Sip Not Available");
			return;
		}
		SipRegistrationListener registrationListener = new SipRegistrationListener() {
			@Override
			public void onRegistrationFailed(String localProfileUri, int errorCode, String errorMessage) {
				updateRegisterStatus(-1);
			}

			@Override
			public void onRegistrationDone(String localProfileUri, long expiryTime) {
				updateRegisterStatus(1);
			}

			@Override
			public void onRegistering(String localProfileUri) {
				updateRegisterStatus(0);
			}
		};
		mApplication.sipRegister(registrationListener);
	}

	/**
	 * 
	 * 
	 * @param status
	 *  
	 */
	private void updateRegisterStatus(final int status) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (status == 0) {
					mApplication.setRegistered(false);
					tvStatus.setText("Registering...");
				} else if (status == 1) {
					mApplication.setRegistered(true);
					tvStatus.setText("Registered");
				} else {
					mApplication.setRegistered(false);
					tvStatus.setText("Failed to register");
				}
			}
		});
	}

	private void initViews() {
		
		editNumber=(EditText)findViewById(R.id.sip_number);
        btnCall=(Button)findViewById(R.id.call);
		head = findViewById(R.id.main_head);
		tvStatus = (TextView) head.findViewById(R.id.main_register_status);
		
		btnCall.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				try{
					if(editNumber!=null){
						String no=editNumber.getText().toString();
						Intent intent = new Intent(getApplicationContext(),CallActivity.class);
						intent.putExtra("number", no);
						startActivity(intent);
						
					}
					else if(editNumber==null){
						Toast.makeText(getApplicationContext(), "Input Number", Toast.LENGTH_SHORT).show();
					}
				}
				catch(Exception e){
					Log.e("DialerAppActivity", "error: " + e.getMessage(),e);
				}
			}
		});
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent settingsActivity = new Intent(this, SipSettings.class);
			startActivityForResult(settingsActivity, RESULT_SETTINGS);
			break;
		}
		return true;
	}
    
}
