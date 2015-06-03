package com.example.voipcall;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.preference.PreferenceManager;
import android.util.Log;

public class SipApplication extends Application{

	private static final String TAG = "SipApplication";

	private SipManager mSipManager = null;
	private SipProfile mSipProfile = null;
	private IncomingCallReceiver callReceiver;
	private boolean isRegistered = false;

	public boolean isSipAvailable() {
		if (!SipManager.isApiSupported(this)) {
			Log.e(TAG, "this API cannot be surpported");
			return false;
		}
		if (!SipManager.isVoipSupported(this)) {
			Log.e(TAG, "your system don't support SIP-based VOIP API");
			return false;
		}
		Log.e(TAG, "Great ,you can use SIP !!!");
		return true;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		if (mSipManager == null) {
			mSipManager = SipManager.newInstance(this);
		}
		registerCallReceiver();

		//
	}
	
	@Override
	public void onTerminate() {
		closeLocalProfile();
		if (callReceiver != null) {
			this.unregisterReceiver(callReceiver);
		}
		super.onTerminate();
	}

	private void registerCallReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.SipDemo.INCOMING_CALL");
		callReceiver = new IncomingCallReceiver();
		this.registerReceiver(callReceiver, filter);
		Log.e(TAG, "callReceiver");
	}

	/**
	 * Sip
	 * 
	 * @param registrationListener
	 */
	public void sipRegister(SipRegistrationListener registrationListener) {
		if (mSipManager == null) {
			mSipManager = SipManager.newInstance(this);
			if (mSipManager == null) {
				Log.e(TAG, "mSipManager is NULL");
				return;
			}
		}
		closeLocalProfile();
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		String username = prefs.getString("namePref", "");
		String domain = prefs.getString("domainPref", "");
		String password = prefs.getString("passPref", "");
		/*String username = "1004";
		String domain = "10.129.28.241";
		String password = "1234";*/

		try {
			SipProfile.Builder builder = new SipProfile.Builder(username, domain);
			builder.setPassword(password);
			mSipProfile = builder.build();
			Log.e(TAG, "profile " + mSipProfile.getUriString());

			Intent i = new Intent();
			i.setAction("android.SipDemo.INCOMING_CALL");
			PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, Intent.FILL_IN_DATA);
			mSipManager.open(mSipProfile, pi, null);
			//
			//mSipManager.setRegistrationListener(mSipProfile.getUriString(), registrationListener);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sip
	 * 
	 * @param destSip
	 * @param listener
	 * @return
	 * @throws SipException
	 */
	public SipAudioCall sipStartCalling(String dest, SipAudioCall.Listener listener) throws SipException {
		Log.e("dest",dest);
		return mSipManager.makeAudioCall(mSipProfile.getUriString(), dest, listener, 30);
		}

	public void sipAnswerIncomingCall(Intent callIntent) {
		Intent answerIntent = new Intent(this, CallActivity.class);
		answerIntent.putExtras(callIntent);
		answerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
		this.startActivity(answerIntent);
	}

	/**
	 * 
	 * 
	 * @param call
	 * @throws SipException
	 */
	public void sipEndCalling(SipAudioCall call) {

		if (call != null) {
			try {
				call.endCall();
				call.close();
			} catch (SipException e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * 
	 * 
	 * @throws SipException
	 */
	public void closeLocalProfile() {
		try {
			if (mSipProfile != null) {
				mSipManager.close(mSipProfile.getUriString());
			}
		} catch (SipException e) {
			e.printStackTrace();
		}
	}

	public SipManager getSipManager() {
		return mSipManager;
	}

	public SipProfile getSipProfile() {
		return mSipProfile;
	}

	public void setSipManager(SipManager mSipManager) {
		this.mSipManager = mSipManager;
	}

	public void setSipProfile(SipProfile mSipProfile) {
		this.mSipProfile = mSipProfile;
	}

	public boolean isRegistered() {
		return isRegistered;
	}

	public void setRegistered(boolean isRegistered) {
		this.isRegistered = isRegistered;
	}
}
