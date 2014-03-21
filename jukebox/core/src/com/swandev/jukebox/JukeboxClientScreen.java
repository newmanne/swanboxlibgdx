package com.swandev.jukebox;

import io.socket.IOAcknowledge;

import java.util.List;

import org.json.JSONArray;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.swandev.swanlib.screen.SwanScreen;
import com.swandev.swanlib.socket.EventCallback;
import com.swandev.swanlib.socket.EventEmitter;
import com.swandev.swanlib.socket.SocketIOState;
import com.swandev.swanlib.util.SwanUtil;

public class JukeboxClientScreen extends SwanScreen {

	final Stage stage;
	// whether or not a user has a song selected
	boolean songSelected = false;
	final com.badlogic.gdx.scenes.scene2d.ui.List<String> list;
	private final JukeboxClient game;
	private TextButton pause;
	private TextButton play;
	private TextButton next;

	public JukeboxClientScreen(SocketIOState socketIO, JukeboxClient game) {
		super(socketIO);
		stage = new Stage();
		this.game = game;
		final Skin skin = game.getAssets().getSkin();
		list = new com.badlogic.gdx.scenes.scene2d.ui.List<String>(skin);
		list.addListener(new ChangeListener() {

			// Unfortunately without this hack the user is prompted with a dialog the moment the screen opens
			boolean first = false;

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (!first) {
					first = true;
					return;
				}
				if (songSelected) {
					new Dialog("Can't select this song now", skin, "dialog").text("You already have a song queued to be played").button("OK").show(stage);
				} else {
					selectSong(list.getSelected());
				}
			}

			private void selectSong(final String songName) {
				new Dialog("Select this song?", skin, "dialog") {
					@Override
					protected void result(Object result) {
						if (result.equals(true)) {
							Gdx.app.log("JUKEBOX", "Song " + songName + " selected to be played");
							getSocketIO().emitToScreen(JukeboxLib.ADD_TO_PLAYLIST, getSocketIO().getNickname(), songName);
							songSelected = true;
						}
					}
				}.text("Play " + songName + "?").button("Yes", true).button("No", false).key(Keys.ENTER, true).key(Keys.ESCAPE, false).show(stage);
			}
		});
		final Table table = new Table();
		table.setFillParent(true);
		addHostButtons(table);
		final ScrollPane scroller = new ScrollPane(list);
		table.add(scroller).fill().expand();
		stage.addActor(table);
	}

	private void addHostButtons(Table table) {
		Gdx.app.log("JUKEBOX", "Adding buttons for host");
		final Skin skin = game.getAssets().getSkin();
		// TODO: make a play/pause button, its dumb to have both
		pause = new EventSendingTextButton("PAUSE", skin, JukeboxLib.USER_PAUSE);
		play = new EventSendingTextButton("PLAY", skin, JukeboxLib.USER_PLAY);
		next = new EventSendingTextButton("SKIP", skin, JukeboxLib.USER_NEXT);
		table.add(pause);
		table.add(play);
		table.add(next);
		table.row();
	}

	public class EventSendingTextButton extends TextButton {

		public EventSendingTextButton(String text, Skin skin, final String socketevent) {
			super(text, skin);
			addListener(new ChangeListener() {

				@Override
				public void changed(ChangeEvent event, Actor actor) {
					getSocketIO().emitToScreen(socketevent);
				}
			});
		}
	}

	@Override
	public void render(float delta) {
		super.render(delta);
		stage.draw();
		stage.act(delta);
	}

	@Override
	public void show() {
		super.show();
		play.setVisible(getSocketIO().isHost());
		pause.setVisible(getSocketIO().isHost());
		next.setVisible(getSocketIO().isHost());
		Gdx.input.setInputProcessor(stage);
		getSocketIO().emitToScreen(JukeboxLib.REQUEST_SONGLIST);
		// TODO: until we resolve that bug...
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Gdx.app.log("JUKEBOX", "Requesting song list from server...");
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void dispose() {
		stage.dispose();
	}

	@Override
	protected void registerEvents() {
		getSocketIO().on(JukeboxLib.SONG_OVER, new EventCallback() {
			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				songSelected = false;
			}
		});
		getSocketIO().on(JukeboxLib.SEND_SONGLIST, new EventCallback() {
			@Override
			public void onEvent(IOAcknowledge arg0, Object... args) {
				Gdx.app.log("JUKEBOX", "song list receieved!");
				final List<String> songs = SwanUtil.parseJsonList((JSONArray) args[0]);
				list.setItems(songs.toArray(new String[songs.size()]));
			}
		});
	}

	@Override
	protected void unregisterEvents(EventEmitter eventEmitter) {
		eventEmitter.unregisterEvent(JukeboxLib.SONG_OVER);
		eventEmitter.unregisterEvent(JukeboxLib.SEND_SONGLIST);
	}

}
