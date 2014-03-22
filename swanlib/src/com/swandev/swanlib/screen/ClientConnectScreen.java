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
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
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
	private final List<Label> announcements = Lists.newArrayList();

	private final float VIRTUAL_WIDTH = 800;
	private final float VIRTUAL_HEIGHT = 600;

	public ClientConnectScreen(final Game game, final SocketIOState socketIO, final SpriteBatch spritebatch) {
		super(socketIO);
		this.game = game;
		this.skin = new Skin(Gdx.files.classpath("skins/uiskin.json"));
		this.stage = new Stage(new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT), spritebatch);

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
				connectButton.setVisible(true);
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

		final Label ipAddressLabel = new Label("IP Address", skin);
		final Label portLabel = new Label("Port", skin);
		final Label nicknameLabel = new Label("Nickname", skin);

		waitingText = new Label("Waiting for host to select the game", skin);
		waitingText.setVisible(false);

		buildTable(skin, ipAddressLabel, portLabel, nicknameLabel, waitingText);
		stage.addActor(table);
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
				Label label = new Label(announcement, skin);
				announcements.add(label);
				table.add(label);
				table.row();
			}

		});
	}

	private void buildTable(final Skin skin, final Label ipAddressLabel, final Label portLabel, final Label nicknameLabel, final Label waitingText) {
		table = new Table(skin);

		table.add(ipAddressLabel);
		table.add(ipAddressField);
		table.row();

		table.add(portLabel);
		table.add(portField);
		table.row();

		table.add(nicknameLabel);
		table.add(nicknameField);
		table.row();

		table.add(connectButton);
		table.add(updateButton);
		table.add(gameStart);
		table.row();

		table.add(waitingText);

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
	}

	@Override
	public void show() {
		super.show();
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void hide() {
		super.hide();
		for (Label announcement : announcements) {
			announcement.remove();
		}
		announcements.clear();
	}

	@Override
	public void dispose() {
		stage.dispose();
	}

	protected abstract void switchToGame();

}
