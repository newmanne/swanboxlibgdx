package com.swandev.poker.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.swandev.poker.PokerGameServer;

public class ServerDesktopLauncher {

	public static void main(String[] args) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "poker-game-server";
		config.width = 480;
		config.height = 320;
		new LwjglApplication(new PokerGameServer(), config);
	}
}
