package com.swandev.swangame;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class ClientMain {

	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Pattern Client Game";
		cfg.useGL20 = true;
		cfg.width = 800;
		cfg.height = 600;

		new LwjglApplication(new PatternClientGame(), cfg);
	}

}
