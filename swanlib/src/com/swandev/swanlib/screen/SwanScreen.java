package com.swandev.swanlib.screen;

import lombok.Getter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.swandev.swanlib.socket.EventEmitter;
import com.swandev.swanlib.socket.SocketIOState;

public abstract class SwanScreen implements Screen {

	@Getter
	private SocketIOState socketIO;

	public SwanScreen(SocketIOState socketIO) {
		this.socketIO = socketIO;
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		socketIO.flushEvents();
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void show() {
		registerEvents();
	}

	protected abstract void registerEvents();

	protected abstract void unregisterEvents(EventEmitter eventEmitter);

	@Override
	public void hide() {
		unregisterEvents(socketIO.getEventEmitter());
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

}
