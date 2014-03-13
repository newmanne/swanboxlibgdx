package com.swandev.jukebox;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.google.common.collect.Lists;
import com.swandev.swanlib.screen.SwanScreen;
import com.swandev.swanlib.socket.EventEmitter;
import com.swandev.swanlib.socket.SocketIOState;

public class JukeboxScreen extends SwanScreen {

	List<Music> songs = Lists.newArrayList();

	public JukeboxScreen(SocketIOState socketIO) {
		super(socketIO);
	}

	@Override
	public void render(float delta) {
		super.render(delta);
	}

	@Override
	public void show() {
		super.show();
		// Get song list
		for (FileHandle song : Gdx.files.internal("music").list()) {
			songs.add(Gdx.audio.newMusic(song));
		}
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void registerEvents() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void unregisterEvents(EventEmitter arg0) {
		// TODO Auto-generated method stub

	}

}
