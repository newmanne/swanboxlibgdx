package com.swandev.pokergame;

import lombok.Getter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.swandev.swangame.screen.ClientConnectScreen;
import com.swandev.swangame.socket.SocketIOState;

public class PokerGame extends Game {
	
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
		connectScreen = new PokerClientConnectScreen(this, socketIO, spriteBatch, getAssets().getSkin());
		handScreen = new HandScreen(this);
		//setScreen(handScreen);
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

		public PokerClientConnectScreen(PokerGame game, SocketIOState socketIO, SpriteBatch spritebatch, Skin skin) {
			super(game, socketIO, spritebatch, skin);
		}

		@Override
		protected void switchToGame() {
			game.setScreen(((PokerGame) game).getHandScreen());
		}
	}
}
