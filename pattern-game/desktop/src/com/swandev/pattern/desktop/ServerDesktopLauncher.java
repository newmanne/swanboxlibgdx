package com.swandev.pattern.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.swandev.pattern.PatternServerGame;

public class ServerDesktopLauncher {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "swangame";
		cfg.width = 480;
		cfg.height = 320;

		new LwjglApplication(new PatternServerGame(), cfg);
	}
}
