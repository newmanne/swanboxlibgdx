package com.swandev.swangame;

import io.socket.IOAcknowledge;
import io.socket.SocketIOException;

import java.net.MalformedURLException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class ConnectScreen implements Screen {

	private final PatternClientGame game;
	private final Stage stage;
	private TextField ipAddressField;
	private TextField portField;
	private TextField nicknameField;
	private TextButton connectButton;
	private TextButton gameStart;
	private Table table;

	public ConnectScreen(final PatternClientGame game) {
		this.game = game;
		this.stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false, game.getSpriteBatch());

		final Skin skin = game.getAssets().getSkin();
		final SocketIOState socketIO = game.getSocketIO();

		final String defaultIP = SwanUtil.isDebug() ? "localhost" : "";
		ipAddressField = new TextField(defaultIP, skin);
		ipAddressField.setMessageText("IP Address");

		portField = new TextField("8080", skin);
		portField.setMessageText("Port");

		nicknameField = new TextField("blinky", skin);
		nicknameField.setMessageText("Blinky");

		connectButton = new TextButton("Connect", skin);
		connectButton.addListener(new ChangeListener() {
			public void changed(ChangeEvent event, Actor actor) {
				connectButton.setDisabled(true);
				connect();
			}
		});

		gameStart = new TextButton("Start", skin);
		gameStart.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				socketIO.getClient().emit(SocketIOEvents.START_PATTERNS);
				game.setScreen(new PatternClientScreen(game));
			}

		});
		gameStart.setVisible(false);
		gameStart.setDisabled(true);

		final Label ipAddressLabel = new Label("IP Address", skin);
		final Label portLabel = new Label("Port", skin);
		final Label nicknameLabel = new Label("Nickname", skin);

		final Label waitingText = new Label("Waiting for host to select the game", skin);
		waitingText.setVisible(false);

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
		stage.addActor(table);

		socketIO.on(SocketIOEvents.ELECTED_CLIENT, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				socketIO.setHost(false);
				waitingText.setVisible(true);
			}

		});
		socketIO.on(SocketIOEvents.ELECTED_HOST, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				socketIO.setHost(true);
				gameStart.setVisible(true);
				gameStart.setDisabled(false);
			}

		});
		socketIO.on(SocketIOEvents.PLAYING_PATTERNS, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				game.setScreen(new PatternClientScreen(game));
			}

		});
		socketIO.on(SocketIOEvents.ANNOUNCEMENT, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				final String announcement = (String) args[0];
				table.add(new Label(announcement, skin));
				table.row();
			}

		});

	}

	public void connect() {
		final SocketIOState socketIO = game.getSocketIO();
		final String address = SwanUtil.toAddress(ipAddressField.getText(), portField.getText());
		try {
			socketIO.connect(address, nicknameField.getText(), false, new ConnectCallback() {

				@Override
				public void onConnect(SocketIOException ex) {
					if (ex != null) {
						connectButton.setDisabled(false);
					} else { // success
					}
				}
			});
		} catch (MalformedURLException e) {
			Gdx.app.error(LogTags.SOCKET_IO, "Malformed server address " + address);
		}
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		stage.draw();
		stage.act(delta);
		game.getSocketIO().flushEvents();
	}

	@Override
	public void resize(int width, int height) {
		stage.setViewport(width, height, true);
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		stage.dispose();
	}

}
