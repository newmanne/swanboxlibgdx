package com.swandev.swangame.screen;

import io.socket.IOAcknowledge;
import io.socket.SocketIOException;

import java.net.MalformedURLException;
import java.util.List;

import lombok.Setter;

import org.json.JSONArray;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.swandev.swangame.PatternServerGame;
import com.swandev.swangame.socket.ConnectCallback;
import com.swandev.swangame.socket.EventCallback;
import com.swandev.swangame.socket.EventEmitter;
import com.swandev.swangame.socket.SocketIOEvents;
import com.swandev.swangame.socket.SocketIOState;
import com.swandev.swangame.util.LogTags;
import com.swandev.swangame.util.SwanUtil;

public class ServerConnectScreen implements Screen {

	final PatternServerGame game;
	final String serverIP = "localhost";
	final int port = 8080;
	final String serverAddress = SwanUtil.toAddress(serverIP, Integer.toString(port));
	boolean connectFailed = false;
	@Setter
	boolean gameStarted = false;

	public ServerConnectScreen(PatternServerGame myGdxGame) {
		this.game = myGdxGame;
	}

	public void connect() {
		final SocketIOState socketIO = game.getSocketIO();
		try {
			socketIO.connect(serverAddress, SocketIOState.SCREEN_NAME, true, new ConnectCallback() {

				@Override
				public void onConnect(SocketIOException ex) {
					if (ex != null) {
						connectFailed = true;
					}
				}
			});
		} catch (MalformedURLException e) {
			Gdx.app.error(LogTags.SOCKET_IO, "Malformed server address " + serverAddress);
		}
	}

	@Override
	public void render(float delta) {
		if (game.getSocketIO().isConnected() && gameStarted) {
			Gdx.app.log(LogTags.SPLASHSCREEN, "Switching to pattern screen");
			game.setScreen(game.getPatternServerScreen());
		} else if (connectFailed) {
			Gdx.app.exit();
		}
		game.getSocketIO().flushEvents();
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void show() {
		registerEvents();
		if (!game.getSocketIO().isConnected()) {
			connect();
		}
	}

	private void registerEvents() {
		game.getSocketIO().on(SocketIOEvents.GAME_BEGIN, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				gameStarted = true;
				List<String> playerNames = SwanUtil.parseJsonList((JSONArray) args[0]);
				game.setPlayerNames(playerNames);
			}
		});
	}

	@Override
	public void hide() {
		unregisterEvents();
	}

	private void unregisterEvents() {
		EventEmitter eventEmitter = game.getSocketIO().getEventEmitter();
		eventEmitter.unregisterEvent(SocketIOEvents.GAME_BEGIN);
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

}
