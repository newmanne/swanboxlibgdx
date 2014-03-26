package com.swandev.jukebox;

public class JukeboxLib {

	public static final String SEND_SONGLIST = "send_songlist";
	public static final String REQUEST_SONGLIST = "request_songlist";
	public static final String ADD_TO_PLAYLIST = "add_to_playlist";
	public static final String USER_PLAY = "user_play";
	public static final String USER_PAUSE = "user_pause";
	public static final String USER_NEXT = "user_next";
	public static final String SONG_OVER = "song_over";
	public static final String SCREEN_READY = "screen_ready";
	public static final String CURRENT_SONG = "current_song";

	public static String formatTime(int time) {
		return (time / 60) + ":" + String.format("%02d", time % 60);
	}

}
