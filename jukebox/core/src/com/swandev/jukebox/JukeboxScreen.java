package com.swandev.jukebox;

import io.socket.IOAcknowledge;

import java.util.HashMap;
import java.util.List;
import java.util.Queue;

import org.json.JSONArray;

import lombok.Data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
//import com.swandev.swangame.socket.SocketIOEvents;
//import com.swandev.swangame.socket.SocketIOEvents;
import com.swandev.swanlib.screen.SwanScreen;
import com.swandev.swanlib.socket.EventCallback;
import com.swandev.swanlib.socket.EventEmitter;
import com.swandev.swanlib.socket.SocketIOState;
import com.swandev.swanlib.util.SwanUtil;

public class JukeboxScreen extends SwanScreen {

	private static final String MUSIC_DIR = "Music/";
	final List<SongData> songs = Lists.newArrayList();
	final Queue<SongData> playList = Queues.newConcurrentLinkedQueue();
	final HashMap<String, String> userRequests = new HashMap<String, String>();
	
	public boolean isPaused = false;
	public boolean soundPlaying = false;
	
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
		//broadcast to all players that the screen is ready.
		getSocketIO().swanBroadcast(JukeboxLib.SCREEN_READY);
		//playList.add(songs.get(0));
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
			System.out.println("Playlist Empty");
			return;
		} else {
			final SongData currentSong = playList.peek();
			final Music currentMusic = currentSong.getMusic();
			if (!currentMusic.isPlaying()) {
				if (!isPaused){
					if (soundPlaying){
						removeFromPlaylist(currentSong);
						soundPlaying = false;
					} else {
						currentMusic.play(); // begin a new song
						soundPlaying = true;
					}
				}
			}
		}
	}
	
	public void removeFromPlaylist(SongData currentSong){
		final Music currentMusic = currentSong.getMusic();
		String songName = currentSong.getSongName();
		String sender = userRequests.get(songName);
		currentMusic.stop();
		playList.remove();
		getSocketIO().swanEmit(JukeboxLib.SONG_OVER, sender);
			
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
		int i = 0;
		for (i = 0; i < songs.size(); i++){
			SongData song = songs.get(i);
			if (song.songName.equals(songName)){
				break;
			}
		}
		
		playList.add(songs.get(i));
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
				List<String> songNames = Lists.newArrayList();
				for (int i = 0 ; i < songs.size(); i++){
					songNames.add(songs.get(i).songName);
				}
				System.out.println("IN REQUEST SONGLIST");
				
				
				getSocketIO().swanBroadcast(JukeboxLib.SEND_SONGLIST,songNames);
			}

		});
		
		getSocketIO().on(JukeboxLib.ADD_TO_PLAYLIST, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				String sender = (String) args[0];
				String songName = (String) args[1];
				userRequests.put(songName, sender);
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
				jukeboxPause();
			}

		});
		
		getSocketIO().on(JukeboxLib.USER_NEXT, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				jukeboxNext();
			}

		});

	}

	@Override
	protected void unregisterEvents(EventEmitter eventEmitter) {
		// TODO Auto-generated method stub
		eventEmitter.unregisterEvent(JukeboxLib.ADD_TO_PLAYLIST);
		eventEmitter.unregisterEvent(JukeboxLib.REQUEST_SONGLIST);
		eventEmitter.unregisterEvent(JukeboxLib.USER_NEXT);
		eventEmitter.unregisterEvent(JukeboxLib.USER_PLAY);
		eventEmitter.unregisterEvent(JukeboxLib.USER_PAUSE);
	}

	@Data
	public static class SongData {
		private final String songName;
		private final Music music;
	}

}
