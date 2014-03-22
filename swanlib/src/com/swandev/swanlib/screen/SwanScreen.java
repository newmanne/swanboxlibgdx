package com.swandev.swanlib.screen;

import java.util.List;

import lombok.Getter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.google.common.collect.Lists;
import com.swandev.swanlib.socket.EventCallback;
import com.swandev.swanlib.socket.EventEmitter;
import com.swandev.swanlib.socket.SocketIOState;

public abstract class SwanScreen implements Screen {

	@Getter
	private final SocketIOState socketIO;

	private final List<String> events = Lists.newArrayList();

	public SwanScreen(SocketIOState socketIO) {
		this.socketIO = socketIO;
	}

	@Override
	public void render(float delta) {
		// TODO: These opengl lines should probably be removed, they aren't really the responsibility of this class...
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

	public void registerEvent(String event, EventCallback callback) {
		getSocketIO().on(event, callback);
		events.add(event);
	}

	protected abstract void registerEvents();

	private void unregisterEvents(EventEmitter eventEmitter) {
		for (String event : events) {
			eventEmitter.unregisterEvent(event);
		}
	}

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
