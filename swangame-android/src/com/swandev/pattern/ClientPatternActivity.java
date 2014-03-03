package com.swandev.pattern;

import java.util.Map;

import android.content.Intent;
import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.common.collect.ImmutableMap;
import com.swandev.swangame.PatternClientGame;
import com.swandev.swangame.util.ConnectParams;

public class ClientPatternActivity extends AndroidApplication {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
		cfg.useGL20 = true;
		
		Intent intent = getIntent();
		Map<String, String> connectParams = ImmutableMap.of(
/*				ConnectParams.NICKNAME, intent.getStringExtra(ConnectParams.NICKNAME),//
				ConnectParams.SERVER_ADDRESS, intent.getStringExtra(ConnectParams.SERVER_ADDRESS));
*/
				ConnectParams.NICKNAME, "blinky",//
				ConnectParams.SERVER_ADDRESS, "http://192.168.0.102:8080");
				
		initialize(new PatternClientGame(connectParams), cfg);
	}

}
