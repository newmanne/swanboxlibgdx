package com.swandev.swangame.screen;

import io.socket.IOAcknowledge;
import io.socket.SocketIOException;

import java.net.MalformedURLException;
import java.util.Map;

import lombok.Setter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.swandev.swangame.socket.CommonSocketIOEvents;
import com.swandev.swangame.socket.ConnectCallback;
import com.swandev.swangame.socket.EventCallback;
import com.swandev.swangame.socket.EventEmitter;
import com.swandev.swangame.socket.SocketIOState;
import com.swandev.swangame.util.CommonLogTags;
import com.swandev.swangame.util.ConnectParams;

/** This is an invisible generic connect screen that handles the reconnecting to server. It should be placed at the star
 * of every game.
 */
public abstract class ClientConnectSplashScreen extends SwanScreen {
	
	protected final Game game;
	private SocketIOException connectError;
	@Setter
	boolean gameStarted = false;
	Map<String, String> connectParams;

	public ClientConnectSplashScreen(Game game, SocketIOState socketIO, Map<String, String> connectParams) {
		super(socketIO);
		this.game = game;
		this.connectParams = connectParams;
	}

	public void connect(String nickname, String address) {
		try {
			getSocketIO().connect(address, nickname, false, new ConnectCallback() {

				@Override
				public void onConnect(SocketIOException ex) {
					connectError = ex;
				}
			});
		} catch (MalformedURLException e) {
			Gdx.app.error(CommonLogTags.SOCKET_IO, "Malformed server address " + address);
		}
	}

	@Override
	public void render(float delta) {
		super.render(delta);
		if (getSocketIO().isConnected() && gameStarted) {
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
		if (!getSocketIO().isConnected()) {
			connect(connectParams.get(ConnectParams.NICKNAME), connectParams.get(ConnectParams.SERVER_ADDRESS));
		}
	}

	@Override
	protected void registerEvents() {
		getSocketIO().on(CommonSocketIOEvents.GAME_START, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				gameStarted = true;
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
