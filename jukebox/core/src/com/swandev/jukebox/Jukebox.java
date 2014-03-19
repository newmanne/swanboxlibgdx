package com.swandev.jukebox;

import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Music.OnCompletionListener;
import com.badlogic.gdx.files.FileHandle;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/** Note that we are NOT in a multi-threaded environment, so don't think too hard about threadsafe */
@RequiredArgsConstructor
public class Jukebox {

	private static final String MUSIC_DIR = "Music/";
	private final Map<String, SongData> songs = Maps.newHashMap();
	private final List<SongRequest> playList = Lists.newArrayList();

	final JukeboxServerScreen jukeboxServerScreen;

	public List<SongRequest> getPlayList() {
		return Lists.newArrayList(playList);
	}

	public void reset() {
		dispose();
		refreshLibrary();
	}

	public void dispose() {
		for (SongData song : songs.values()) {
			song.getMusic().dispose();
		}
		songs.clear();
		playList.clear();
	}

	/** Return a list of all the song names */
	public Set<String> getLibrary() {
		return songs.keySet();
	}

	/** Scan the directory and update the songs available */
	private void refreshLibrary() {
		if (!Gdx.files.external(MUSIC_DIR).isDirectory()) {
			throw new RuntimeException("Could not find music directory!");
		}
		final FileHandle[] files = Gdx.files.external(MUSIC_DIR).list();
		for (final FileHandle file : files) {
			final String fileName = file.nameWithoutExtension();
			final Music song = Gdx.audio.newMusic(file);
			final SongData songData = new SongData(fileName, song);
			songs.put(songData.getSongName(), songData);
		}
	}

	public SongRequest getCurrentSongRequest() {
		return playList.isEmpty() ? null : playList.get(0);
	}

	public SongData getCurrentSongData() {
		final SongRequest request = getCurrentSongRequest();
		return request == null ? null : songs.get(request.getSongName());
	}

	public void play() {
		final SongData currentSong = getCurrentSongData();
		if (currentSong != null) {
			final Music music = currentSong.getMusic();
			music.setOnCompletionListener(new OnCompletionListener() {

				@Override
				public void onCompletion(Music music) {
					popPlaylist();
				}
			});
			music.play();
		}
	}

	public void pause() {
		final SongData currentSong = getCurrentSongData();
		if (currentSong != null) {
			currentSong.getMusic().pause();
		}
	}

	public void popPlaylist() {
		final SongData currentSong = getCurrentSongData();
		if (currentSong != null) {
			final String requester = getCurrentSongRequest().getRequester();
			playList.remove(0);
			currentSong.getMusic().stop();
			jukeboxServerScreen.getSocketIO().swanEmit(JukeboxLib.SONG_OVER, requester);
			jukeboxServerScreen.uiUpdatePlayList();
			play(); // activate the next song, if present
		}
	}

	public void next() {
		popPlaylist();
	}

	public void request(final String sender, final String songName) {
		Preconditions.checkArgument(songs.get(songName) != null);
		final SongRequest request = new SongRequest(sender, songName);
		playList.add(request);
		play(); // might be possible to play right now
	}

	@Data
	public static class SongData {
		private final String songName;
		private final Music music;
	}

	@Data
	public static class SongRequest {
		private final String requester;
		private final String songName;
	}

}
