package com.swandev.poker;

import io.socket.IOAcknowledge;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.swandev.swanlib.screen.SwanGameStartScreen;
import com.swandev.swanlib.socket.EventCallback;

public class PokerGameScreen extends SwanGameStartScreen {

	private static final long DELAY_BETWEEN_HANDS_IN_MS = TimeUnit.SECONDS.toMillis(10);

	// *** Layout Coordinates ***//
	private static final float COORD_SCALE = 50f;

	// Define a grid and orient all the elements of the screen based on that grid.
	public static final float CAMERA_WIDTH = 38f * COORD_SCALE;
	public static final float CAMERA_HEIGHT = 17f * COORD_SCALE;

	// orient the cards on the table
	public static final float TABLE_CARD_WIDTH = 4f * COORD_SCALE;
	public static final float TABLE_CARD_HEIGHT = 6f * COORD_SCALE;
	public static final float TABLE_CARDS_ORIGIN_X = 8f * COORD_SCALE;
	public static final float TABLE_CARDS_ORIGIN_Y = 8f * COORD_SCALE;
	public static final float TABLE_CARDS_HSPACING = 0.5f * COORD_SCALE;

	// orient the table graphic
	public static final float TABLE_IMAGE_WIDTH = 5 * TABLE_CARD_WIDTH + 6 * TABLE_CARDS_HSPACING;
	public static final float TABLE_IMAGE_HEIGHT = TABLE_CARD_HEIGHT + 2 * TABLE_CARDS_HSPACING;
	public static final float TABLE_IMAGE_ORIGIN_X = TABLE_CARDS_ORIGIN_X - TABLE_CARDS_HSPACING;
	public static final float TABLE_IMAGE_ORIGIN_Y = TABLE_CARDS_ORIGIN_Y - TABLE_CARDS_HSPACING;

	// orient the card images for each player's hand
	public static final float PLAYER_CARD_WIDTH = 2.5f * COORD_SCALE;
	public static final float PLAYER_CARD_HEIGHT = 4f * COORD_SCALE;
	public static final float PLAYER_CARD1_ORIGIN_X = 0.5f * COORD_SCALE;
	public static final float PLAYER_CARD2_ORIGIN_X = 2f * COORD_SCALE;

	// The player tables start padded from the bottom-left corner
	public static final float PLAYER_TABLES_PADDING_Y = 1f * COORD_SCALE;
	public static final float PLAYER_TABLES_PADDING_X = 1f * COORD_SCALE;

	// all rows in a player table are the same height
	public static final float PLAYER_TABLE_ROW_HEIGHT = 1f * COORD_SCALE;

	// override relevant dimensions for the player tables
	public static final float PLAYER_NAME_WIDTH = 4f * COORD_SCALE;
	public static final float PLAYER_MONEY_WIDTH = 2.5f * COORD_SCALE;
	public static final float PLAYER_ACTION_WIDTH = 1.5f * COORD_SCALE;
	public static final float PLAYER_TURN_WIDTH = 0.5f * COORD_SCALE;

	// orient the pot label at the top of the screen
	public static final float POT_LABEL_HEIGHT = 1f * COORD_SCALE;
	public static final float POT_LABEL_WIDTH = 2f * COORD_SCALE;
	public static final float POT_VALUE_LABEL_WIDTH = 5f * COORD_SCALE;

	private static final int STARTING_VALUE = 50000;

	private PokerTable pokerTable;

	final Map<String, PlayerStats> playerMap = Maps.newHashMap();

	final PokerGameServer game;

	Map<Integer, TextureRegion> cardToImage = Maps.newHashMap();
	List<String> playerNames;

	private final Stage stage;
	private final Image[] tableCards = new Image[5];
	private final Map<String, PlayerTable> nameToTableMap = Maps.newHashMap();
	private Label potValueLabel;

	private Image backgroundImage;

	enum PokerRound {
		PREFLOP, FLOP, TURN, RIVER
	}

	@Override
	protected void registerEvents() {
		registerEvent(PokerLib.FOLD_REQUEST, new EventCallback() {
			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				final String player = (String) args[0];
				pokerTable.foldPlayer(playerMap.get(player));
			}
		});
		registerEvent(PokerLib.BET_REQUEST, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				final String playerName = (String) args[0];
				final Integer amount = (Integer) args[1];
				final PlayerStats player = playerMap.get(playerName);
				pokerTable.betPlayer(player, amount);
				// UI
				nameToTableMap.get(playerName).setChipValue(player.getMoney());
				setPotValue(pokerTable.getPot().getValue());
			}
		});
	}

	public void uiForDrawCards(PokerRound round) {
		int i;
		for (i = 0; i < round.ordinal() + 2; i++) {
			tableCards[i].setDrawable(new TextureRegionDrawable(cardToImage.get(pokerTable.getTableCards().get(i).getImageNumber())));
			tableCards[i].setVisible(true);
		}
		for (; i < tableCards.length; i++) {
			tableCards[i].setVisible(false);
		}
	}

	public void uiForPreFlop() {
		for (PlayerStats player : playerMap.values()) {
			PlayerTable playerTable = nameToTableMap.get(player.getName());
			playerTable.setChipValue(player.getMoney()); // update the money label
			playerTable.setCardImages(PokerLib.CARD_BACK, PokerLib.CARD_BACK); // re-initialize the cards to face-down
			playerTable.setCardsVisible(true);
		}
		for (Image card : tableCards) {
			card.setDrawable(new TextureRegionDrawable(cardToImage.get(PokerLib.CARD_BACK)));
			card.setVisible(false);
		}
	}

	public PokerGameScreen(PokerGameServer game) {
		super(game.getSocketIO());
		this.game = game;

		stage = new Stage(new StretchViewport(CAMERA_WIDTH, CAMERA_HEIGHT));
		cardToImage = PokerLib.getCardTextures();
	}

	public void setPlayerTurn(String playerName, boolean isCurrentTurn) {
		nameToTableMap.get(playerName).setCurrentTurn(isCurrentTurn);
	}

	public void setPlayerAction(String playerName, String action) {
		nameToTableMap.get(playerName).setLastAction(action);
	}

	public void clearPlayerAction(String playerName) {
		setPlayerAction(playerName, "");
	}

	public void clearPlayerActions() {
		for (PlayerTable table : nameToTableMap.values()) {
			table.clearAction();
		}
	}

	public void clearPlayerCards(String playerName) {
		nameToTableMap.get(playerName).setCardsVisible(false);
	}

	@Override
	protected void doRender(float delta) {
		stage.draw();
		stage.act(delta);
		// Table.drawDebug(stage);
	}

	@Override
	public void resize(int w, int h) {
		Gdx.app.log("SIZE_DEBUG", "Resizing to " + w + "x" + h);
		stage.getViewport().update(w, h, true);
	}

	@Override
	protected void doShow() {
		playerNames = Lists.newArrayList(getSocketIO().getNicknames());
		if (playerNames.size() > 8 || playerNames.size() < 2) {
			throw new IllegalStateException("You can only play poker with between 2 and 8 players");
		}
		List<PlayerStats> players = Lists.newArrayList();
		for (String playerName : playerNames) {
			PlayerStats playerStats = new PlayerStats(playerName, STARTING_VALUE);
			playerMap.put(playerName, playerStats);
			players.add(playerStats);
		}

		final Skin skin = game.getAssets().getSkin();
		buildBackground(skin);
		buildPotLabel(skin);
		buildCards();
		buildPlayerTables(skin);

		pokerTable = new PokerTable(this, players);
	}

	@Override
	protected void onEveryoneReady() {
		pokerTable.newHand();
		setPotValue(pokerTable.pot.getValue());
	}

	private void buildCards() {
		for (int i = 0; i < 5; ++i) {
			tableCards[i] = new Image();
			tableCards[i].setWidth(TABLE_CARD_WIDTH);
			tableCards[i].setX(TABLE_CARDS_ORIGIN_X + i * (TABLE_CARD_WIDTH + TABLE_CARDS_HSPACING));
			tableCards[i].setHeight(TABLE_CARD_HEIGHT);
			tableCards[i].setY(TABLE_CARDS_ORIGIN_Y);
			stage.addActor(tableCards[i]);
		}
	}

	private PlayerTable genEmptyPlayerTable(Skin skin) {
		// Used to keep proper sizing for all player tables
		PlayerTable empty = new PlayerTable(skin, "", null, null);
		empty.setCardsVisible(false);
		empty.setCurrentTurn(false);
		return empty;
	}

	private void buildPlayerTables(Skin skin) {
		Table playerTables = new Table(skin);

		// construct the top row of the table: P1| | | | | P8
		if (playerNames.size() >= 1) {
			String playerName = playerNames.get(0);
			PlayerTable newTable = new PlayerTable(skin, playerName, playerMap.get(playerName).getMoney(), cardToImage);
			nameToTableMap.put(playerName, newTable);
			playerTables.add(newTable);
		}
		for (int i = 0; i < 4; ++i) {
			playerTables.add();
		}
		if (playerNames.size() >= 8) {
			String playerName = playerNames.get(7);
			PlayerTable newTable = new PlayerTable(skin, playerName, playerMap.get(playerName).getMoney(), cardToImage);
			nameToTableMap.put(playerName, newTable);
			playerTables.add(newTable);
		} else {
			playerTables.add(genEmptyPlayerTable(skin));
		}
		playerTables.row();

		// construct the bottom row of the table: P2|P3|P4|P5|P6|P7
		for (int i = 1; i < Math.min(playerNames.size(), 7); ++i) {
			String playerName = playerNames.get(i);
			PlayerTable newTable = new PlayerTable(skin, playerName, playerMap.get(playerName).getMoney(), cardToImage);
			nameToTableMap.put(playerName, newTable);
			playerTables.add(newTable);
		}
		if (playerNames.size() < 7) {
			for (int i = playerNames.size(); i < 7; ++i) {
				playerTables.add(genEmptyPlayerTable(skin));
			}
		}

		playerTables.center().bottom();
		playerTables.padBottom(PLAYER_TABLES_PADDING_Y);
		playerTables.setFillParent(true);
		// playerTables.debug();
		stage.addActor(playerTables);
	}

	private void buildBackground(Skin skin) {
		// Adds a background texture to the stage
		backgroundImage = new Image(new TextureRegion(new Texture(Gdx.files.internal("images/background.png"))));
		backgroundImage.setX(0);
		backgroundImage.setY(0);
		backgroundImage.setWidth(CAMERA_WIDTH);
		backgroundImage.setHeight(CAMERA_HEIGHT);
		backgroundImage.setFillParent(true);
		stage.addActor(backgroundImage);

		Image tableImage = new Image(new TextureRegion(new Texture(Gdx.files.internal("images/table.png"))));
		tableImage.setX(TABLE_IMAGE_ORIGIN_X);
		tableImage.setY(TABLE_IMAGE_ORIGIN_Y);
		tableImage.setWidth(TABLE_IMAGE_WIDTH);
		tableImage.setHeight(TABLE_IMAGE_HEIGHT);
		stage.addActor(tableImage);
	}

	private void buildPotLabel(Skin skin) {
		Table potTable = new Table(skin);

		Label potTextLabel = new Label("Pot:", skin);
		potValueLabel = new Label("0", skin);

		potTable.add(potTextLabel).height(POT_LABEL_HEIGHT).width(POT_LABEL_WIDTH);
		potTable.add(potValueLabel).height(POT_LABEL_HEIGHT).width(POT_VALUE_LABEL_WIDTH);
		potTable.row();
		potTable.center().top();
		potTable.setFillParent(true);

		stage.addActor(potTable);
	}

	public void setPotValue(Integer value) {
		potValueLabel.setText(value.toString());
	}

	public void uiBetweenHands() {
		for (PlayerStats player : playerMap.values()) {
			PlayerTable playerTable = nameToTableMap.get(player.getName());
			playerTable.setChipValue(player.getMoney()); // update the money label
			if (player.getPrivateCards().size() == 2 && !player.isFolded()) {
				playerTable.setCardImages(player.getPrivateCards().get(0).getImageNumber(), player.getPrivateCards().get(1).getImageNumber()); // show their cards
			}
		}

		setPotValue(0); // reset the pot

		Timer timer = new Timer("delayBetweenRounds");
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				pokerTable.newHand();
				setPotValue(pokerTable.getPot().getValue());

			}
		}, DELAY_BETWEEN_HANDS_IN_MS);
	}

	@Override
	public void dispose() {
		stage.dispose();
	}

}
