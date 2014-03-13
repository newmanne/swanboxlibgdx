package com.swandev.poker;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.swandev.poker.Card.Rank;
import com.swandev.poker.Card.Suit;

public class Deck {

	List<Card> deck = Lists.newArrayList();

	public Deck() {
		reset();
	}

	public void reset() {
		deck.clear();
		for (Suit suit : Suit.values()) {
			for (Rank rank : Rank.values()) {
				deck.add(new Card(suit, rank));
			}
		}
	}

	public void shuffle() {
		Collections.shuffle(deck);
	}

	public Card dealTop() {
		final Card card = deck.get(0);
		deck.remove(0);
		return card;
	}

	public void deal(PlayerStats player) {
		player.getPrivateCards().add(dealTop());
		player.getPrivateCards().add(dealTop());
	}

	public List<Card> dealTable() {
		List<Card> table = Lists.newArrayList();
		for (int i = 0; i < 5; i++) {
			table.add(dealTop());
		}
		return table;
	}

}
