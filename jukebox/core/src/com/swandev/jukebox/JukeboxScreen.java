package com.swandev.jukebox;

import io.socket.IOAcknowledge;

import java.util.List;
import java.util.Queue;

import lombok.Data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
//import com.swandev.swangame.socket.SocketIOEvents;
import com.swandev.swanlib.screen.SwanScreen;
import com.swandev.swanlib.socket.EventCallback;
import com.swandev.swanlib.socket.EventEmitter;
import com.swandev.swanlib.socket.SocketIOState;

public class JukeboxScreen extends SwanScreen {

	private static final String MUSIC_DIR = "Music/";
	final List<SongData> songs = Lists.newArrayList();
	final Queue<SongData> playList = Queues.newConcurrentLinkedQueue();
	public boolean isPaused = false;

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
		//broadcast to all players the current song list on the system.
		getSocketIO().swanBroadcast(JukeboxLib.SEND_SONGLIST, songs);
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
				if (!isPaused){
					if (currentMusic.getPosition() > 0) { // song over
						playList.remove();
					} else {
						currentMusic.play(); // begin a new song
					}
				}
			}
		}
	}
	
	public void jukeboxPlay(){
		Music currentSong = playList.peek().getMusic();
		currentSong.play();
		isPaused = false;
	}
	
	public void jukeboxPause(){
		Music currentSong = playList.peek().getMusic();
		isPaused = true;
		currentSong.pause();
	}
	
	public void jukeboxNext(){
		Music currentSong = playList.peek().getMusic();
		currentSong.stop();
	}
	
	
	public void addToPlaylist(String songName){
		playList.add(songs.get(songs.indexOf(songName)));
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
		
		getSocketIO().on(JukeboxLib.REQUEST_SONGLIST, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				//getSocketIO().swanEmit(JukeboxLib.SEND_SONGLIST, , args)
				getSocketIO().swanBroadcast(JukeboxLib.SEND_SONGLIST, songs);
			}

		});
		
		getSocketIO().on(JukeboxLib.ADD_TO_PLAYLIST, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				String songName = (String) args[0];
				addToPlaylist(songName);
			}

		});
		
		getSocketIO().on(JukeboxLib.USER_PLAY, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				jukeboxPlay();
			}
		});
		
		getSocketIO().on(JukeboxLib.USER_PAUSE, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				String songName = (String) args[0];
				addToPlaylist(songName);
			}

		});

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
