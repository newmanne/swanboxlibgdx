package com.swandev.jukebox;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.TagException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Music.OnCompletionListener;
import com.badlogic.gdx.files.FileHandle;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@RequiredArgsConstructor
public class Jukebox {

	private static final String MUSIC_DIR = "Music/";
	private final Map<String, SongData> songs = Maps.newHashMap();
	private final List<SongRequest> playList = Lists.newArrayList();

	final JukeboxServerScreen jukeboxServerScreen;
	private boolean paused = false;

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
	public Collection<SongData> getLibrary() {
		return songs.values();
	}

	/** Scan the directory and update the songs available */
	private void refreshLibrary() {
		if (!Gdx.files.external(MUSIC_DIR).isDirectory()) {
			throw new RuntimeException("Could not find music directory!");
		}
		final FileHandle[] files = Gdx.files.external(MUSIC_DIR).list();
		for (final FileHandle file : files) {
			if (file.extension().equals("mp3")) {
				try {
					addSongToLibrary(file);
				} catch (Exception e) { // yes, catchall exceptions are bad, blah blah blah
					Gdx.app.log("JUKEBOX", "Error adding " + file.name() + " to the library", e);
				}
			}
		}
	}

	private void addSongToLibrary(final FileHandle file) throws CannotReadException, IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException, KeyNotFoundException {
		final AudioFile audioFile = AudioFileIO.read(file.file());
		final String title = audioFile.getTag().getFirst(FieldKey.TITLE);
		final Music song = Gdx.audio.newMusic(file);
		int duration = audioFile.getAudioHeader().getTrackLength();
		String artist;
		try {
			artist = audioFile.getTag().getFirst(FieldKey.ARTIST);
		} catch (KeyNotFoundException e) {
			artist = "UNKNOWN";
		}
		final SongData songData = new SongData(title, song, duration, artist);
		Gdx.app.log("JUKEBOX", "Adding to library: " + songData);
		// TODO: handle duplicates?
		songs.put(songData.toString(), songData);
	}

	public SongRequest getCurrentSongRequest() {
		return playList.isEmpty() ? null : playList.get(0);
	}

	public SongData getCurrentSongData() {
		final SongRequest request = getCurrentSongRequest();
		return request == null ? null : songs.get(request.getSongName());
	}

	public void play() {
		paused = false;
		final SongData currentSong = getCurrentSongData();
		if (currentSong != null) {
			final Music music = currentSong.getMusic();
			if (!music.isPlaying()) {
				music.setOnCompletionListener(new OnCompletionListener() {

					@Override
					public void onCompletion(Music music) {
						popPlaylist();
					}
				});
				music.play();
				jukeboxServerScreen.getSocketIO().swanBroadcast(JukeboxLib.CURRENT_SONG, getCurrentSongData().toString(), getCurrentSongRequest().getRequester());
			}
		}
	}

	public void pause() {
		paused = true;
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
		// remove an old request if it exists, bump your new request down
		Optional<SongRequest> previousRequest = Iterables.tryFind(playList, new Predicate<SongRequest>() {
			@Override
			public boolean apply(SongRequest request) {
				return request.getRequester().equals(sender);
			}
		});
		if (previousRequest.isPresent()) {
			playList.remove(previousRequest.get());
		}
		playList.add(request);
		if (!paused) {
			play(); // might be possible to play right now
		}
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class SongData {
		private String songName;
		// exclude from json
		private transient Music music;
		private int lengthInSeconds;
		private String artist;

		@Override
		public String toString() {
			return songName + " (" + artist + ")";
		}
	}

	@Data
	public static class SongRequest {
		private final String requester;
		private final String songName;
	}

}
