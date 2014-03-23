package com.swandev.jukebox;

import io.socket.IOAcknowledge;

import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.swandev.jukebox.Jukebox.SongData;
import com.swandev.jukebox.Jukebox.SongRequest;
import com.swandev.swanlib.screen.SwanGameStartScreen;
import com.swandev.swanlib.socket.EventCallback;
import com.swandev.swanlib.socket.SocketIOState;

public class JukeboxServerScreen extends SwanGameStartScreen {

	private final Jukebox jukebox;
	private final Stage stage;
	private final Table playListTable;
	private final JukeboxServer game;
	private final Label timeElapsed;
	private final CubeAnimation cubeAnimation;

	public JukeboxServerScreen(SocketIOState socketIO, JukeboxServer game) {
		super(socketIO);
		this.game = game;
		jukebox = new Jukebox(this);
		cubeAnimation = new CubeAnimation();
		stage = new Stage();

		timeElapsed = new Label("", game.getAssets().getSkin());
		playListTable = new Table();
		stage.addActor(playListTable);
	}

	@Override
	protected void doRender(float delta) {
		cubeAnimation.render();
		stage.draw();
		stage.act(delta);
		final SongData songData = jukebox.getCurrentSongData();
		if (songData != null) {
			timeElapsed.setText(formatTimeElapsed(songData));
		}
	}

	@Override
	public void doShow() {
		jukebox.reset();
	}

	public void uiUpdatePlayList() {
		final Skin skin = game.getAssets().getSkin();
		playListTable.clear();
		final List<SongRequest> requests = jukebox.getPlayList();
		playListTable.add(new Label("Time elapsed: ", skin));
		playListTable.add(timeElapsed);
		playListTable.row();
		playListTable.add(new Label("Song:", skin));
		playListTable.add(new Label("Requester:", skin));
		playListTable.row();
		for (final SongRequest request : requests) {
			final Label song = new Label(request.getSongName(), skin);
			final Label requester = new Label(request.getRequester(), skin);
			playListTable.add(song);
			playListTable.add(requester);
			playListTable.row();
		}
		playListTable.setFillParent(true);
	}

	@Override
	public void dispose() {
		jukebox.dispose();
		stage.dispose();
		cubeAnimation.dispose();
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	@Override
	protected void registerEvents() {
		registerEvent(JukeboxLib.REQUEST_SONGLIST, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				getSocketIO().swanBroadcast(JukeboxLib.SEND_SONGLIST, jukebox.getLibrary());
			}

		});
		registerEvent(JukeboxLib.ADD_TO_PLAYLIST, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				final String sender = (String) args[0];
				final String songName = (String) args[1];
				jukebox.request(sender, songName);
				uiUpdatePlayList();
			}

		});
		registerEvent(JukeboxLib.USER_PLAY, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				jukebox.play();
				cubeAnimation.resume();
			}
		});
		registerEvent(JukeboxLib.USER_PAUSE, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				jukebox.pause();
				cubeAnimation.pause();
			}

		});
		getSocketIO().on(JukeboxLib.USER_NEXT, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				jukebox.next();
			}

		});

	}

	private String formatTimeElapsed(SongData song) {
		return String.format("%.1f / %d s", song.getMusic().getPosition(), song.getLengthInSeconds());
	}

	@Override
	protected void onEveryoneReady() {
	}

}
