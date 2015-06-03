package com.example.voipcall;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SipSettings extends PreferenceActivity {
	// @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}
}
