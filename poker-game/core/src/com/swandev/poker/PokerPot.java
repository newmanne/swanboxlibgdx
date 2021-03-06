package com.swandev.poker;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import lombok.Data;

import com.google.common.collect.Lists;

@Data
public class PokerPot {

	int value;
	
	private int leftover = 0;
	private int oldLeftover = 0;

	public void add(int amount) {
		value += amount;
	}

	public void reset() {
		value = leftover + oldLeftover;
		oldLeftover = leftover;
		leftover = 0;
	}

	public List<PlayerStats> payout(List<PlayerStats> rankList, List<PlayerStats> foldedList) {
		
		List<PlayerStats> winners = Lists.newArrayList();
		List<List<PlayerStats>> tierList = Lists.newArrayList();
		int i, j, k, l;
		for (i = 0; i < rankList.size(); i++) {
			List<PlayerStats> sameTier = Lists.newArrayList();
			sameTier.add(rankList.get(i));
			for (i = i + 1; i < rankList.size(); i++) {
				if (sameTier.get(0).getHand().compareTo(rankList.get(i).getHand()) != 0) {
					i--;
					break;
				}
				sameTier.add(rankList.get(i));
			}
			tierList.add(sameTier);
		}

		// For each list in tierList, sort in ascending order of totalBet

		for (List<PlayerStats> tier : tierList) {
			Collections.sort(tier, new Comparator<PlayerStats>() {

				@Override
				public int compare(PlayerStats o1, PlayerStats o2) {
					return o1.getTotalBet() - o2.getTotalBet();
				}
			});
		}

		// For each person in the same tier, take away their contribution
		// from remaining players.
		for (i = 0; i < tierList.size(); i++) {
			List<PlayerStats> stillIn = Lists.newArrayList(tierList.get(i));
			for (j = 0; j < tierList.get(i).size(); j++) {
				if (tierList.get(i).get(j).getTotalBet() == 0) {
					continue;
				}
				int amount = 0;
				int maxValue = tierList.get(i).get(j).getTotalBet();

				// Collect total pot contribution from each person
				for (k = i; k < tierList.size(); k++) {
					for (l = j; l < tierList.get(k).size(); l++) {
						PlayerStats tmp = tierList.get(k).get(l);
						if (tmp.getTotalBet() < maxValue) {
							amount += tmp.getTotalBet();
							tmp.setTotalBet(0);
						} else {
							amount += maxValue;
							tmp.setTotalBet(tmp.getTotalBet() - maxValue);
						}
					}
				}
				for (k = 0; k < foldedList.size(); k++) {
					PlayerStats tmp = foldedList.get(k);
					if (tmp.getTotalBet() < maxValue) {
						amount += tmp.getTotalBet();
						tmp.setTotalBet(0);
					} else {
						amount += maxValue;
						tmp.setTotalBet(tmp.getTotalBet() - maxValue);

					}
				}
				if (oldLeftover > 0){
					if (oldLeftover < maxValue){
						amount += oldLeftover;
						oldLeftover = 0;
					}else{
						amount += maxValue;
						oldLeftover -= maxValue;
					}
				}
				
				int leftover = amount%(stillIn.size() * PokerLib.ANTE);
				if (leftover > 0){
					this.leftover += leftover;
					amount -= leftover;
				}
				
				amount = amount / stillIn.size();
				
				for (k = 0; k < stillIn.size(); k++) {
					stillIn.get(k).setMoney(stillIn.get(k).getMoney() + amount);
					if (!(tierList.size() == 1) && i == tierList.size()-1 && j == tierList.get(i).size() - 1){
						// do nothing
						//Gdx.app.log("poker", "adding nothing list");
					}
					else if (!winners.contains(stillIn.get(k))) {
						
						//Gdx.app.log("poker", "adding winners list");
						winners.add(stillIn.get(k));
					}
				}
				stillIn.remove(0);

			}
		}

		return winners;
	}

}
