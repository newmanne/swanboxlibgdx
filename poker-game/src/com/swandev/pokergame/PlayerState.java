package com.swandev.pokergame;

import com.badlogic.gdx.scenes.scene2d.ui.Button;

public class PlayerState {
	
	public int card1;
	public int card2;
	
	public int chipValue;
	public int betValue;
	
	public Button nextCardButton;
	public Button nextSuitButton;
	
	public PlayerState(){
		this.card1 = PokerLib.CARD_BACK;
		this.card2 = PokerLib.CARD_BACK;
		this.chipValue = 100000;
		this.betValue = 0;
	}
	
	public boolean receiveCard(int cardCode){
		if (this.card1 == PokerLib.CARD_BACK){
			this.card1 = cardCode;
		} else if (this.card2 == PokerLib.CARD_BACK){
			this.card2 = cardCode;
		} else{
			//both cards were initialized; pushing a new card is not allowed!
			return false;
		}
		return true;
	}
	
	public void clearHand(){
		this.card1 = PokerLib.CARD_BACK;
		this.card2 = PokerLib.CARD_BACK;
	}
	
	public void placeBet(int bet){
		//The bet button should be disabled if you aren't allowed to make
		//a bet, but in case we are given an erroneous bet then just do
		//nothing.
		if (bet <= chipValue){
			this.chipValue -= bet;
			this.betValue += bet;
		}
	}
}
