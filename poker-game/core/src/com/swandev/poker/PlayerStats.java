package com.swandev.poker;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.google.common.collect.Lists;

@ToString
public class PlayerStats {
	@Getter
	@Setter
	private int money;
	@Getter
	private final List<Card> privateCards = Lists.newArrayList();
	@Getter
	@Setter
	private PokerHand hand;
	@Setter
	@Getter
	private boolean folded;
	@Getter
	private int bet;
	@Getter
	private final String name;
	@Getter
	@Setter
	private boolean alive = true;
	@Getter
	@Setter
	private int totalBet;

	@Getter
	private int moneyAtHandStart;

	public PlayerStats(String name, int money) {
		this.name = name;
		this.money = money;
	}

	public void placeBet(int amount, PokerPot pot) {
		bet += amount;
		money -= amount;
		totalBet += amount;
		pot.add(amount);
	}

	public void clearBet() {
		bet = 0;
	}

	public void resetBetweenRounds() {
		bet = 0;
		folded = false;
		privateCards.clear();
		totalBet = 0;
		moneyAtHandStart = money;
	}

	public boolean isAllIn() {
		return getMoney() == 0 && !isFolded() && isAlive();
	}

}
