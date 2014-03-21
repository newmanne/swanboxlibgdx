package com.swandev.swanlib.screen;

import io.socket.IOAcknowledge;
import io.socket.SocketIOException;

import java.net.MalformedURLException;
import java.util.List;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.google.common.collect.Lists;
import com.swandev.swanlib.socket.CommonSocketIOEvents;
import com.swandev.swanlib.socket.ConnectCallback;
import com.swandev.swanlib.socket.EventCallback;
import com.swandev.swanlib.socket.EventEmitter;
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
	private Table table;
	private final Label waitingText;
	private final List<Label> announcements = Lists.newArrayList();

	public ClientConnectScreen(final Game game, final SocketIOState socketIO, final SpriteBatch spritebatch, final Skin skin) {
		super(socketIO);
		this.game = game;
		this.skin = skin;
		this.stage = new Stage();

		final String defaultIP = Gdx.app.getType() == ApplicationType.Desktop ? "localhost" : "192.168.0.100";
		ipAddressField = new TextField(defaultIP, skin);
		ipAddressField.setMessageText("IP Address");

		portField = new TextField("8080", skin);
		portField.setMessageText("Port");

		nicknameField = new TextField("blinky", skin);
		nicknameField.setMessageText("Blinky");

		connectButton = new TextButton("Connect", skin);
		connectButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				connectButton.setDisabled(true);
				connect();
			}
		});

		gameStart = new TextButton("Start", skin);
		gameStart.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				socketIO.swanBroadcast(CommonSocketIOEvents.GAME_START);
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
		getSocketIO().on(CommonSocketIOEvents.ELECTED_CLIENT, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				getSocketIO().setHost(false);
				waitingText.setVisible(true);
			}

		});
		getSocketIO().on(CommonSocketIOEvents.ELECTED_HOST, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				getSocketIO().setHost(true);
				gameStart.setVisible(true);
				gameStart.setDisabled(false);
			}

		});
		getSocketIO().on(CommonSocketIOEvents.GAME_START, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				switchToGame();
			}

		});
		getSocketIO().on(CommonSocketIOEvents.ANNOUNCEMENT, new EventCallback() {

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
					}
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
	protected void unregisterEvents(EventEmitter eventEmitter) {
		eventEmitter.unregisterEvent(CommonSocketIOEvents.ELECTED_CLIENT);
		eventEmitter.unregisterEvent(CommonSocketIOEvents.ELECTED_HOST);
		eventEmitter.unregisterEvent(CommonSocketIOEvents.GAME_START);
		eventEmitter.unregisterEvent(CommonSocketIOEvents.ANNOUNCEMENT);
	}

	@Override
	public void dispose() {
		stage.dispose();
	}

	protected abstract void switchToGame();

}
