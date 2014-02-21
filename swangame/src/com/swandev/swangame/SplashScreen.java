package com.swandev.swangame;

import io.socket.IOAcknowledge;
import io.socket.SocketIOException;

import java.net.MalformedURLException;
import java.util.List;

import org.json.JSONArray;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

public class SplashScreen implements Screen {

	final PatternServerGame game;
	final String serverIP = "localhost";
	final int port = 8080;
	final String serverAddress = "http://" + serverIP + ":" + port;
	boolean connectFailed = false;
	boolean gameStarted = false;

	public SplashScreen(PatternServerGame myGdxGame) {
		this.game = myGdxGame;
		game.getSocketIO().on(SocketIOEvents.GAME_BEGIN, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				gameStarted = true;
				List<String> playerNames = SwanUtil.parseJsonList((JSONArray) args[0]);
				game.setPlayerNames(playerNames);
			}
		});
		connect();
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
			game.setScreen(new PatternScreen(game));
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
		// TODO Auto-generated method stub

	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

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
