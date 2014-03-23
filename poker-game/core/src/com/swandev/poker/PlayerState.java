package com.swandev.poker;

public class PlayerState {

	public int card1;
	public int card2;

	public int chipValue;
	public int betValue;
	public int totalBetValue;
	
	public int callValue;

	public PlayerState() {
		card1 = PokerLib.CARD_BACK;
		card2 = PokerLib.CARD_BACK;
		chipValue = 100000;
		betValue = 0;
		callValue = 0;
	}

	public boolean receiveCard(int cardCode) {
		if (this.card1 == PokerLib.CARD_BACK) {
			this.card1 = cardCode;
		} else if (this.card2 == PokerLib.CARD_BACK) {
			this.card2 = cardCode;
		} else {
			// both cards were initialized; pushing a new card is not allowed!
			return false;
		}
		return true;
	}

	public void clearHand() {
		this.card1 = PokerLib.CARD_BACK;
		this.card2 = PokerLib.CARD_BACK;
	}
	
	public void reset(){
		card1 = PokerLib.CARD_BACK;
		card2 = PokerLib.CARD_BACK;
		chipValue = 100000;
		betValue = 0;
		callValue = 0;
	}
}
