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
	private final Image myTurnImage;
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
		
		chipValueLabel = new Label(chipValue.toString(), skin);
		
		actionLabel = new Label("", skin);
		actionLabel.setVisible(false);
		
		TextureRegion chevron = new TextureRegion(new Texture(Gdx.files.internal("images/cur_player_chevron.png")));
		chevron.flip(true, false); //flip the image in the X direction
		myTurnImage = new Image(chevron);
		myTurnImage.setVisible(false);
		
		card1Image = new Image();
		card2Image = new Image();
		clearCardImages();
		toggleCardsVisible(false);
		
		//Build the table
		defaults().height(rowHeight);
		add(card1Image).height(cardHeight).width(cardWidth).right();
		add(card2Image).height(cardHeight).width(cardWidth);
		row();
		add(nameLabel).width(nameWidth).colspan(2);
		add(myTurnImage).width(turnWidth);
		row();
		add(chipValueLabel).width(moneyWidth);
		add(actionLabel).width(actionWidth);
		row();			
	}
	
	public void setCardImages(int card1, int card2){
		card1Image.setDrawable(new TextureRegionDrawable(cardToImage.get(card1)));
		card2Image.setDrawable(new TextureRegionDrawable(cardToImage.get(card2)));
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
		myTurnImage.setVisible(myTurn);
	}
}
