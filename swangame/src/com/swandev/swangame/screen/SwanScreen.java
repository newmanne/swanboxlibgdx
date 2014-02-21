package com.swandev.swangame.screen;

import com.badlogic.gdx.Screen;
import com.swandev.swangame.socket.EventEmitter;
import com.swandev.swangame.socket.SocketIOState;

public abstract class SwanScreen implements Screen {

	private SocketIOState socketIO;

	public SwanScreen(SocketIOState socketIO) {
		this.socketIO = socketIO;
	}

	@Override
	public void render(float delta) {
		socketIO.flushEvents();
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void show() {
		registerEvents(socketIO);
	}

	public abstract void registerEvents(SocketIOState socketIO);

	public abstract void unregisterEvents(EventEmitter eventEmitter);

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

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

}
