package com.swandev.jukebox;

import java.util.List;
import java.util.Queue;

import lombok.Data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.swandev.swanlib.screen.SwanScreen;
import com.swandev.swanlib.socket.EventEmitter;
import com.swandev.swanlib.socket.SocketIOState;

public class JukeboxScreen extends SwanScreen {

	private static final String MUSIC_DIR = "Music/";
	final List<SongData> songs = Lists.newArrayList();
	final Queue<SongData> playList = Queues.newConcurrentLinkedQueue();

	public JukeboxScreen(SocketIOState socketIO) {
		super(socketIO);
	}

	@Override
	public void render(float delta) {
		super.render(delta);
		jukeboxLogic();
	}

	@Override
	public void show() {
		super.show();
		updateSongList();
		playList.add(songs.get(0));
	}

	public void updateSongList() {
		songs.clear();
		if (!Gdx.files.external(MUSIC_DIR).isDirectory()) {
			throw new RuntimeException("Could not find music directory!");
		}
		final FileHandle[] files = Gdx.files.external(MUSIC_DIR).list();
		for (FileHandle file : files) {
			final String fileName = file.nameWithoutExtension();
			final Music song = Gdx.audio.newMusic(file);
			final SongData songData = new SongData(fileName, song);
			songs.add(songData);
		}
	}

	public void jukeboxLogic() {
		if (playList.isEmpty()) {
			return;
		} else {
			final SongData currentSong = playList.peek();
			final Music currentMusic = currentSong.getMusic();
			if (!currentMusic.isPlaying()) {
				if (currentMusic.getPosition() > 0) { // song over
					playList.remove();
				} else {
					currentMusic.play(); // begin a new song
				}
			}
		}
	}

	public void printPlayList() {
		Gdx.app.log("JUKEBOX", "Printing Play List");
		for (SongData song : songs) {
			Gdx.app.log("JUKEBOX", song.toString());
		}
		Gdx.app.log("JUKEBOX", "//////////////////");
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

	@Data
	public static class SongData {
		private final String songName;
		private final Music music;
	}

}
