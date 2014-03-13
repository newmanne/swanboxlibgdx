package com.swandev.pattern;

import lombok.Getter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.swandev.swangame.screen.PatternServerScreen;
import com.swandev.swanlib.screen.ServerConnectScreen;
import com.swandev.swanlib.socket.SocketIOState;

public class PatternServerGame extends Game {

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
	private PatternServerScreen patternServerScreen;

	@Override
	public void create() {
		spriteBatch = new SpriteBatch();
		assets = new Assets();
		socketIO = new SocketIOState();
		shapeRenderer = new ShapeRenderer();
		serverConnectScreen = new PatternServerConnectScreen(this, getSocketIO());
		patternServerScreen = new PatternServerScreen(this);
		setScreen(serverConnectScreen);
	}

	@Override
	public void dispose() {
		super.dispose();
		spriteBatch.dispose();
		assets.dispose();
	}

	public static class PatternServerConnectScreen extends ServerConnectScreen {

		public PatternServerConnectScreen(Game game, SocketIOState socketIO) {
			super(game, socketIO);
		}

		@Override
		protected Screen getGameScreen() {
			return ((PatternServerGame) game).getPatternServerScreen();
		}

	}

}
