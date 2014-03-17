package com.swandev.jukebox;

import lombok.Getter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.swandev.swanlib.screen.ServerConnectScreen;
import com.swandev.swanlib.socket.SocketIOState;


public class JukeboxServer extends Game {
	
	@Getter
	private SpriteBatch spriteBatch;

	@Getter
	private ShapeRenderer shapeRenderer;

	@Getter
	private Assets assets;

	@Getter
	private SocketIOState socketIO;

	@Getter
	private ServerConnectScreen serverConnectScreen;

	@Getter
	private JukeboxScreen jukeboxScreen;

	@Override
	public void create() {
		spriteBatch = new SpriteBatch();
		assets = new Assets();
		socketIO = new SocketIOState();
		serverConnectScreen = new JukeboxServerConnectScreen(this, getSocketIO());
		jukeboxScreen = new JukeboxScreen(getSocketIO());
		setScreen(serverConnectScreen);
		// setScreen(pokerServerScreen);
	}

	@Override
	public void dispose() {
		super.dispose();
		serverConnectScreen.dispose();
		jukeboxScreen.dispose();
		spriteBatch.dispose();
		assets.dispose();
	}

	public static class JukeboxServerConnectScreen extends ServerConnectScreen {

		public JukeboxServerConnectScreen(JukeboxServer game, SocketIOState socketIO) {
			super(game, socketIO);
		}

		@Override
		protected Screen getGameScreen() {
			// TODO Auto-generated method stub
			return ((JukeboxServer) game).getJukeboxScreen();
		}


	}

}
