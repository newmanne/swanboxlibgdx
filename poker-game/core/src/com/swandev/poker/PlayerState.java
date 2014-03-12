package com.swandev.poker;


public class PlayerState {
	
	public int card1;
	public int card2;
	
	public int chipValue;
	public int betValue;
	
	public int callValue;

	public PlayerState(){
		this.card1 = PokerLib.CARD_BACK;
		this.card2 = PokerLib.CARD_BACK;
		this.chipValue = 100000;
		this.betValue = 0;
		this.callValue = 0;
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
}
