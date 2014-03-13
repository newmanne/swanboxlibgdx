package com.swandev.poker;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import lombok.Data;
import net.ericaro.neoitertools.Itertools;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import com.google.common.collect.Multisets;
import com.google.common.collect.Sets;
import com.swandev.poker.Card.Rank;
import com.swandev.poker.Card.Suit;

/** A five card hand */
@Data
public class PokerHand implements Comparable<PokerHand> {

	public enum HandType {
		HIGHCARD, ONEPAIR, TWOPAIR, THREEOFAKIND, STRAIGHT, FLUSH, FULLHOUSE, FOUROFAKIND, STRAIGHTFLUSH;
	}

	private final List<Card> hand;
	private final Multiset<Rank> ranks;
	private HandType handType;

	public PokerHand(List<Card> hand) {
		Preconditions.checkNotNull(hand);
		this.hand = Lists.newArrayList(hand);
		Collections.sort(this.hand);
		List<Rank> rankList = Lists.newArrayList();
		for (Card card : hand) {
			rankList.add(card.getRank());
		}
		ranks = Multisets.copyHighestCountFirst(HashMultiset.create(rankList));
		setHandType();
	}

	public void setHandType() {
		if ((getNumUniqueSuits() == 1) && (getHighRank().ordinal() - getLowRank().ordinal() == 4)) {
			handType = HandType.STRAIGHTFLUSH;
		} else if ((getNumUniqueRanks() == 2) && (existNOfRank(4))) {
			handType = HandType.FOUROFAKIND;
		} else if (getNumUniqueRanks() == 2) {
			handType = HandType.FULLHOUSE;
		} else if (getNumUniqueSuits() == 1) {
			handType = HandType.FLUSH;
		} else if ((getHighRank().ordinal() - getLowRank().ordinal() == 4) && (getNumUniqueRanks() == 5)) {
			handType = HandType.STRAIGHT;
		} else if (existNOfRank(3)) {
			handType = HandType.THREEOFAKIND;
		} else if (getNumUniqueRanks() == 3) {
			handType = HandType.TWOPAIR;
		} else if (getNumUniqueRanks() == 4) {
			handType = HandType.ONEPAIR;
		} else {
			handType = HandType.HIGHCARD;
		}
	}

	private boolean existNOfRank(int i) {
		boolean existsNOfRank = false;
		for (Entry<Rank> entry : ranks.entrySet()) {
			if (entry.getCount() == i) {
				existsNOfRank = true;
			}
		}
		return existsNOfRank;
	}

	private int getNumUniqueRanks() {
		return ranks.elementSet().size();
	}

	private Rank getHighRank() {
		return Iterables.getLast(hand).getRank();
	}

	private Rank getLowRank() {
		return hand.get(0).getRank();
	}

	private int getNumUniqueSuits() {
		Set<Suit> suits = Sets.newHashSet();
		for (Card card : hand) {
			suits.add(card.getSuit());
		}
		return suits.size();
	}

	public static PokerHand getBestHandFromSeven(List<Card> cards) {
		Preconditions.checkArgument(cards.size() == 7);
		List<List<Card>> permutations = Itertools.list(Itertools.permutations(Itertools.iter(cards), 5));
		List<PokerHand> hands = Lists.newArrayList();
		for (List<Card> permutation : permutations) {
			hands.add(new PokerHand(permutation));
		}
		return Collections.max(hands);
	}

	@Override
	public int compareTo(PokerHand otherHand) {
		final int returnValue;
		if (getHandType() != otherHand.getHandType()) {
			returnValue = getHandType().ordinal() - otherHand.getHandType().ordinal();
		} else {
			// Break ties as follows: Sort by counts, values and compare. Remember that because the two hands are of the same handtype, that means that they have the same number of unique ranks!
			final Multiset<Rank> otherRanks = otherHand.getRanks();
			final Iterator<Entry<Rank>> iterator = ranks.entrySet().iterator();
			final Iterator<Entry<Rank>> otherIterator = otherRanks.entrySet().iterator();
			Rank rank = iterator.next().getElement();
			Rank otherRank = otherIterator.next().getElement();
			while (iterator.hasNext() && rank == otherRank) {
				rank = iterator.next().getElement();
				otherRank = otherIterator.next().getElement();
			}
			returnValue = rank.ordinal() - otherRank.ordinal();
		}
		return returnValue;
	}

}
