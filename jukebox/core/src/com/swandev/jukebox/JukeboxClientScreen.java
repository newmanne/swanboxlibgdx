package com.swandev.jukebox;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.google.common.collect.Lists;
import com.swandev.swanlib.screen.SwanScreen;
import com.swandev.swanlib.socket.EventEmitter;
import com.swandev.swanlib.socket.SocketIOState;

public class JukeboxClientScreen extends SwanScreen {

	final Stage stage;

	public JukeboxClientScreen(SocketIOState socketIO) {
		super(socketIO);
		stage = new Stage();

		final Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
		final com.badlogic.gdx.scenes.scene2d.ui.List<String> list = new com.badlogic.gdx.scenes.scene2d.ui.List<String>(skin);

		final List<String> items = Lists.newArrayList();
		for (int i = 0; i < 50; i++) {
			items.add("Song " + Integer.toString(i));
		}
		list.setItems(items.toArray(new String[items.size()]));
		final ScrollPane scroller = new ScrollPane(list);
		final Table table = new Table();
		table.setFillParent(true);
		table.add(scroller).fill().expand();
		stage.addActor(table);
		list.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				final String songName = list.getSelected();
				new Dialog("Some Dialog", skin, "dialog") {
					@Override
					protected void result(Object result) {
						if (result.equals(true)) {
							Gdx.app.log("JUKEBOX", "Song " + songName + " selected to be played");
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
