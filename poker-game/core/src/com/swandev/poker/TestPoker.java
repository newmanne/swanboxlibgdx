package com.swandev.poker;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.swandev.poker.Card.Rank;
import com.swandev.poker.Card.Suit;
import com.swandev.poker.PokerHand.HandType;

public class TestPoker {

	@Test
	public void test_tie_breaking() {
		List<Card> table = Lists.newArrayList();
		// table
		table.add(new Card(Suit.SPADE, Rank.KING));
		table.add(new Card(Suit.CLUB, Rank.SIX));
		table.add(new Card(Suit.HEART, Rank.ACE));
		table.add(new Card(Suit.DIAMOND, Rank.SEVEN));
		table.add(new Card(Suit.SPADE, Rank.SIX));

		List<Card> batman = Lists.newArrayList(table);
		// batman
		batman.add(new Card(Suit.CLUB, Rank.FIVE));
		batman.add(new Card(Suit.DIAMOND, Rank.NINE));
		PokerHand batmanHand = PokerHand.getBestHandFromSeven(batman);

		// yoshi
		List<Card> yoshi = Lists.newArrayList(table);
		yoshi.add(new Card(Suit.SPADE, Rank.TEN));
		yoshi.add(new Card(Suit.SPADE, Rank.NINE));
		PokerHand yoshiHand = PokerHand.getBestHandFromSeven(yoshi);

		List<PokerHand> hands = Lists.newArrayList(yoshiHand, batmanHand);
		assertEquals(yoshiHand, Collections.max(hands));
	}

	@Test
	public void test_straight_flush() {
		List<Card> cards = Lists.newArrayList();
		cards.add(new Card(Suit.DIAMOND, Rank.FOUR));
		cards.add(new Card(Suit.DIAMOND, Rank.FIVE));
		cards.add(new Card(Suit.DIAMOND, Rank.SIX));
		cards.add(new Card(Suit.DIAMOND, Rank.SEVEN));
		cards.add(new Card(Suit.DIAMOND, Rank.EIGHT));
		cards.add(new Card(Suit.HEART, Rank.FOUR));
		cards.add(new Card(Suit.SPADE, Rank.FOUR));
		Collections.shuffle(cards);
		PokerHand bestHandFromSeven = PokerHand.getBestHandFromSeven(cards);
		assertEquals(HandType.STRAIGHTFLUSH, bestHandFromSeven.getHandType());
	}

	@Test
	public void test_pair() {
		List<Card> cards = Lists.newArrayList();
		cards.add(new Card(Suit.DIAMOND, Rank.JACK));
		cards.add(new Card(Suit.DIAMOND, Rank.NINE));
		cards.add(new Card(Suit.SPADE, Rank.SIX));
		cards.add(new Card(Suit.DIAMOND, Rank.SEVEN));
		cards.add(new Card(Suit.DIAMOND, Rank.EIGHT));
		cards.add(new Card(Suit.HEART, Rank.FOUR));
		cards.add(new Card(Suit.SPADE, Rank.FOUR));
		Collections.shuffle(cards);
		PokerHand bestHandFromSeven = PokerHand.getBestHandFromSeven(cards);
		assertEquals(HandType.ONEPAIR, bestHandFromSeven.getHandType());
	}

	@Test
	public void test_two_pair() {
		List<Card> cards = Lists.newArrayList();
		cards.add(new Card(Suit.DIAMOND, Rank.JACK));
		cards.add(new Card(Suit.SPADE, Rank.JACK));
		cards.add(new Card(Suit.SPADE, Rank.SIX));
		cards.add(new Card(Suit.DIAMOND, Rank.SIX));
		cards.add(new Card(Suit.DIAMOND, Rank.EIGHT));
		cards.add(new Card(Suit.HEART, Rank.NINE));
		cards.add(new Card(Suit.SPADE, Rank.TEN));
		Collections.shuffle(cards);
		PokerHand bestHandFromSeven = PokerHand.getBestHandFromSeven(cards);
		assertEquals(HandType.TWOPAIR, bestHandFromSeven.getHandType());
	}

	@Test
	public void test_best_two_pair() {
		List<Card> cards = Lists.newArrayList();
		cards.add(new Card(Suit.DIAMOND, Rank.JACK));
		cards.add(new Card(Suit.SPADE, Rank.JACK));
		cards.add(new Card(Suit.SPADE, Rank.SIX));
		cards.add(new Card(Suit.DIAMOND, Rank.SIX));
		cards.add(new Card(Suit.DIAMOND, Rank.EIGHT));
		cards.add(new Card(Suit.HEART, Rank.EIGHT));
		cards.add(new Card(Suit.SPADE, Rank.TEN));
		Collections.shuffle(cards);
		PokerHand bestHandFromSeven = PokerHand.getBestHandFromSeven(cards);
		assertEquals(HandType.TWOPAIR, bestHandFromSeven.getHandType());
		assertEquals(2, bestHandFromSeven.getRanks().count(Rank.JACK));
		assertEquals(2, bestHandFromSeven.getRanks().count(Rank.EIGHT));
	}

	@Test
	public void three_of_a_kind() {
		List<Card> cards = Lists.newArrayList();
		cards.add(new Card(Suit.DIAMOND, Rank.JACK));
		cards.add(new Card(Suit.SPADE, Rank.JACK));
		cards.add(new Card(Suit.HEART, Rank.JACK));
		cards.add(new Card(Suit.DIAMOND, Rank.SIX));
		cards.add(new Card(Suit.DIAMOND, Rank.EIGHT));
		cards.add(new Card(Suit.HEART, Rank.NINE));
		cards.add(new Card(Suit.SPADE, Rank.TEN));
		Collections.shuffle(cards);
		PokerHand bestHandFromSeven = PokerHand.getBestHandFromSeven(cards);
		assertEquals(HandType.THREEOFAKIND, bestHandFromSeven.getHandType());
	}

	@Test
	public void full_house() {
		List<Card> cards = Lists.newArrayList();
		cards.add(new Card(Suit.CLUB, Rank.ACE));
		cards.add(new Card(Suit.SPADE, Rank.ACE));
		cards.add(new Card(Suit.HEART, Rank.ACE));
		cards.add(new Card(Suit.CLUB, Rank.EIGHT));
		cards.add(new Card(Suit.CLUB, Rank.EIGHT));
		cards.add(new Card(Suit.HEART, Rank.NINE));
		cards.add(new Card(Suit.SPADE, Rank.TEN));
		Collections.shuffle(cards);
		PokerHand bestHandFromSeven = PokerHand.getBestHandFromSeven(cards);
		assertEquals(HandType.FULLHOUSE, bestHandFromSeven.getHandType());
	}

	@Test
	public void test_best_full_house() {
		List<Card> cards = Lists.newArrayList();
		cards.add(new Card(Suit.CLUB, Rank.ACE));
		cards.add(new Card(Suit.SPADE, Rank.ACE));
		cards.add(new Card(Suit.HEART, Rank.ACE));
		cards.add(new Card(Suit.CLUB, Rank.EIGHT));
		cards.add(new Card(Suit.CLUB, Rank.EIGHT));
		cards.add(new Card(Suit.HEART, Rank.NINE));
		cards.add(new Card(Suit.SPADE, Rank.NINE));
		Collections.shuffle(cards);
		PokerHand bestHandFromSeven = PokerHand.getBestHandFromSeven(cards);
		assertEquals(HandType.FULLHOUSE, bestHandFromSeven.getHandType());
		assertEquals(3, bestHandFromSeven.getRanks().count(Rank.ACE));
		assertEquals(2, bestHandFromSeven.getRanks().count(Rank.NINE));
	}

	@Test
	public void four_of_a_kind() {
		List<Card> cards = Lists.newArrayList();
		cards.add(new Card(Suit.DIAMOND, Rank.JACK));
		cards.add(new Card(Suit.SPADE, Rank.JACK));
		cards.add(new Card(Suit.HEART, Rank.JACK));
		cards.add(new Card(Suit.CLUB, Rank.JACK));
		cards.add(new Card(Suit.DIAMOND, Rank.EIGHT));
		cards.add(new Card(Suit.HEART, Rank.NINE));
		cards.add(new Card(Suit.SPADE, Rank.TEN));
		Collections.shuffle(cards);
		PokerHand bestHandFromSeven = PokerHand.getBestHandFromSeven(cards);
		assertEquals(HandType.FOUROFAKIND, bestHandFromSeven.getHandType());
	}

}
