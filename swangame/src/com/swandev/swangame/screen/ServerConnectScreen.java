package com.swandev.swangame.screen;

import io.socket.IOAcknowledge;
import io.socket.SocketIOException;

import java.net.MalformedURLException;

import lombok.Setter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.swandev.swangame.PatternServerGame;
import com.swandev.swangame.socket.CommonSocketIOEvents;
import com.swandev.swangame.socket.ConnectCallback;
import com.swandev.swangame.socket.EventCallback;
import com.swandev.swangame.socket.EventEmitter;
import com.swandev.swangame.socket.SocketIOState;
import com.swandev.swangame.util.LogTags;
import com.swandev.swangame.util.SwanUtil;

// TODO: make this abstract class
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
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		if (game.getSocketIO().isConnected() && gameStarted && game.getSocketIO().isPlayerListReady()) {
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
		gameStarted = false;
		game.getSocketIO().setPlayerListReady(false);
		registerEvents();
		if (!game.getSocketIO().isConnected()) {
			connect();
		}
	}

	private void registerEvents() {
		game.getSocketIO().on(CommonSocketIOEvents.GAME_START, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				gameStarted = true;
				game.getSocketIO().requestNicknames();
			}
		});
	}

	@Override
	public void hide() {
		unregisterEvents();
	}

	private void unregisterEvents() {
		EventEmitter eventEmitter = game.getSocketIO().getEventEmitter();
		eventEmitter.unregisterEvent(CommonSocketIOEvents.GAME_START);
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
