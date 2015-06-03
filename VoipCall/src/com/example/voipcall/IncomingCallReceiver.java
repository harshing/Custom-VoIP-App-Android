package com.example.voipcall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.sip.SipManager;
import android.util.Log;

public class IncomingCallReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (SipManager.isIncomingCallIntent(intent)) {
			Log.e("hi", "income");
			SipApplication mApplication = (SipApplication) context;
			mApplication.sipAnswerIncomingCall(intent);
		}
	}
}
