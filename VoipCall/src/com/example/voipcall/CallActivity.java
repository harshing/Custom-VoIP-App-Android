package com.example.voipcall;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CallActivity extends Activity{

	private static final String TAG = "ActCall";
	private Button btnEnd, btnStart, btnSpeakeron;
	private TextView tvStatus;

	private boolean isSpeakeron = false;

	public SipAudioCall call = null;
	public IncomingCallReceiver callReceiver;
	private SipApplication mApplication;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.act_call);
		mApplication = (SipApplication) this.getApplication();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		initViews();

		Intent intent = getIntent();
		if (intent == null) {
			finish();
			return;
		}
		String number = (String) intent.getSerializableExtra("number");
		if (number != null) {
			btnStart.setVisibility(View.GONE);
			number="sip:"+number;
			Log.e(TAG,number);
			startCalling(number);
		} else {
			Log.e(TAG, "incoming call");
			btnEnd.setVisibility(View.GONE);
			PlayRing.play(this);
		}
	}

	SipAudioCall.Listener callListener = new SipAudioCall.Listener() {
		@Override
		public void onCallBusy(SipAudioCall call) {
			updateStatus("Call Busy...");
		}

		@Override
		public void onCallEnded(SipAudioCall call) {
			updateStatus("Call Ended");
			endCalling();
			finish();
		}

		@Override
		public void onCallEstablished(SipAudioCall call) {
			updateStatus("Call Established");
			Log.e(TAG, "Establishing...");
			call.startAudio();
			call.setSpeakerMode(true);
			if (call.isMuted())
				call.toggleMute();
			updateStatus(call);
		}

		@Override
		public void onReadyToCall(SipAudioCall call) {
			updateStatus("onReadyToCall");
		}

		@Override
		public void onRingingBack(SipAudioCall call) {
			updateStatus("onRingingBack...");
		}

		@Override
		public void onCallHeld(SipAudioCall call) {
			updateStatus("onCallHeldthe call is on hold");
		}

		

		
		@Override
		public void onError(SipAudioCall call, int errorCode,
				String errorMessage) {
			// TODO Auto-generated method stub
			super.onError(call, errorCode, errorMessage);
			Log.e(TAG,errorMessage);
		}

		@Override
		public void onCalling(SipAudioCall call) {
			Log.e(TAG, "onCalling");
			updateStatus("Calling");
			try {
				call.answerCall(30);
			} catch (SipException ee) {
				ee.printStackTrace();
			}
		}

		@Override
		public void onRinging(SipAudioCall call, SipProfile caller) {
			updateStatus("Ringing...");
			Log.e(TAG, "Ringing...");
			try {
				call.answerCall(30);
			} catch (SipException e) {
				Log.e(TAG, "Error---" + e.getMessage());
			}
		}
	};

	private void startCalling(String number) {
		if (!mApplication.isRegistered()) {
			Toast.makeText(this, "not registered", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		Log.e("TEST", "making call");
		//number="sip:"+number;
		try {
			call = mApplication.sipStartCalling(number, callListener);
		} catch (SipException e) {
			Log.e(TAG, "startcalling error ----" + e.getMessage());
			endCalling();
		}
	}

	private void endCalling() {
		mApplication.sipEndCalling(call);
	}

	/**
	 * 
	 * 
	 * @param callIntent
	 */
	private void answerIncomingCall(Intent callIntent) {
		if (!SipManager.isVoipSupported(this)) {
			Log.e(TAG, "voip not supported");
			updateStatus("Voip not supported");
			return;
		}
		Log.e(TAG, "answerIncomingCall");
		try {
			SipAudioCall.Listener listener = new SipAudioCall.Listener() {
				@Override
				public void onRinging(SipAudioCall call, SipProfile caller) {
					try {
						call.answerCall(30);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			call = mApplication.getSipManager().takeAudioCall(callIntent, listener);
			Log.e(TAG, "answered");
			call.answerCall(30);
			call.startAudio();
			call.setSpeakerMode(true);
			if (call.isMuted()) {
				call.toggleMute();
			}
			updateStatus(call);
		} catch (SipException e) {
			Log.e(TAG, "answerIncomingCall error " + e.getMessage());
			mApplication.sipEndCalling(call);
			finish();
		}
	}

	public void updateStatus(final String status) {
		this.runOnUiThread(new Runnable() {
			public void run() {
				tvStatus.setText(status);
			}
		});
	}

	public void updateStatus(SipAudioCall call) {
		String useName = call.getPeerProfile().getDisplayName();
		if (useName == null) {
			useName = call.getPeerProfile().getUserName();
		}
		updateStatus(useName + "@" + call.getPeerProfile().getSipDomain());
	}

	private void initViews() {
		tvStatus = (TextView) findViewById(R.id.call_tv_status);

		btnStart = (Button) findViewById(R.id.call_btn_start);
		btnStart.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				btnStart.setVisibility(View.GONE);
				btnEnd.setVisibility(View.VISIBLE);

				PlayRing.stop();
				answerIncomingCall(getIntent());
			}
		});

		btnEnd = (Button) findViewById(R.id.call_btn_end);
		btnEnd.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				endCalling();
				finish();
			}
		});

		btnSpeakeron = (Button) findViewById(R.id.call_btn_speakeron);
		btnSpeakeron.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				try {
					AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
					int currVolume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);

					if (!isSpeakeron) {
						btnSpeakeron.setText("Turn Speaker Off");

						audioManager.setMode(AudioManager.ROUTE_SPEAKER);

						if (!audioManager.isSpeakerphoneOn()) {
							audioManager.setSpeakerphoneOn(true);

							audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
									audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
									AudioManager.STREAM_VOICE_CALL);
						}
					} else {

						btnSpeakeron.setText("Turn Speaker On");

						if (audioManager.isSpeakerphoneOn()) {
							audioManager.setSpeakerphoneOn(false);
							audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, currVolume,
									AudioManager.STREAM_VOICE_CALL);
						}
					}

					isSpeakeron = !isSpeakeron;

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
	}
}
