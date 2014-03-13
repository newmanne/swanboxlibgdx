package com.swandev.poker;

import java.util.List;

import lombok.Data;

@Data
public class PokerPot {

	int value;

	public void add(int amount) {
		value += amount;
	}

	public void reset() {
		value = 0;
	}

	public void payout(List<PlayerStats> winners) {
		// TODO: actually deal with split pots - perhaps this should have a map<Player, contribution> ?
		for (PlayerStats winner : winners) {
			winner.setMoney(winner.getMoney() + value / winners.size());
		}
	}

}
