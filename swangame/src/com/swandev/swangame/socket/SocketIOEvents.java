package com.swandev.swangame.socket;

public class SocketIOEvents {

	public static final String NICKNAME_SET = "nickname_set";

	// pattern game
	public static final String PLAYING_PATTERNS = "playing_patterns";
	public static final String FINISHED_SEQUENCE = "finished_sequence";
	public static final String UPDATE_SEQUENCE = "update_sequence";
	public static final String PATTERN_ENTERED = "pattern_entered";
	public static final String PATTERN_REQUESTED = "pattern_requested";

	public static final String ELECTED_CLIENT = "elected_client";
	public static final String ELECTED_HOST = "elected_host";
	public static final String ANNOUNCEMENT = "announcement";
	public static final String INVALID_PATTERN = "invalid_pattern";
	public static final String GAME_OVER = "game_over";

	public static final String SWAN_EMIT = "swan_emit";
	public static final String SWAN_BROADCAST = "swan_broadcast";

	public static final String GET_NICKNAMES = "swan_get_nicknames";

	public static final String SCREEN_SET = "screen_set";

}
