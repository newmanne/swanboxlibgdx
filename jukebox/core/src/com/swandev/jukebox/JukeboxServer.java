package com.swandev.jukebox;

import lombok.Getter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.swandev.swanlib.screen.ServerConnectScreen;
import com.swandev.swanlib.socket.SocketIOState;

public class JukeboxServer extends Game {

	@Getter
	private Assets assets;

	@Getter
	private SocketIOState socketIO;

	@Getter
	private ServerConnectScreen serverConnectScreen;

	@Getter
	private JukeboxServerScreen jukeboxScreen;

	@Override
	public void create() {
		assets = new Assets();
		socketIO = new SocketIOState();
		serverConnectScreen = new JukeboxServerConnectScreen(this, getSocketIO());
		jukeboxScreen = new JukeboxServerScreen(getSocketIO(), this);
		setScreen(serverConnectScreen);
	}

	@Override
	public void dispose() {
		super.dispose();
		serverConnectScreen.dispose();
		jukeboxScreen.dispose();
		assets.dispose();
	}

	public static class JukeboxServerConnectScreen extends ServerConnectScreen {

		public JukeboxServerConnectScreen(JukeboxServer game, SocketIOState socketIO) {
			super(game, socketIO);
		}

		@Override
		protected Screen getGameScreen() {
			return ((JukeboxServer) game).getJukeboxScreen();
		}

	}

}
