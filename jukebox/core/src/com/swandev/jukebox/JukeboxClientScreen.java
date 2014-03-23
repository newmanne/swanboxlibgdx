package com.swandev.jukebox;

import io.socket.IOAcknowledge;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.swandev.swanlib.screen.SwanGameStartScreen;
import com.swandev.swanlib.socket.EventCallback;
import com.swandev.swanlib.socket.SocketIOState;
import com.swandev.swanlib.util.SwanUtil;

public class JukeboxClientScreen extends SwanGameStartScreen {

	final Stage stage;
	// whether or not a user has a song selected
	boolean songSelected = false;
	final com.badlogic.gdx.scenes.scene2d.ui.List<String> list;
	private final JukeboxClient game;
	private ImageButton playPause;
	private ImageButton next;

	private final int fontSize = 20;
	private final Table table;

	private final float VIRTUAL_WIDTH = 600;
	private final float VIRTUAL_HEIGHT = 800;

	private final List<Actor> fontActors;

	private Image backgroundImage;

	public JukeboxClientScreen(SocketIOState socketIO, JukeboxClient game) {
		super(socketIO);

		stage = new Stage(new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT), game.getSpriteBatch());
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
		// final Table table = new Table();
		table = new Table();
		table.setFillParent(true);

		final ScrollPane scroller = new ScrollPane(list);

		final Group group = new Group();
		scroller.setFillParent(true);

		Image backgroundImage = new Image(new TextureRegion(new Texture(Gdx.files.internal("jukeboxBackground.jpg"))));
		backgroundImage.setFillParent(true);
		group.addActor(backgroundImage);
		group.addActor(scroller);

		Label nameLabel = new Label("Swanbox Jukebox:", skin);
		table.add(nameLabel).colspan(2);
		table.row();
		table.add(group).fill().expand().colspan(2);
		table.row();
		addHostButtons(table);
		buildBackground(skin);
		stage.addActor(table);

		fontActors = Lists.<Actor> newArrayList(list, nameLabel);
	}

	private void buildBackground(Skin skin) {
		// Adds a background texture to the stage
		backgroundImage = new Image(new TextureRegion(new Texture(Gdx.files.internal("images/jukeboxBackground.jpg"))));
		backgroundImage.setX(0);
		backgroundImage.setY(0);
		backgroundImage.setWidth(VIRTUAL_WIDTH - 10);
		backgroundImage.setHeight(VIRTUAL_HEIGHT - 10);
		backgroundImage.setFillParent(true);
		stage.addActor(backgroundImage);
	}

	private void addHostButtons(Table table) {
		Gdx.app.log("JUKEBOX", "Adding buttons for host");
		final Skin skin = game.getAssets().getSkin();
		// TODO: make a play/pause button, its dumb to have both
		next = new EventSendingTextButton("SKIP", skin, JukeboxLib.USER_NEXT);
		playPause = new PlayPauseButton(skin);
		table.add(playPause).height(100).width(100).center();
		table.add(next).height(100).width(100).center();
		table.row();
	}

	public class EventSendingTextButton extends ImageButton {

		protected String socketEvent;

		public EventSendingTextButton(String text, Skin skin, final String socketEvent) {
			super(new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("images/next_up.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("images/next_down.png")))));
			this.socketEvent = socketEvent;
			addListener(new ChangeListener() {

				@Override
				public void changed(ChangeEvent event, Actor actor) {
					getSocketIO().emitToScreen(socketEvent);
					playPause.getStyle().imageUp = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("images/pause_up.png"))));
					playPause.getStyle().imageDown = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("images/pause_down.png"))));

				}
			});
		}
	}

	public class PlayPauseButton extends ImageButton {

		final private static String play = "PLAY";
		final private static String pause = "PAUSE";
		private String state;
		Map<String, String> stateToEvents = ImmutableMap.of(play, JukeboxLib.USER_PLAY, pause, JukeboxLib.USER_PAUSE);

		public PlayPauseButton(Skin skin) {
			super(new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("images/pause_up.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("images/pause_down.png")))));
			state = pause;
			addListener(new ChangeListener() {

				@Override
				public void changed(ChangeEvent event, Actor actor) {
					getSocketIO().emitToScreen(stateToEvents.get(state));
					state = state.equals(pause) ? play : pause;
					changePlayPauseButton(state);

				}
			});
		}
	}

	public void changePlayPauseButton(String state) {
		if (state.equals("PAUSE")) {
			playPause.getStyle().imageUp = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("images/pause_up.png"))));
			playPause.getStyle().imageDown = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("images/pause_down.png"))));
		} else {
			playPause.getStyle().imageUp = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("images/play_up.png"))));
			playPause.getStyle().imageDown = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("images/play_down.png"))));
		}

	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
		SwanUtil.resizeFonts(fontActors, game.getAssets().getFontGenerator(), fontSize, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
	}

	@Override
	public void dispose() {
		stage.dispose();
	}

	@Override
	protected void registerEvents() {
		registerEvent(JukeboxLib.SONG_OVER, new EventCallback() {
			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				songSelected = false;
			}
		});
		registerEvent(JukeboxLib.SEND_SONGLIST, new EventCallback() {
			@Override
			public void onEvent(IOAcknowledge arg0, Object... args) {
				Gdx.app.log("JUKEBOX", "song list receieved!");
				final List<String> songs = SwanUtil.parseJsonList((JSONArray) args[0]);
				list.setItems(songs.toArray(new String[songs.size()]));
			}
		});
	}

	@Override
	protected void doRender(float delta) {
		stage.draw();
		stage.act(delta);
	}

	@Override
	public void doShow() {
		playPause.setVisible(getSocketIO().isHost());
		next.setVisible(getSocketIO().isHost());
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	protected void onEveryoneReady() {
		getSocketIO().emitToScreen(JukeboxLib.REQUEST_SONGLIST);
		Gdx.app.log("JUKEBOX", "Requesting song list from server...");
	}
}
