package com.swandev.swanlib.screen;

import io.socket.IOAcknowledge;
import io.socket.SocketIOException;

import java.net.MalformedURLException;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Orientation;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.swandev.swanlib.socket.CommonSocketIOEvents;
import com.swandev.swanlib.socket.ConnectCallback;
import com.swandev.swanlib.socket.EventCallback;
import com.swandev.swanlib.socket.SocketIOState;
import com.swandev.swanlib.util.CommonLogTags;
import com.swandev.swanlib.util.SwanUtil;

public abstract class ClientConnectScreen extends SwanScreen {

	private static final int LABEL_FIELD_PADDING = 20;
	private static final int FIELD_WIDTH = 400;
	private static final int defaultFontSize = 30;
	protected final Game game;
	private final Stage stage;
	private final Skin skin;
	private final TextField ipAddressField;
	private final TextField portField;
	private final TextField nicknameField;
	private final TextButton connectButton;
	private final TextButton gameStart;
	private final TextButton updateButton;
	private Table table;
	private final Label waitingText;

	private float VIRTUAL_WIDTH = 800;
	private float VIRTUAL_HEIGHT = 600;
	private final FreeTypeFontGenerator fontGenerator;
	private final Label ipAddressLabel;
	private final Label portLabel;
	private final Label nicknameLabel;
	private final Label announcementLabel;

	private Image backgroundImage;
	private final List<Actor> fontActors;

	public ClientConnectScreen(final Game game, final SocketIOState socketIO, final SpriteBatch spritebatch) {
		super(socketIO);
		this.game = game;
		this.skin = new Skin(Gdx.files.classpath("skins/uiskin.json"));
		fontGenerator = new FreeTypeFontGenerator(Gdx.files.classpath("fonts/arial.ttf"));
		this.stage = new Stage(new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT), spritebatch);
		
		Orientation orientation = Gdx.input.getNativeOrientation();
		
		switch (orientation){
			case Landscape:
				VIRTUAL_WIDTH = 800;
				VIRTUAL_HEIGHT = 600;
				break;
			case Portrait:
				VIRTUAL_WIDTH = 600;
				VIRTUAL_HEIGHT = 800;
				break;
			default:
				VIRTUAL_WIDTH = 800;
				VIRTUAL_HEIGHT = 600;
				break;
		}

		final String defaultIP = Gdx.app.getType() == ApplicationType.Desktop ? "localhost" : "192.168.0.100";
		ipAddressField = new TextField(defaultIP, skin);
		ipAddressField.setMessageText("IP Address");

		portField = new TextField("8080", skin);
		portField.setMessageText("Port");
		final List<String> sampleNames = ImmutableList.of("Blinky", "Pacman", "Robocop", "DemonSlayer", "HAL", "ChickenLittle", "HansSolo", "Yoshi", "LittleEngineThatCould", "Ghost", "GoLeafsGo", "Batman");
		final String defaultName = sampleNames.get(RandomUtils.nextInt(0, sampleNames.size()));
		nicknameField = new TextField(defaultName + RandomStringUtils.randomNumeric(3), skin);
		nicknameField.setMessageText("Blinky");

		connectButton = new TextButton("Connect", skin);
		connectButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				connectButton.setDisabled(true);
				connectButton.setVisible(false);
				if (!getSocketIO().isConnected()) {
					connect();
				}
			}
		});
		updateButton = new TextButton("Update Nickname", skin);
		updateButton.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				getSocketIO().setNickname(nicknameField.getText());
				updateButton.setVisible(false);
				updateButton.setDisabled(true);
			}

		});
		updateButton.setDisabled(true);
		updateButton.setVisible(false);

		gameStart = new TextButton("Start", skin);
		gameStart.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				// this is a special event, emit directly to server
				socketIO.getClient().emit(CommonSocketIOEvents.GAME_START);
			}

		});
		gameStart.setVisible(false);
		gameStart.setDisabled(true);

		ipAddressLabel = new Label("IP Address", skin);
		portLabel = new Label("Port", skin);
		nicknameLabel = new Label("Nickname", skin);
		announcementLabel = new Label("", skin);

		waitingText = new Label("Waiting for host to select the game", skin);
		waitingText.setVisible(false);
		buildBackground(skin);

		buildTable(skin);
		stage.addActor(table);

		// keep track of anyone whose fonts need to be resized properly. could use some cleaning up
		fontActors = Lists.<Actor> newArrayList(ipAddressField, portField, nicknameField, ipAddressLabel, portLabel, nicknameLabel, waitingText, announcementLabel, connectButton, updateButton, gameStart);
	}

	@Override
	protected void registerEvents() {
		registerEvent(CommonSocketIOEvents.INVALID_NICKNAME, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				new Dialog("Invalid nickname", skin).text("Please pick a different nickname").button("OK").show(stage);
				updateButton.setDisabled(false);
				updateButton.setVisible(true);
				nicknameField.setDisabled(false);
			}
		});
		registerEvent(CommonSocketIOEvents.ELECTED_CLIENT, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				getSocketIO().setHost(false);
				waitingText.setVisible(true);
			}

		});
		registerEvent(CommonSocketIOEvents.ELECTED_HOST, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				getSocketIO().setHost(true);
				gameStart.setVisible(true);
				gameStart.setDisabled(false);
			}

		});
		registerEvent(CommonSocketIOEvents.GAME_START, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				switchToGame();
			}

		});
		registerEvent(CommonSocketIOEvents.ANNOUNCEMENT, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				final String announcement = (String) args[0];
				announcementLabel.setText(announcement);
			}
		});
	}

	private void buildBackground(Skin skin) {
		// Adds a background texture to the stage
		backgroundImage = new Image(new TextureRegion(new Texture(Gdx.files.classpath("backgrounds/swanBackground2.jpg"))));
		backgroundImage.setX(0);
		backgroundImage.setY(0);
		backgroundImage.setWidth(VIRTUAL_WIDTH);
		backgroundImage.setHeight(VIRTUAL_HEIGHT);
		backgroundImage.setFillParent(true);
		stage.addActor(backgroundImage);
	}

	private void buildTable(final Skin skin) {
		table = new Table(skin);
		table.defaults().align(Align.left);
		table.add(ipAddressLabel).padRight(LABEL_FIELD_PADDING);
		table.add(ipAddressField).prefWidth(FIELD_WIDTH);
		table.row();

		table.add(portLabel).padRight(LABEL_FIELD_PADDING);
		table.add(portField).prefWidth(FIELD_WIDTH);
		table.row();

		table.add(nicknameLabel).padRight(LABEL_FIELD_PADDING);
		table.add(nicknameField).prefWidth(FIELD_WIDTH);
		table.row();

		table.add(connectButton);
		table.add(gameStart);
		table.row();
		table.add(updateButton).colspan(2);
		table.row();
		table.add(waitingText).colspan(2);
		table.row();
		table.add(announcementLabel).colspan(2);
		table.center();

		table.setFillParent(true);
	}

	public void connect() {
		final String address = SwanUtil.toAddress(ipAddressField.getText(), portField.getText());
		try {
			getSocketIO().connect(address, nicknameField.getText(), false, new ConnectCallback() {

				@Override
				public void onConnect(SocketIOException ex) {
					if (ex != null) {
						connectButton.setDisabled(false);
					} else {
						connectButton.setVisible(false);
						ipAddressField.setDisabled(true);
						portField.setDisabled(true);
						nicknameField.setDisabled(true);
					}
				}

				@Override
				public void onDisconnect() {
					connectButton.setText("Connect");
					connectButton.setDisabled(false);
					connectButton.setVisible(true);
					ipAddressField.setDisabled(false);
					portField.setDisabled(false);
				}
			});
		} catch (MalformedURLException e) {
			Gdx.app.error(CommonLogTags.SOCKET_IO, "Malformed server address " + address);
		}
	}

	@Override
	public void render(float delta) {
		super.render(delta);
		stage.draw();
		stage.act(delta);
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
		SwanUtil.resizeFonts(fontActors, fontGenerator, defaultFontSize, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
	}

	@Override
	public void show() {
		super.show();
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void hide() {
		announcementLabel.setText("");
		super.hide();
	}

	@Override
	public void dispose() {
		stage.dispose();
		fontGenerator.dispose();
	}

	protected abstract void switchToGame();

}
