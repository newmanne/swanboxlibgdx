package com.swandev.poker;

import io.socket.IOAcknowledge;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.swandev.swanlib.screen.SwanScreen;
import com.swandev.swanlib.socket.EventCallback;
import com.swandev.swanlib.socket.EventEmitter;

public class PokerGameScreen extends SwanScreen {

	private static final int STARTING_VALUE = 100000;

	private PokerTable pokerTable;

	final Map<String, PlayerStats> playerMap = Maps.newHashMap();

	enum PokerRound {
		PREFLOP, FLOP, TURN, RIVER
	}

	@Override
	protected void registerEvents() {
		getSocketIO().on(PokerLib.FOLD_REQUEST, new EventCallback() {
			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				final String player = (String) args[0];
				pokerTable.foldPlayer(playerMap.get(player));
			}
		});

		getSocketIO().on(PokerLib.BET_REQUEST, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				final String playerName = (String) args[0];
				final Integer amount = (Integer) args[1];
				final PlayerStats player = playerMap.get(playerName);
				pokerTable.betPlayer(player, amount);
				// UI
				tables.get(playerName).setChipValue(player.getMoney());
			}
		});

	}

	public void uiForDrawCards(PokerRound round) {
		for (int i = 0; i < round.ordinal() + 2; i++) {
			cards[i].setDrawable(new TextureRegionDrawable(cardToImage.get(pokerTable.getTableCards().get(i).getImageNumber())));
		}
	}

	public void uiForPreFlop() {
		// re-initialize the cards to face-down
		for (PlayerStats player : playerMap.values()) {
			tables.get(player.getName()).setChipValue(player.getMoney());
		}
		for (Image card : cards) {
			card.setDrawable(new TextureRegionDrawable(cardToImage.get(CARD_BACK)));
		}
	}

	// UI
	public static final float CAMERA_WIDTH = 21f;
	public static final float CAMERA_HEIGHT = 13f;

	public static final float LABEL_WIDTH = 2f;
	public static final float LABEL_HEIGHT = 1f;

	public static final float PLAYER_NAME_WIDTH = 4f;

	public static final float CARD_WIDTH = 3f;
	public static final float CARD_HEIGHT = 4f;

	public static final float PLAYER_TABLE_PADDING_X = 1f;
	public static final float PLAYER_TABLE_PADDING_Y = 1f;

	public static final float CARD_PADDING_X = 1f;
	public static final float CARD_PADDING_Y = 2f;

	private static final int CARD_BACK = -1;

	final PokerGameServer game;
	final OrthographicCamera camera;

	Map<Integer, TextureRegion> cardToImage = Maps.newHashMap();
	List<String> playerNames;
	float xMid;
	float yMid;

	private final Stage stage;
	private final Image[] cards = new Image[5];
	private final Map<String, PlayerTable> tables = Maps.newHashMap();
	private final int width;
	private final int height;
	private final float ppuX;
	private final float ppuY;
	private Image backgroundImage;

	public PokerGameScreen(PokerGameServer game) {
		super(game.getSocketIO());
		this.game = game;
		camera = new OrthographicCamera();
		camera.setToOrtho(false);
		width = Gdx.graphics.getWidth();
		height = Gdx.graphics.getHeight();

		ppuX = width / CAMERA_WIDTH;
		ppuY = height / CAMERA_HEIGHT;

		stage = new Stage(width, height, false, game.getSpriteBatch());

		cardToImage = PokerLib.getCardTextures();

		xMid = width / 2;
		yMid = height / 2;
	}

	@Override
	public void render(float delta) {
		super.render(delta);

		stage.draw();
		stage.act(delta);
	}

	@Override
	protected void unregisterEvents(EventEmitter eventEmitter) {
		eventEmitter.unregisterEvent(PokerLib.FOLD_REQUEST);
		eventEmitter.unregisterEvent(PokerLib.BET_REQUEST);
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void show() {
		super.show();
		playerNames = Lists.newArrayList(getSocketIO().getNicknames());
		List<PlayerStats> players = Lists.newArrayList();
		for (String playerName : playerNames) {
			PlayerStats playerStats = new PlayerStats(playerName, STARTING_VALUE);
			playerMap.put(playerName, playerStats);
			players.add(playerStats);
		}

		final Skin skin = game.getAssets().getSkin();
		buildBackground(skin);
		buildCards();
		buildPlayerTables(skin);

		pokerTable = new PokerTable(this, players);
		pokerTable.newHand();
	}

	private void buildCards() {
		for (int i = 0; i < 5; ++i) {
			cards[i] = new Image();
			cards[i].setWidth(CARD_WIDTH * ppuX);
			cards[i].setX(CARD_PADDING_X * ppuX + i * (CARD_PADDING_X + CARD_WIDTH) * ppuX);
			cards[i].setHeight(CARD_HEIGHT * ppuY);
			cards[i].setY(Gdx.graphics.getHeight() - (CARD_HEIGHT + CARD_PADDING_Y) * ppuY);
			stage.addActor(cards[i]);
		}
	}

	private void buildPlayerTables(Skin skin) {
		for (int i = 0; i < playerNames.size(); ++i) {
			String playerName = playerNames.get(i);
			PlayerTable newTable = new PlayerTable(skin, playerName, playerMap.get(playerName).getMoney());
			newTable.bottom().left();
			newTable.padLeft((PLAYER_TABLE_PADDING_X + (i % 4) * (PLAYER_NAME_WIDTH + PLAYER_TABLE_PADDING_X)) * ppuX);
			float yCoord = PLAYER_TABLE_PADDING_Y * ppuY;
			if (i > 3) {
				yCoord += (LABEL_HEIGHT * 2 + PLAYER_TABLE_PADDING_Y) * ppuY;
			}
			newTable.padBottom(yCoord);
			newTable.setFillParent(true);
			stage.addActor(newTable);
			tables.put(playerName, newTable);
		}
	}

	private void buildBackground(Skin skin) {
		// Adds a background texture to the stage
		backgroundImage = new Image(new TextureRegion(new Texture(Gdx.files.internal("images/background.png"))));
		backgroundImage.setX(0);
		backgroundImage.setY(0);
		backgroundImage.setWidth(width);
		backgroundImage.setHeight(height);
		backgroundImage.setFillParent(true);
		stage.addActor(backgroundImage);
	}

	private class PlayerTable extends Table {
		private final Label nameLabel;
		private final Label chipLabel;
		private final Label chipValueLabel;

		public PlayerTable(Skin skin, String name, Integer chipValue) {
			nameLabel = new Label(name, skin);
			chipLabel = new Label("Chips:", skin);
			chipValueLabel = new Label(chipValue.toString(), skin);

			defaults().width(LABEL_WIDTH * ppuX);
			defaults().height(LABEL_HEIGHT * ppuY);
			add(nameLabel).colspan(2).width(PLAYER_NAME_WIDTH);
			row();
			add(chipLabel);
			add(chipValueLabel);
			row();
		}

		public void setChipValue(Integer value) {
			chipValueLabel.setText(value.toString());
		}
	}

	public void uiBetweenHands() {
		for (PlayerStats player : playerMap.values()) {
			tables.get(player.getName()).setChipValue(player.getMoney()); // update the label
		}
		Timer timer = new Timer("delayBetweenRounds");
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				pokerTable.newHand();

			}
		}, 5000);
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
		// TODO Auto-generated method stub

	}

}
