package com.swandev.pattern.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.swandev.pattern.PatternClientGame;

public class ClientDesktopLauncher {

	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Pattern Client Game";
		cfg.width = 800;
		cfg.height = 600;

		new LwjglApplication(new PatternClientGame(), cfg);
	}

}
