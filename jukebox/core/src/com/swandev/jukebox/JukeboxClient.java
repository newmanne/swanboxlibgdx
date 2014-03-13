package com.swandev.jukebox;

import lombok.Getter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.swandev.swanlib.screen.ClientConnectScreen;
import com.swandev.swanlib.socket.SocketIOState;

public class JukeboxClient extends Game {

	@Getter
	private SpriteBatch spriteBatch;

	@Getter
	private ShapeRenderer shapeRenderer;

	@Getter
	private Assets assets;

	@Getter
	private SocketIOState socketIO;

	@Getter
	private ClientConnectScreen connectScreen;

	@Getter
	private JukeboxScreen jukeboxScreen;

	@Override
	public void create() {
		spriteBatch = new SpriteBatch();
		assets = new Assets();
		socketIO = new SocketIOState();
		shapeRenderer = new ShapeRenderer();
		connectScreen = new JukeboxClientConnectScreen(this, socketIO, spriteBatch, getAssets().getSkin());
		jukeboxScreen = new JukeboxScreen(getSocketIO());
		setScreen(jukeboxScreen);
	}

	@Override
	public void dispose() {
		super.dispose();
		connectScreen.dispose();
		jukeboxScreen.dispose();
		spriteBatch.dispose();
		assets.dispose();
	}

	public static class JukeboxClientConnectScreen extends ClientConnectScreen {

		public JukeboxClientConnectScreen(JukeboxClient game, SocketIOState socketIO, SpriteBatch spritebatch, Skin skin) {
			super(game, socketIO, spritebatch, skin);
		}

		@Override
		protected void switchToGame() {
			game.setScreen(((JukeboxClient) game).getJukeboxScreen());
		}
	}
}
