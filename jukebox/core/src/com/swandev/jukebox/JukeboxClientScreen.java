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

	public JukeboxClientScreen(SocketIOState socketIO) {
		super(socketIO);
		stage = new Stage();

		final Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
		list = new com.badlogic.gdx.scenes.scene2d.ui.List<String>(skin);
		final ScrollPane scroller = new ScrollPane(list);
		final Table table = new Table();
		table.setFillParent(true);
		table.add(scroller).fill().expand();
		stage.addActor(table);
		list.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if (songSelected) {
					new Dialog("Can't select this song now", skin, "dialog").text("You already have a song queued to be played. Please wait for it to finish before selecting a new song").button("OK");
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
							getSocketIO().emitToScreen(JukeboxLib.ADD_TO_PLAYLIST, songName);
							songSelected = true;
						}
					}
				}.text("Play " + songName + "?").button("Yes", true).button("No", false).key(Keys.ENTER, true).key(Keys.ESCAPE, false).show(stage);
			}
		});

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
		Gdx.input.setInputProcessor(stage);
		getSocketIO().emitToScreen(JukeboxLib.REQUEST_SONGLIST);
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

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
