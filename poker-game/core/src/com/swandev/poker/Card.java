package com.swandev.poker;

import lombok.Data;

@Data
public class Card implements Comparable<Card> {

	private final Suit suit;
	private final Rank rank;

	public enum Suit {
		DIAMOND, CLUB, HEART, SPADE;
	}

	public enum Rank {
		TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE;
	}

	@Override
	public int compareTo(Card otherCard) {
		return getRank().ordinal() - otherCard.getRank().ordinal();
	}

	public int getImageNumber() {
		return Integer.parseInt(String.format("%d%02d", suit.ordinal() + 1, rank.ordinal() + 2));
	}

	@Override
	public String toString() {
		return rank + " of " + suit;
	}

}
