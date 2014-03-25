package com.swandev.poker;

import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class PlayerTable extends Table {
	private final Label nameLabel;
	
	private final Image chevronLeft;
	private final Image chevronRight;
	
	private final Label chipValueLabel;
	private final Label actionLabel;
	
	private final Image card1Image;
	private final Image card2Image;
	
	private final Map<Integer, TextureRegion> cardToImage;

	public PlayerTable(
			Skin skin, 
			String name, 
			Integer chipValue, 
			Map<Integer, TextureRegion> cardAtlas,
			float rowHeight,
			float cardHeight,
			float cardWidth,
			float nameWidth,
			float turnWidth,
			float moneyWidth,
			float actionWidth) {
		nameLabel = new Label(name, skin);
		nameLabel.setWrap(true);
		nameLabel.setAlignment(Align.left);
		cardToImage = cardAtlas;
		
		String chipVal = (chipValue != null)? chipValue.toString() : "";
		chipValueLabel = new Label(chipVal, skin);
		
		actionLabel = new Label("", skin);
		actionLabel.setVisible(false);
		
		TextureRegion chevronL = new TextureRegion(new Texture(Gdx.files.internal("images/cur_player_chevron.png")));
		TextureRegion chevronR = new TextureRegion(new Texture(Gdx.files.internal("images/cur_player_chevron.png")));
		chevronR.flip(true, false); //flip the image in the X direction
		
		chevronLeft = new Image(chevronL);
		chevronLeft.setVisible(false);
		
		chevronRight = new Image(chevronR);
		chevronRight.setVisible(false);
		
		card1Image = new Image();
		card2Image = new Image();
		clearCardImages();
		toggleCardsVisible(false);
		
		//Build the table
		defaults().height(rowHeight);
		add();
		add(card1Image).height(cardHeight).width(cardWidth).right();
		add(card2Image).height(cardHeight).width(cardWidth);
		row();
		add(chevronLeft).width(turnWidth);
		add(nameLabel).width(nameWidth).colspan(2);
		add(chevronRight).width(turnWidth);
		row();
		add();
		add(chipValueLabel).width(moneyWidth);
		add(actionLabel).width(actionWidth);
		row();
		debug();
	}
	
	public void setCardImages(int card1, int card2){
		if (cardToImage != null){
			card1Image.setDrawable(new TextureRegionDrawable(cardToImage.get(card1)));
			card2Image.setDrawable(new TextureRegionDrawable(cardToImage.get(card2)));
		}
	}
	
	public void clearCardImages(){
		setCardImages(PokerLib.CARD_BACK, PokerLib.CARD_BACK);
	}
	
	public void toggleCardsVisible (boolean visible){
		Gdx.app.log("PLAYER_TABLE", "Toggling cards "+(visible? "":"in")+"visible!");
		card1Image.setVisible(visible);
		card2Image.setVisible(visible);
	}

	public void setChipValue(Integer value) {
		chipValueLabel.setText(value.toString());
	}
	
	public void setLastAction(String action){
		Gdx.app.log("PLAYER_TABLE", "Setting action to "+action);
		actionLabel.setText(action);
		actionLabel.setVisible(true);
	}
	
	public void clearAction(){
		Gdx.app.log("PLAYER_TABLE", "Clearing action.");
		actionLabel.setVisible(false);
	}
	
	public void toggleCurrentTurn(boolean myTurn){
		Gdx.app.log("PLAYER_TABLE", (myTurn? "Setting":"Clearing")+" Current Turn");
		chevronRight.setVisible(myTurn);
		chevronLeft.setVisible(myTurn);
	}
}
