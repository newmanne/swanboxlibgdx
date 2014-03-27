package com.swandev.jukebox.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.swandev.jukebox.JukeboxServer;

public class JukeboxServerDesktopLauncher {
	public static void main(String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		// config.fullscreen = true;
		new LwjglApplication(new JukeboxServer(), config);
	}
}
