package com.swandev.poker;

import lombok.Getter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.swandev.swanlib.screen.ClientConnectScreen;
import com.swandev.swanlib.socket.SocketIOState;

public class PokerGameClient extends Game {

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
	private HandScreen handScreen;

	@Override
	public void create() {
		spriteBatch = new SpriteBatch();
		assets = new Assets();
		socketIO = new SocketIOState();
		shapeRenderer = new ShapeRenderer();
		connectScreen = new PokerClientConnectScreen(this, socketIO, spriteBatch);
		handScreen = new HandScreen(this);
		setScreen(connectScreen);
	}

	@Override
	public void dispose() {
		super.dispose();
		connectScreen.dispose();
		handScreen.dispose();
		spriteBatch.dispose();
		assets.dispose();
	}

	public static class PokerClientConnectScreen extends ClientConnectScreen {

		public PokerClientConnectScreen(PokerGameClient game, SocketIOState socketIO, SpriteBatch spritebatch) {
			super(game, socketIO, spritebatch);
		}

		@Override
		protected void switchToGame() {
			game.setScreen(((PokerGameClient) game).getHandScreen());
		}
	}
}
