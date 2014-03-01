package com.swandev.pokergame;

import java.util.Map;

import lombok.Getter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.google.common.collect.Maps;
import com.swandev.pokergame.HandScreen.HandRenderer.CardImage;
import com.swandev.swangame.screen.SwanScreen;
import com.swandev.swangame.socket.EventEmitter;

public class HandScreen extends SwanScreen {
	//*** Layout Coordinates ***//
	//Use the pixels per unit to define a grid and orient
	//all the elements of the screen based on that grid.
	private static final float CAMERA_WIDTH = 15f;  //how many boxes wide the screen is
	private static final float CAMERA_HEIGHT = 10f; //how many boxes high the screen is
	private float ppuX; // pixels per unit on the X axis
	private float ppuY; // pixels per unit on the Y axis
	
	//orient the cards in the players hand
	private static final float CARD_WIDTH = 4f;
	private static final float CARD_HEIGHT = 6f;	
	private static final float CARD1_ORIGIN_X = 5f;
	private static final float CARD1_ORIGIN_Y = 3f;
	private static final float CARD2_ORIGIN_X = 9f;
	private static final float CARD2_ORIGIN_Y = 3f;
	
	//orient the table of buttons for betting/folding
	private static final float BUTTON_WIDTH = 3f;
	private static final float BUTTON_HEIGHT = 1f;
	private static final float BUTTON_PADDING_LEFT = 1f;
	
	//orient the text boxes which show the amount of $$ owned and bet
	private static final float MONEY_TEXT_WIDTH = 2f;
	private static final float MONEY_TEXT_HEIGHT = 1f;
	private static final float MONEY_TABLE_PADDING_RIGHT = 1f;
	private static final float MONEY_TABLE_PADDING_BOTTOM = 1f;
	
	private static Label cashLabel;
	private static Label betLabel;
	
	private HandRenderer myHand;
	
	private PokerGame game;
	
	private final Stage stage;
	private PlayerState state;
	
	/** Textures **/
	private Map<Integer, TextureRegion> cardTextureMap = Maps.newHashMap();
	
	/** Animations **/
	//private Animation rollLeftAnimation;
	//private Animation rollRightAnimation;

	private int width;
	private int height;	
	
	public void setSize(int w, int h){
		this.width = w;
		this.height = h;
		ppuX = (float)width / CAMERA_WIDTH;
		ppuY = (float)height / CAMERA_HEIGHT;
	}
	
	public HandScreen(PokerGame game){
		super(game.getSocketIO());
		this.game = game;
		this.stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false, game.getSpriteBatch());
		this.state = new PlayerState();
		ppuX = (float)Gdx.graphics.getWidth() / CAMERA_WIDTH;
		ppuY = (float)Gdx.graphics.getHeight() / CAMERA_HEIGHT;
		myHand = new HandRenderer();
		
		final Skin skin = game.getAssets().getSkin();
		buildButtonTable(skin);	
		buildMoneyText(skin);
		buildCards(skin);
		
		this.cardTextureMap = PokerLib.getCardTextures();
	}
	
	private void buildButtonTable(Skin skin){
		Table buttonTable = new Table(skin);

		buttonTable.defaults().width(BUTTON_WIDTH*ppuX);
		buttonTable.defaults().height(BUTTON_HEIGHT*ppuY);
		buttonTable.center().left();
		buttonTable.padLeft(BUTTON_PADDING_LEFT*ppuX);
		
		/*Next Card Button increments the card value of both cards*/
		TextButton nextCardButton = new TextButton("Next Card", skin);
		nextCardButton.addListener(new ChangeListener(){
			@Override
			public void changed(ChangeEvent event, Actor actor){
				if (state.card1 == PokerLib.CARD_BACK){
					state.card1 = 100;
					state.card2 = 100;
				}
				state.card1 += 1;
				state.card2 += 1;
				if (state.card1 % 100 > PokerLib.MAX_CARD){
					state.card1 -= PokerLib.MAX_CARD;
					state.card2 -= PokerLib.MAX_CARD;
				}
			}
		});
		buttonTable.add(nextCardButton);
		buttonTable.row();
		
		/*Next Suit Button cycles each card through the suits*/
		TextButton nextSuitButton = new TextButton("Next Suit", skin);
		nextSuitButton.addListener(new ChangeListener(){
			@Override
			public void changed(ChangeEvent event, Actor actor){
				if (state.card1 == PokerLib.CARD_BACK){
					state.card1 = 100;
					state.card2 = 100;
				}
				state.card1 += 100;
				state.card2 += 100;
				if (state.card1 - PokerLib.MAX_CARD > PokerLib.MAX_SUIT){
					state.card1 -= PokerLib.MAX_SUIT;
					state.card2 -= PokerLib.MAX_SUIT;
				}
			}
		});
		buttonTable.add(nextSuitButton);
		buttonTable.row();
		
		/* All-In Button Requests a bet which is equal to the total cash the player owns*/
		TextButton allInButton = new TextButton("All In!", skin);
		allInButton.addListener(new ChangeListener(){
			@Override
			public void changed(ChangeEvent event, Actor actor){
				requestBet(state.chipValue);
			}
		});
		buttonTable.add(allInButton);
		buttonTable.row();
		
		/* Raise-1000 Button Requests a bet which is equal to the total cash the player owns*/
		TextButton raiseButton = new TextButton("Raise $1000", skin);
		raiseButton.addListener(new ChangeListener(){
			@Override
			public void changed(ChangeEvent event, Actor actor){
				requestBet(state.callValue - state.betValue + 1000);
			}
		});
		buttonTable.add(raiseButton);
		buttonTable.row();
		
		/* Call Button Requests a bet which is equal to the total cash the player owns*/
		TextButton callButton = new TextButton("Call", skin);
		callButton.addListener(new ChangeListener(){
			@Override
			public void changed(ChangeEvent event, Actor actor){
				requestBet(state.callValue - state.betValue);
			}
		});
		buttonTable.add(callButton);
		buttonTable.row();
		
		buttonTable.setFillParent(true);

		stage.addActor(buttonTable);
	}
	
	private void requestBet(int betValue){
		//Request a bet be made of the specified value
		if (betValue > state.chipValue){
			return;
		}
		
		//TODO: When the socket stuff is integrated, this method should do the error checking above
		//and then simply send the BET_REQUEST packet to the server. We change the PlayerState in
		//response to the Acknowledge from the server.
		state.chipValue -= betValue;
		state.betValue += betValue;
		state.callValue = state.betValue;
	}
	
	private void buildMoneyText(Skin skin){
		Table moneyTextTable = new Table(skin);
		
		moneyTextTable.defaults().width(MONEY_TEXT_WIDTH*ppuX);
		moneyTextTable.defaults().height(MONEY_TEXT_HEIGHT*ppuY);
		moneyTextTable.bottom().right();
		moneyTextTable.padRight(MONEY_TABLE_PADDING_RIGHT*ppuX);
		moneyTextTable.padBottom(MONEY_TABLE_PADDING_BOTTOM*ppuY);
		
		Label cashText = new Label("Cash:", skin);
		cashLabel = new Label("--", skin);
		moneyTextTable.add(cashText);
		moneyTextTable.add(cashLabel).padRight(MONEY_TABLE_PADDING_RIGHT*ppuX);
		
		Label betText = new Label("Bet:", skin);
		betLabel = new Label("--", skin);
		moneyTextTable.add(betText);
		moneyTextTable.add(betLabel);	
		
		moneyTextTable.setFillParent(true);
		
		stage.addActor(moneyTextTable);	
	}
	
	private void buildCards(Skin skin){
		CardImage card1Image = myHand.getCard1();
		card1Image.setX(CARD1_ORIGIN_X*ppuX);
		card1Image.setY(CARD1_ORIGIN_Y*ppuY);
		card1Image.setWidth(CARD_WIDTH*ppuX);
		card1Image.setHeight(CARD_HEIGHT*ppuY);
		stage.addActor(card1Image);
		
		CardImage card2Image = myHand.getCard2();
		card2Image.setX(CARD2_ORIGIN_X*ppuX);
		card2Image.setY(CARD2_ORIGIN_Y*ppuY);
		card2Image.setWidth(CARD_WIDTH*ppuX);
		card2Image.setHeight(CARD_HEIGHT*ppuY);
		stage.addActor(card2Image);
	}
	
	public void render(float delta){
		super.render(delta);
		myHand.updateCards(state);
		betLabel.setText(new Integer(state.betValue).toString());
		cashLabel.setText(new Integer(state.chipValue).toString());
		
		stage.draw();
		stage.act(delta);
	}
	
	public class HandRenderer {
		
		//The cards turn face-up when you press on either of them
		private boolean isFaceUp;
		
		//Used to detect an upwards drag
		private float startY;
		
		@Getter
		private CardImage card1;
		
		@Getter
		private CardImage card2;
		
		public HandRenderer(){
			card1 = new CardImage();
			card2 = new CardImage();
			startY = -1f;
		}
		
		public class CardImage extends Image{			
			public CardImage(){
				super();
				isFaceUp = false;
				
				addListener(new DragListener(){
					public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){
						startY = y;
						return true;
					}
					
					public void touchDragged(InputEvent event, float x, float y, int pointer){
						if (y > startY && startY > 0){
							isFaceUp = true;
						}
					}
					
					public void touchUp(InputEvent event, float x, float y, int pointer, int button){
						isFaceUp = false;
						startY = -1f;
					}
					
				});
			}
		}
		public void updateCards(PlayerState state){
			int card1Value = PokerLib.CARD_BACK;
			int card2Value = PokerLib.CARD_BACK;
			if (isFaceUp){
				card1Value = state.card1;
				card2Value = state.card2;
			}
			card1.setDrawable(new TextureRegionDrawable(cardTextureMap.get(card1Value)));
			card2.setDrawable(new TextureRegionDrawable(cardTextureMap.get(card2Value)));
		}
	}

	@Override
	public void resize(int width, int height) {
		stage.setViewport(width, height, true);
	}

	@Override
	public void show() {
		super.show();
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void hide() {
		super.hide();
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

	@Override
	protected void registerEvents() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void unregisterEvents(EventEmitter eventEmitter) {
		// TODO Auto-generated method stub
		
	}

}
