package com.swandev.poker;

import lombok.Getter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.swandev.swanlib.screen.ServerConnectScreen;
import com.swandev.swanlib.socket.SocketIOState;

public class PokerGameServer extends Game {

	@Getter
	private SpriteBatch spriteBatch;

	@Getter
	private Assets assets;

	@Getter
	private SocketIOState socketIO;

	@Getter
	private ServerConnectScreen serverConnectScreen;

	@Getter
	private PokerGameScreen pokerServerScreen;

	@Override
	public void create() {
		spriteBatch = new SpriteBatch();
		assets = new Assets();
		socketIO = new SocketIOState();
		serverConnectScreen = new PokerGameConnectScreen(this, getSocketIO());
		pokerServerScreen = new PokerGameScreen(this);
		setScreen(serverConnectScreen);
		// setScreen(pokerServerScreen);
	}

	@Override
	public void dispose() {
		super.dispose();
		spriteBatch.dispose();
		assets.dispose();
	}

	public static class PokerGameConnectScreen extends ServerConnectScreen {

		public PokerGameConnectScreen(Game game, SocketIOState socketIO) {
			super(game, socketIO);
		}

		@Override
		protected Screen getGameScreen() {
			return ((PokerGameServer) game).getPokerServerScreen();
		}
	}

}
