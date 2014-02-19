package com.swandev.swangame;

import java.net.MalformedURLException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

public class SplashScreen implements Screen {

	final MyGdxGame game;
	final String serverIP = "localhost";
	final int port = 8080;
	final String serverAddress = "http://" + serverIP + ":" + port;

	public SplashScreen(MyGdxGame myGdxGame) {
		this.game = myGdxGame;
	}

	public boolean connect() {
		final SocketIOState socketIO = game.getSocketIO();
		try {
			socketIO.connect(serverAddress, SocketIOState.SCREEN_NAME);
		} catch (MalformedURLException e) {
			Gdx.app.error(LogTags.SOCKET_IO, "Malformed server address " + serverAddress);
		}
		return socketIO.isConnected(); // TODO: obviously you need to block...
	}

	@Override
	public void render(float delta) {
		if (connect()) {
			Gdx.app.log(LogTags.SPLASHSCREEN, "Switching to pattern screen");
			game.setScreen(new PatternScreen(game));
		} else {
			Gdx.app.exit();
		}
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
