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
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.google.common.collect.ImmutableList;
import com.swandev.swanlib.socket.CommonSocketIOEvents;
import com.swandev.swanlib.socket.ConnectCallback;
import com.swandev.swanlib.socket.EventCallback;
import com.swandev.swanlib.socket.SocketIOState;
import com.swandev.swanlib.util.CommonLogTags;
import com.swandev.swanlib.util.SwanUtil;

public abstract class ClientConnectScreen extends SwanScreen {

	private static final int LABEL_FIELD_PADDING = 20;
	private static final int FIELD_WIDTH = 400;
	private static final float defaultFontSize = 30;
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

	private final float VIRTUAL_WIDTH = 800;
	private final float VIRTUAL_HEIGHT = 600;
	private final FreeTypeFontGenerator fontGenerator;
	private final Label ipAddressLabel;
	private final Label portLabel;
	private final Label nicknameLabel;
	private final Label announcementLabel;

	public ClientConnectScreen(final Game game, final SocketIOState socketIO, final SpriteBatch spritebatch) {
		super(socketIO);
		this.game = game;
		this.skin = new Skin(Gdx.files.classpath("skins/uiskin.json"));
		fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("arial.ttf"));
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

		ipAddressLabel = new Label("IP Address", skin);
		portLabel = new Label("Port", skin);
		nicknameLabel = new Label("Nickname", skin);
		announcementLabel = new Label("", skin);

		waitingText = new Label("Waiting for host to select the game", skin);
		waitingText.setVisible(false);

		buildTable(skin);
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
				announcementLabel.setText(announcement);
			}
		});
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
		resizeFonts();
	}

	private void resizeFonts() {
		float wScale = 1.0f * Gdx.graphics.getWidth() / VIRTUAL_WIDTH;
		float hScale = 1.0f * Gdx.graphics.getHeight() / VIRTUAL_HEIGHT;
		if (wScale < 1) {
			wScale = 1;
		}
		if (hScale < 1) {
			hScale = 1;
		}
		BitmapFont generatedFont = generateFont((int) (defaultFontSize * Math.max(wScale, hScale)));
		generatedFont.setScale((float) (1.0 / wScale), (float) (1.0 / hScale));

		TextFieldStyle textFieldStyle = skin.get(TextFieldStyle.class);
		textFieldStyle.font = generatedFont;
		ipAddressField.setStyle(textFieldStyle);
		portField.setStyle(textFieldStyle);
		nicknameField.setStyle(textFieldStyle);
		ipAddressField.setText(ipAddressField.getText());
		portField.setText(portField.getText());
		nicknameField.setText(nicknameField.getText());

		LabelStyle labelStyle = skin.get(LabelStyle.class);
		labelStyle.font = generatedFont;
		ipAddressLabel.setStyle(labelStyle);
		portLabel.setStyle(labelStyle);
		nicknameLabel.setStyle(labelStyle);
		waitingText.setStyle(labelStyle);
		announcementLabel.setStyle(labelStyle);

		TextButtonStyle textButtonStyle = skin.get(TextButtonStyle.class);
		textButtonStyle.font = generatedFont;
		connectButton.setStyle(textButtonStyle);
		updateButton.setStyle(textButtonStyle);
		gameStart.setStyle(textButtonStyle);

		table.invalidateHierarchy();
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
	}

	public BitmapFont generateFont(int size) {
		FreeTypeFontParameter freeTypeFontParameter = new FreeTypeFontParameter();
		freeTypeFontParameter.size = size;
		freeTypeFontParameter.magFilter = TextureFilter.Linear;
		freeTypeFontParameter.minFilter = TextureFilter.Linear;
		BitmapFont generatedFont = fontGenerator.generateFont(freeTypeFontParameter);
		return generatedFont;
	}

	protected abstract void switchToGame();

}
