package com.swandev.poker;

import io.socket.IOAcknowledge;

import java.util.List;
import java.util.Map;

import lombok.Getter;

import org.apache.commons.lang3.mutable.MutableInt;
import org.json.JSONArray;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.swandev.poker.HandScreen.HandRenderer.CardImage;
import com.swandev.swanlib.screen.SwanGameStartScreen;
import com.swandev.swanlib.socket.EventCallback;
import com.swandev.swanlib.util.SwanUtil;

public class HandScreen extends SwanGameStartScreen {
	// *** Layout Coordinates ***//
	private static final float COORD_SCALE = 50f;
	// Define a grid and orient all the elements of the screen based on that grid.
	private static final float CAMERA_WIDTH = 15f * COORD_SCALE; // how many boxes wide the screen is
	private static final float CAMERA_HEIGHT = 10f * COORD_SCALE; // how many boxes high the screen is

	// orient the cards in the players hand
	private static final float CARD_WIDTH = 4f * COORD_SCALE;
	private static final float CARD_HEIGHT = 6f * COORD_SCALE;
	private static final float CARD1_ORIGIN_X = 5f * COORD_SCALE;
	private static final float CARD1_ORIGIN_Y = 2.5f * COORD_SCALE;
	private static final float CARD2_ORIGIN_X = 9f * COORD_SCALE;
	private static final float CARD2_ORIGIN_Y = 2.5f * COORD_SCALE;

	// orient the image for hand complete
	private static final float OVER_IMAGE_HEIGHT = 3f * COORD_SCALE;
	private static final float OVER_IMAGE_WIDTH = 8f * COORD_SCALE;
	private static final float OVER_IMAGE_X = 5f * COORD_SCALE;
	private static final float OVER_IMAGE_Y = 4.5f * COORD_SCALE;

	// orient the table of buttons for betting/folding
	private static final float BUTTON_WIDTH = 3f * COORD_SCALE;
	private static final float BUTTON_HEIGHT = 1f * COORD_SCALE;
	private static final float BUTTON_PADDING_LEFT = 1f * COORD_SCALE;
	private static final float BUTTON_PADDING_TOP = 1.5f * COORD_SCALE;

	// orient the text boxes which show the amount of $$ owned and bet
	private static final float MONEY_TEXT_WIDTH = 2f * COORD_SCALE;
	private static final float MONEY_TEXT_HEIGHT = 1f * COORD_SCALE;
	private static final float MONEY_TABLE_PADDING_RIGHT = 0.5f * COORD_SCALE;
	private static final float MONEY_TABLE_PADDING_BOTTOM = 1f * COORD_SCALE;

	// size the buttons for the raise dialog
	private static final float RAISE_TEXT_HEIGHT = 2f * COORD_SCALE; // width should adjust with the text
	private static final float RAISE_BUTTON_WIDTH = 2f * COORD_SCALE; // height is the same as width

	// size of label for the username
	private static final float USERNAME_HEIGHT = 1f * COORD_SCALE;
	private static final float USERNAME_WIDTH = 5f * COORD_SCALE;

	private static final int DEFAULT_FONT_SIZE = 25;

	// These labels are members so we can dynamically change their values
	// without looking them up in the stage
	private Label cashLabel;
	private Label betLabel;
	private Label callLabel;

	private Image handOver;

	// The hand is a member so we can dynamically change the values and the
	// orientation (face-up v. face-down) of the cards
	private HandRenderer myHand;

	// The action buttons (Raise, Check, Call, Fold, All-In) are members so
	// we can dynamically disable/enable them based on the current chip/call/bet values
	private TextButton raiseButton;
	private TextButton allInButton;
	private TextButton callButton;
	private TextButton checkButton;
	private TextButton foldButton;

	private final PokerGameClient game;

	private final Stage stage;
	private final PlayerState state;
	private Image backgroundImage;

	/** Textures **/
	private final Map<Integer, TextureRegion> cardTextureMap;

	private int width;
	private int height;

	public HandScreen(PokerGameClient game) {
		super(game.getSocketIO());
		this.game = game;
		cardTextureMap = PokerLib.getCardTextures();

		Gdx.app.log("SIZE_DEBUG", "Constructing to " + width + "x" + height);
		stage = new Stage(new StretchViewport(CAMERA_WIDTH, CAMERA_HEIGHT));
		state = new PlayerState();
	}

	@Override
	protected void registerEvents() {
		registerEvent(PokerLib.DEAL_HAND, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				state.clearHand();
				handOver.setVisible(false);
				List<Integer> hand = SwanUtil.parseJsonList((JSONArray) args[0]);
				state.betValue = (Integer) args[1];
				state.chipValue = (Integer) args[2];
				state.callValue = (Integer) args[3];
				state.totalBetValue = (Integer) args[4];
				state.receiveCard(hand.get(0));
				state.receiveCard(hand.get(1));
				betLabel.setText(Integer.toString(state.totalBetValue));
				cashLabel.setText(Integer.toString(state.chipValue));
				callLabel.setText(Integer.toString(state.callValue));
				myHand.setCardVisibility(true);
			}
		});
		registerEvent(PokerLib.YOUR_TURN, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				state.betValue = (Integer) args[0];
				state.chipValue = (Integer) args[1];
				state.callValue = (Integer) args[2];
				callLabel.setText(Integer.toString(state.callValue));
				enableLegalActionButtons();
			}
		});
		registerEvent(PokerLib.ACTION_ACKNOWLEDGE, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				state.betValue = (Integer) args[0];
				state.chipValue = (Integer) args[1];
				state.callValue = (Integer) args[2];
				state.totalBetValue = (Integer) args[3];
				betLabel.setText(Integer.toString(state.totalBetValue));
				cashLabel.setText(Integer.toString(state.chipValue));
				callLabel.setText(Integer.toString(state.callValue));
				disableActionButtons();
			}
		});
		registerEvent(PokerLib.HAND_COMPLETE, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				Gdx.app.log("poker", "Saw a hand complete message!");

				state.betValue = (Integer) args[0];
				state.chipValue = (Integer) args[1];
				state.callValue = (Integer) args[2];
				state.totalBetValue = (Integer) args[3];
				state.clearHand();
				Boolean you_won = (Boolean) args[4];
				String imgPath = you_won ? "images/you_win_banner.png" : "images/you_lose_xs.png";
				handOver.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal(imgPath)))));
				handOver.setVisible(true);

				betLabel.setText(Integer.toString(state.totalBetValue));
				cashLabel.setText(Integer.toString(state.chipValue));
				callLabel.setText(Integer.toString(state.callValue));
				disableActionButtons();

				myHand.setCardVisibility(false);
			}
		});
		registerEvent(PokerLib.GAMEOVER, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				game.setScreen(game.getConnectScreen());
			}
		});
	}

	private void buildBackground(Skin skin) {
		// Adds a background texture to the stage
		backgroundImage = new Image(new TextureRegion(new Texture(Gdx.files.internal("images/background.png"))));
		backgroundImage.setBounds(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		backgroundImage.setFillParent(true);
		stage.addActor(backgroundImage);
	}

	private void buildCards(Skin skin) {
		// The hand renderer takes care of the actual images that the cards display; but
		// here we position the cards and add them to the renderer.

		// Note: If you want to overlap the cards because you're cool like that, just change the
		// origins such that they overlap and the one added second (card2) is "in front" of the first one.
		CardImage card1Image = myHand.getCard1();
		card1Image.setBounds(CARD1_ORIGIN_X, CARD1_ORIGIN_Y, CARD_WIDTH, CARD_HEIGHT);
		stage.addActor(card1Image);

		CardImage card2Image = myHand.getCard2();
		card2Image.setBounds(CARD2_ORIGIN_X, CARD2_ORIGIN_Y, CARD_WIDTH, CARD_HEIGHT);
		stage.addActor(card2Image);
	}

	private void buildMoneyText(Skin skin) {
		// These labels appear at the bottom of the screen in the form of:
		// Call: XXXXX Cash: XXXXX Bet: XXXX
		// The second of each pair is a member of the Screen and is updated along with the PlayerState.
		Table moneyTextTable = new Table(skin);

		moneyTextTable.defaults().width(MONEY_TEXT_WIDTH);
		moneyTextTable.defaults().height(MONEY_TEXT_HEIGHT);
		moneyTextTable.bottom().right();
		moneyTextTable.padRight(MONEY_TABLE_PADDING_RIGHT).padBottom(MONEY_TABLE_PADDING_BOTTOM);

		Label callText = new Label("Call:", skin);
		callLabel = new Label(new Integer(state.callValue).toString(), skin);
		moneyTextTable.add(callText);
		moneyTextTable.add(callLabel).padRight(MONEY_TABLE_PADDING_RIGHT);

		Label cashText = new Label("Cash:", skin);
		cashLabel = new Label(new Integer(state.chipValue).toString(), skin);
		moneyTextTable.add(cashText);
		moneyTextTable.add(cashLabel).padRight(MONEY_TABLE_PADDING_RIGHT);

		Label betText = new Label("Bet:", skin);
		betLabel = new Label(new Integer(state.betValue).toString(), skin);
		moneyTextTable.add(betText);
		moneyTextTable.add(betLabel);

		moneyTextTable.setFillParent(true);
		// moneyTextTable.debug();
		stage.addActor(moneyTextTable);
	}

	private void buildButtonTable(Skin skin) {
		// Adds a table of action buttons in the top-left corner of the screen
		Table buttonTable = new Table(skin);

		buttonTable.defaults().width(BUTTON_WIDTH);
		buttonTable.defaults().height(BUTTON_HEIGHT);
		buttonTable.top().left();
		buttonTable.padLeft(BUTTON_PADDING_LEFT).padTop(BUTTON_PADDING_TOP);

		/* All-In Button Requests a bet which is equal to the total cash the player owns */
		allInButton = new TextButton("All In!", skin);
		allInButton.setColor(Color.RED);
		allInButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				requestBet(state.chipValue);
			}
		});
		buttonTable.add(allInButton);
		buttonTable.row();

		/* Raise-1000 Button Requests a bet which 1000 more than the call value - the bet value */
		raiseButton = new TextButton("Raise", skin);
		raiseButton.setColor(Color.BLUE);
		raiseButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				getRaiseValueAndRequest();
			}
		});
		buttonTable.add(raiseButton);
		buttonTable.row();

		/* Call Button Requests a bet which makes up the difference between the current bet and the call value */
		callButton = new TextButton("Call", skin);
		callButton.setColor(Color.GREEN);
		callButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				requestBet(state.callValue - state.betValue);
			}
		});
		buttonTable.add(callButton);
		buttonTable.row();

		/* Check Button takes no action and passes priority to the next player */
		checkButton = new TextButton("Check", skin);
		checkButton.setColor(Color.YELLOW);
		checkButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				requestBet(PokerLib.BET_CHECK);
			}
		});
		buttonTable.add(checkButton);
		buttonTable.row();

		/* Fold Button gives up on the current bet and leaves this player out of this round */
		foldButton = new TextButton("Fold", skin);
		foldButton.setColor(Color.GRAY);
		foldButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				requestBet(PokerLib.BET_FOLD);
			}
		});
		buttonTable.add(foldButton);
		buttonTable.row();

		buttonTable.setFillParent(true);
		stage.addActor(buttonTable);
	}

	private void buildHandOver(Skin skin) {
		handOver = new Image();
		handOver.setVisible(false);

		handOver.setBounds(OVER_IMAGE_X, OVER_IMAGE_Y, OVER_IMAGE_WIDTH, OVER_IMAGE_HEIGHT);
		stage.addActor(handOver);
	}

	private void buildUserName(Skin skin) {
		Table nameTable = new Table(skin);

		Label userNameLabel = new Label("Name: " + game.getSocketIO().getNickname(), skin);
		userNameLabel.setAlignment(Align.center);

		nameTable.add(userNameLabel).width(USERNAME_WIDTH).height(USERNAME_HEIGHT);
		nameTable.row();

		nameTable.center().top();
		nameTable.setFillParent(true);
		nameTable.debug();
		stage.addActor(nameTable);
	}

	private void disableActionButtons() {
		// disables all the action buttons; should be called after a request is sent
		// and again once the acknowledge comes back
		raiseButton.setDisabled(true);
		allInButton.setDisabled(true);
		callButton.setDisabled(true);
		checkButton.setDisabled(true);
		foldButton.setDisabled(true);
	}

	private void enableLegalActionButtons() {
		// Based on the current PlayerState (call/chip/bet values), only some actions
		// are legal to process. Only enable those buttons which are legal!
		disableActionButtons(); // start by disabling all buttons

		// now re-enable only the legal ones
		foldButton.setDisabled(false); // you can always fold
		if ((state.betValue < state.callValue) && (state.chipValue >= (state.callValue - state.betValue))) {
			callButton.setDisabled(false); // you can only call if you haven't bet up to the call value
		} else if (state.betValue == state.callValue) {
			checkButton.setDisabled(false); // otherwise you will be able to check
		}
		if (state.callValue == 0) {
			checkButton.setDisabled(false);
		}

		if (state.chipValue > 0) {
			allInButton.setDisabled(false);
		}
		if (state.chipValue > state.callValue + 1000) {
			// all raises are exactly $1000 for now. Once that
			// changes, we should check for some minimum amount
			// a player is allowed to raise.
			raiseButton.setDisabled(false);
		}
	}

	private void requestBet(int betValue) {
		// send the request to the server
		if (betValue == -1) {
			getSocketIO().emitToScreen(PokerLib.FOLD_REQUEST, getSocketIO().getNickname());
			myHand.setCardVisibility(false);
		} else {
			getSocketIO().emitToScreen(PokerLib.BET_REQUEST, getSocketIO().getNickname(), betValue);
		}

		// disable the buttons while you wait for the ack; the valid ones will be re-enabled on a YOUR_TURN
		// method or in response to an INVALID_ACTION call (shouldn't happen if the buttons were enabled properly)
		disableActionButtons();
	}

	@Override
	public void doRender(float delta) {
		stage.draw();
		stage.act(delta);
	}

	public class HandRenderer {
		// Used to populate the cards with the images to be rendered.
		// Since your hand is displayed on a screen that is likely somewhat visible
		// to other players, we want to hide the cards unless you click and drag upwards
		// on them (just like the pros do when they keep their cards down and lift them in
		// private).
		// Positioning the cards and adding them to the stage is handled above.
		private float startY;

		@Getter
		private final CardImage card1;

		@Getter
		private final CardImage card2;

		private final PlayerState state;

		public HandRenderer(PlayerState s) {
			card1 = new CardImage();
			card2 = new CardImage();
			setCardDrawables(PokerLib.CARD_BACK, PokerLib.CARD_BACK);
			setCardVisibility(false);
			state = s;
			startY = -1f;
		}

		public void setCardDrawables(int card1Value, int card2Value) {
			card1.setDrawable(new TextureRegionDrawable(cardTextureMap.get(card1Value)));
			card2.setDrawable(new TextureRegionDrawable(cardTextureMap.get(card2Value)));
		}

		public void setCardVisibility(boolean visible) {
			card1.setVisible(visible);
			card2.setVisible(visible);
		}

		public class CardImage extends Image {
			public CardImage() {
				super();

				addListener(new DragListener() {
					@Override
					public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
						// When they first touch down on the card image, record the Y-coordinate
						startY = y;
						return true;
					}

					@Override
					public void touchDragged(InputEvent event, float x, float y, int pointer) {
						// If they drag along the card and the Y-coordinate increases, reveal the cards
						if (y > startY && startY > 0) {
							setCardDrawables(state.card1, state.card2);
						}
					}

					@Override
					public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
						// When they release the touch, turn the cards back down
						setCardDrawables(PokerLib.CARD_BACK, PokerLib.CARD_BACK);
						startY = -1f;
					}

				});
			}
		}
	}

	private void getRaiseValueAndRequest() {
		final MutableInt myRaise = new MutableInt(0);
		Skin skin = game.getAssets().getSkin();
		myRaise.setValue(PokerLib.ANTE);

		Dialog raiseDialog = new Dialog("Choose a Raise Value!", skin, "dialog") {
			@Override
			protected void result(Object result) {
				if (result.equals(true) && myRaise.getValue() > 0) {
					requestBet(state.callValue + myRaise.getValue());
				}
			}
		}.button("Cancel", false).button("Submit", true).key(Keys.ENTER, true).key(Keys.ESCAPE, false);

		final Label raiseValueLabel = new Label(myRaise.getValue().toString(), skin);
		raiseValueLabel.setAlignment(Align.center);

		// Make the Decrement Button
		final ImageButton decrementButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("images/decr_up.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("images/decr_down.png")))));
		decrementButton.setDisabled(cannotRaiseValue(myRaise.getValue() - PokerLib.ANTE));

		// Make the Increment Button
		final ImageButton incrementButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("images/incr_up.png")))), new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("images/incr_down.png")))));
		incrementButton.setDisabled(cannotRaiseValue(myRaise.getValue() + PokerLib.ANTE));

		// Now add the listeners, since they can enable/disable each other
		decrementButton.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				myRaise.setValue(myRaise.getValue() - PokerLib.ANTE);
				raiseValueLabel.setText(myRaise.getValue().toString());
				decrementButton.setDisabled(cannotRaiseValue(myRaise.getValue() - PokerLib.ANTE));
				incrementButton.setDisabled(cannotRaiseValue(myRaise.getValue() + PokerLib.ANTE));
			}

		});

		incrementButton.addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				myRaise.setValue(myRaise.getValue() + PokerLib.ANTE);
				raiseValueLabel.setText(myRaise.getValue().toString());
				decrementButton.setDisabled(cannotRaiseValue(myRaise.getValue() - PokerLib.ANTE));
				incrementButton.setDisabled(cannotRaiseValue(myRaise.getValue() + PokerLib.ANTE));
			}

		});

		// Add them to the Dialog table
		raiseDialog.getContentTable().add(decrementButton).height(RAISE_BUTTON_WIDTH).width(RAISE_BUTTON_WIDTH);
		raiseDialog.getContentTable().add(raiseValueLabel).height(RAISE_TEXT_HEIGHT).minWidth(RAISE_TEXT_HEIGHT);
		raiseDialog.getContentTable().add(incrementButton).height(RAISE_BUTTON_WIDTH).width(RAISE_BUTTON_WIDTH);

		raiseDialog.show(stage);
	}

	public boolean cannotRaiseValue(int raiseValue) {
		return (raiseValue < PokerLib.ANTE) || (state.chipValue < state.callValue + raiseValue);
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
		SwanUtil.resizeAllFonts(stage, game.getAssets().getFontGenerator(), DEFAULT_FONT_SIZE, CAMERA_WIDTH, CAMERA_HEIGHT);
	}

	@Override
	protected void onEveryoneReady() {
	}

	@Override
	public void doShow() {
		state.reset();
		myHand = new HandRenderer(state);

		final Skin skin = game.getAssets().getSkin();

		// Build the elements of the stage as seen on the client screen
		// Note: Order is important here! The order in which we add the elements
		// is the order in which they will be rendered; this only *really* matters
		// for the background since it needs to be behind everything else, but also
		// determines who's in front in some weird resizing cases.
		buildBackground(skin);
		buildUserName(skin);
		buildMoneyText(skin);
		buildButtonTable(skin);
		buildCards(skin);
		buildHandOver(skin);

		// This call should be made at the end of a response to a "Your Turn" message, after
		// changing the PlayerState appropriately.
		// enableLegalActionButtons(); move to the registered events
		disableActionButtons();
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void dispose() {
		stage.dispose();
	}

}
