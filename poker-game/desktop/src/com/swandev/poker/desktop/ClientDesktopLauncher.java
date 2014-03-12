package com.swandev.poker.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.swandev.poker.PokerGameClient;

public class ClientDesktopLauncher {
	public static void main(String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "poker-game-client";
		new LwjglApplication(new PokerGameClient(), config);
	}
}
