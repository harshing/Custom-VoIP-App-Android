package com.example.voipcall;

import android.content.Context;
import android.media.MediaPlayer;

public class PlayRing {


	private static MediaPlayer player;

	public static void play(Context context) {
		if (player != null)
			player.stop();

		player = MediaPlayer.create(context, R.raw.bg_lol);
		player.setLooping(true);
		player.start();
	}

	public static void stop() {
		if (player != null) {
			player.stop();
		}
	}
}
