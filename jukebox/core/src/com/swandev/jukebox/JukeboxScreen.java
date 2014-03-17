package com.swandev.jukebox;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.google.common.collect.Lists;
import com.swandev.swanlib.screen.SwanScreen;
import com.swandev.swanlib.socket.EventEmitter;
import com.swandev.swanlib.socket.SocketIOState;


public class JukeboxScreen extends SwanScreen {

	List<SongData> songs = Lists.newArrayList();
	List<SongData> playList = Lists.newArrayList();
	Music currentSong;
	Skin buttonSkin;
	

	public JukeboxScreen(SocketIOState socketIO) {
		super(socketIO);
	}


	@Override
	public void render(float delta) {
		super.render(delta);
		playStatus();
	}

	@Override
	public void show() {
		super.show();
		getSongList();
		playList = songs;
	
	}
	
	public void getSongList(){
		boolean exists = Gdx.files.external("Music/").isDirectory();
		if (!exists){
			//error
		}
		
		FileHandle [] files = Gdx.files.external("Music/").list();
		for (FileHandle song : files){
			String fileName = song.nameWithoutExtension();
			Music songName = Gdx.audio.newMusic(song);
			SongData songData = new SongData(fileName, songName);
			songs.add(songData);
		}
	}
	
	public void playStatus(){
		if (currentSong == null){
			printPlayList();
			currentSong = playList.get(0).music;
			currentSong.play();
		}
		if (!currentSong.isPlaying()){
			removeFromPlayList();
			printPlayList();
			if (!playList.isEmpty()){
				currentSong = songs.get(0).music;
				currentSong.play();
			}
		}
		
	}
	
	
	public synchronized void addToPlayList(SongData song){
		playList.add(song);
	}
	
	
	public synchronized void removeFromPlayList(){
		if (!playList.isEmpty()){
			playList.remove(0);
		}
	}
	
	public void printPlayList(){
		int length = playList.size();
		int i;
		System.out.println("Printing Play List");
		for (i = 0; i < length; i++){
			System.out.println(playList.get(i).songName);
		}
		System.out.println("///////////////////");
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

class SongData{
	String songName;
	Music music;
	
	public SongData(String name, Music file){
		songName = name;
		music = file;
	}
}
