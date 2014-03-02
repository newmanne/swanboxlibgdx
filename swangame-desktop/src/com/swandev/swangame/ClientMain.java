package com.swandev.swangame;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.google.common.collect.ImmutableMap;
import com.swandev.swangame.util.ConnectParams;

public class ClientMain {

	public static void main(String[] args) {
		if (args.length != 2) {
			throw new IllegalStateException("expected you to run this differently");
		}
		final ImmutableMap<String, String> params = ImmutableMap.of(ConnectParams.NICKNAME, args[0], ConnectParams.SERVER_ADDRESS, args[1]);
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Pattern Client Game";
		cfg.useGL20 = true;
		cfg.width = 800;
		cfg.height = 600;

		new LwjglApplication(new PatternClientGame(params), cfg);
	}

}
