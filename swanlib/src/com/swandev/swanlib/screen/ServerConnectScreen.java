package com.swandev.swanlib.screen;

import io.socket.IOAcknowledge;
import io.socket.SocketIOException;

import java.net.MalformedURLException;

import lombok.Setter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.swandev.swanlib.socket.CommonSocketIOEvents;
import com.swandev.swanlib.socket.ConnectCallback;
import com.swandev.swanlib.socket.EventCallback;
import com.swandev.swanlib.socket.EventEmitter;
import com.swandev.swanlib.socket.SocketIOState;
import com.swandev.swanlib.util.CommonLogTags;
import com.swandev.swanlib.util.SwanUtil;

public abstract class ServerConnectScreen extends SwanScreen {

	protected final Game game;
	final String serverIP = "localhost";
	final int port = 8080;
	final String serverAddress = SwanUtil.toAddress(serverIP, Integer.toString(port));
	boolean connectFailed = false;
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
					if (ex != null) {
						connectFailed = true;
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
		if (getSocketIO().isConnected() && gameStarted && getSocketIO().isPlayerListReady()) {
			Gdx.app.log(CommonLogTags.SERVER_CONNECT_SCREEN, "Switching to game");
			game.setScreen(getGameScreen());
		} else if (connectFailed) {
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
		getSocketIO().on(CommonSocketIOEvents.GAME_START, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				gameStarted = true;
				getSocketIO().requestNicknames();
			
			}
		});

	}

	@Override
	protected void unregisterEvents(EventEmitter eventEmitter) {
		eventEmitter.unregisterEvent(CommonSocketIOEvents.GAME_START);
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

}
