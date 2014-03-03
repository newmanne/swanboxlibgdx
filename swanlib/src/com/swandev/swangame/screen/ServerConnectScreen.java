package com.swandev.swangame.screen;

import io.socket.SocketIOException;

import java.net.MalformedURLException;

import lombok.Setter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.swandev.swangame.socket.ConnectCallback;
import com.swandev.swangame.socket.EventEmitter;
import com.swandev.swangame.socket.SocketIOState;
import com.swandev.swangame.util.CommonLogTags;
import com.swandev.swangame.util.SwanUtil;

public abstract class ServerConnectScreen extends SwanScreen {

	protected final Game game;
	final String serverIP = "localhost";
	final int port = 8080;
	final String serverAddress = SwanUtil.toAddress(serverIP, Integer.toString(port));
	private SocketIOException connectError;
	@Setter
	boolean gameStarted = false;

	public ServerConnectScreen(Game game, SocketIOState socketIO) {
		super(socketIO);
		this.game = game;
	}

	public void connect() {
		try {
			getSocketIO().connect(serverAddress, SocketIOState.SCREEN_NAME, true, new ConnectCallback() {

				@Override
				public void onConnect(SocketIOException ex) {
					connectError = ex;
					if (ex == null) {
						getSocketIO().requestNicknames();
					}
				}
			});
		} catch (MalformedURLException e) {
			Gdx.app.error(CommonLogTags.SOCKET_IO, "Malformed server address " + serverAddress);
		}
	}

	@Override
	public void render(float delta) {
		super.render(delta);
		if (getSocketIO().isConnected() && getSocketIO().isPlayerListReady()) {
			Gdx.app.log(CommonLogTags.SERVER_CONNECT_SCREEN, "Switching to game");
			game.setScreen(getGameScreen());
		} else if (connectError != null) {
			Gdx.app.error(CommonLogTags.SOCKET_IO, "Error connecting to server, exiting", connectError);
			Gdx.app.exit();
		}
	}

	protected abstract Screen getGameScreen();

	@Override
	public void show() {
		super.show();
		gameStarted = false;
		getSocketIO().setPlayerListReady(false);
		if (!getSocketIO().isConnected()) {
			connect();
		}
	}

	@Override
	protected void registerEvents() {
	}

	@Override
	protected void unregisterEvents(EventEmitter eventEmitter) {
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

}
