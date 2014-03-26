package com.swandev.jukebox;

import io.socket.IOAcknowledge;

import java.util.Collection;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.google.common.collect.Lists;
import com.swandev.jukebox.Jukebox.SongData;
import com.swandev.swanlib.screen.SwanGameStartScreen;
import com.swandev.swanlib.socket.EventCallback;
import com.swandev.swanlib.socket.SocketIOState;
import com.swandev.swanlib.util.SwanUtil;

public class JukeboxClientScreen extends SwanGameStartScreen {

	final Stage stage;
	boolean songSelected = false;
	private final JukeboxClient game;
	private ImageButton playPause;
	private ImageButton next;
	private final Label currentSongInfo;

	private final int fontSize = 20;
	private final Table table;

	private final float VIRTUAL_WIDTH = 600;
	private final float VIRTUAL_HEIGHT = 800;

	private final List<Actor> fontActors;

	private Image backgroundImage;

	private final Group songGroup;

	public Skin skin;
	private Label yourSelectionInfo;

	private static final Json json = new Json();

	private Collection<SongData> songs;

	private boolean yourSongIsPlaying = false;

	public JukeboxClientScreen(SocketIOState socketIO, JukeboxClient game) {
		super(socketIO);

		stage = new Stage(new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT), game.getSpriteBatch());
		this.game = game;
		skin = game.getAssets().getSkin();

		table = new Table();
		table.setFillParent(true);
		songGroup = new Group();

		Label nameLabel = new Label("Swanbox Jukebox:", skin);
		Label currentSongLabel = new Label("Current Song: ", skin);
		Label yourSelectionLabel = new Label("Your selection: ", skin);
		yourSelectionInfo = new Label("", skin);
		table.add(nameLabel).colspan(2);
		table.row();

		currentSongInfo = new Label("", skin);
		table.add(currentSongLabel);
		table.add(currentSongInfo).left();
		table.row();
		table.add(yourSelectionLabel);
		table.add(yourSelectionInfo).left();
		table.row();
		table.add(songGroup).fill().expand().colspan(2);
		table.row();
		addButtons(table);
		buildBackground(skin);
		stage.addActor(table);

		// how do we we set the current song label to fixed font??
		fontActors = Lists.<Actor> newArrayList(nameLabel, currentSongLabel, currentSongInfo, yourSelectionLabel, yourSelectionInfo);
	}

	private void buildBackground(Skin skin) {
		// Adds a background texture to the stage
		backgroundImage = new Image(new TextureRegion(new Texture(Gdx.files.internal("images/jukeboxBackground.jpg"))));
		backgroundImage.setBounds(0, 0, VIRTUAL_WIDTH - 10, VIRTUAL_HEIGHT - 10);
		backgroundImage.setFillParent(true);
		stage.addActor(backgroundImage);
	}

	private void addButtons(Table table) {
		final Skin skin = game.getAssets().getSkin();
		next = new EventSendingTextButton("SKIP", skin, JukeboxLib.USER_NEXT);
		playPause = new PlayPauseButton(getSocketIO());
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

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
		SwanUtil.resizeFonts(fontActors, game.getAssets().getFontGenerator(), fontSize, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
	}

	@Override
	public void dispose() {
		stage.dispose();
	}

	private boolean isMySongPlaying() {
		return yourSongIsPlaying;
	}

	@Override
	protected void registerEvents() {
		registerEvent(JukeboxLib.SONG_OVER, new EventCallback() {
			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				songSelected = false;
				yourSongIsPlaying = false;
				yourSelectionInfo.setText("");
				currentSongInfo.setText("");
			}

		});
		registerEvent(JukeboxLib.SEND_SONGLIST, new EventCallback() {
			@Override
			public void onEvent(IOAcknowledge arg0, Object... args) {
				Gdx.app.log("JUKEBOX", "song list receieved!");
				// This code is messy and exemplifies the problem that we are using 3 different json processing engines in the same project. But whatever, that's not going to change now.
				songs = Lists.newArrayList();
				Gdx.app.log("HELP", JSONObject.valueToString(args));
				JSONArray jsonArray = (JSONArray) args[0];
				for (int i = 0; i < jsonArray.length(); i++) {
					songs.add(json.fromJson(SongData.class, jsonArray.getJSONObject(i).toString()));
				}
				buildSongList(songs);
			}
		});

		registerEvent(JukeboxLib.CURRENT_SONG, new EventCallback() {
			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				currentSongInfo.setText((CharSequence) args[0]);
				final String requester = (String) args[1];
				if (requester.equals(getSocketIO().getNickname())) {
					yourSongIsPlaying = true;
				}
			}
		});

	}

	private void buildSongList(Collection<SongData> songs) {
		songGroup.clear();
		final Table songTable = new Table();
		final ScrollPane scroller = new ScrollPane(songTable);

		for (SongData song : songs) {
			songTable.add(new SongInfoTable(song));
			songTable.row().height(fontSize * 2f);
		}
		songTable.top();
		scroller.setFillParent(true);
		songGroup.addActor(scroller);
	}

	public class SongInfoTable extends Table {

		// hacky way to record the "key" to use
		final String songName;

		public SongInfoTable(SongData songData) {
			super();
			songName = songData.toString();
			defaults().pad(10).left();
			add(new Label(songData.getSongName(), skin));
			add(new Label("(" + songData.getArtist() + ")", skin));
			addListener(new ClickListener() {

				@Override
				public void clicked(InputEvent event, float x, float y) {
					if (isMySongPlaying()) {
						new Dialog("Can't select this song now", skin, "dialog").text("Please wait until your song finishes playing").button("OK").show(stage);
					} else {
						selectSong(songName);
					}
				}

				private void selectSong(final String songName) {
					final String dialogText = songSelected ? "Switch request to " : "Request ";
					new Dialog("Select this song?", skin, "dialog") {
						@Override
						protected void result(Object result) {
							if (result.equals(true)) {
								Gdx.app.log("JUKEBOX", "Song " + songName + " selected to be played");
								getSocketIO().emitToScreen(JukeboxLib.ADD_TO_PLAYLIST, getSocketIO().getNickname(), songName);
								songSelected = true;
								yourSelectionInfo.setText(songName);
							}
						}
					}.text(dialogText + songName + "?").button("Yes", true).button("No", false).key(Keys.ENTER, true).key(Keys.ESCAPE, false).show(stage);
				}
			});
		}
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
