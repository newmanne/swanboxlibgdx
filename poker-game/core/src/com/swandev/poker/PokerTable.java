package com.swandev.poker;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import lombok.Getter;

import com.badlogic.gdx.Gdx;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.swandev.poker.PokerGameScreen.PokerRound;

public class PokerTable {

	PokerRound round;
	@Getter
	int callValue;
	@Getter
	PokerPot pot = new PokerPot();
	Deck deck = new Deck();
	@Getter
	List<Card> tableCards = Lists.newArrayList();
	List<PlayerStats> players;
	int dealer;
	int currentPlayer;
	int numChecksOrFoldsRequiredToAdvanceRounds;
	final PokerGameScreen pokerGameScreen;

	public PokerTable(PokerGameScreen pokerGameScreen, List<PlayerStats> players) {
		this.pokerGameScreen = pokerGameScreen;
		this.players = players;
	}

	public void newHand() {
		Gdx.app.log("poker", "Starting a new round of poker!");
		callValue = 0;
		deck.reset();
		round = PokerRound.PREFLOP;
		pot.reset();
		for (PlayerStats player : players) {
			player.resetBetweenRounds();
		}
		deck.shuffle();
		tableCards = deck.dealTable();
		Gdx.app.log("poker", "Table cards are " + tableCards);
		for (PlayerStats player : players) {
			if (player.isAlive()) {
				deck.deal(player);
				Gdx.app.log("poker", "Dealt " + player.getPrivateCards() + " to player " + player.getName());
				player.setHand(getBestHand(player));
				player.placeBet(PokerLib.ANTE < player.getMoney() ? player.getMoney() : PokerLib.ANTE, pot);
				pokerGameScreen.getSocketIO().swanEmit(PokerLib.DEAL_HAND, player.getName(), getPictueValuesForCards(player), 0, player.getMoney(), 0, player.getTotalBet());
			} else {
				pokerGameScreen.getSocketIO().swanEmit(PokerLib.GAMEOVER, player.getName());
			}
		}
		numChecksOrFoldsRequiredToAdvanceRounds = getNumRemainingPlayersInRound();
		if (getNumRemainingPlayersInRound() == 1) {
			pokerGameScreen.getSocketIO().swanBroadcast(PokerLib.GAMEOVER);
			Gdx.app.log("POKER", "Game over detected, switching to server connect screen");
			pokerGameScreen.game.setScreen(pokerGameScreen.game.getServerConnectScreen());
		} else {
			dealer = nextUnfoldedAlivePlayerThatCanAct(dealer);
			currentPlayer = nextUnfoldedAlivePlayerThatCanAct(dealer);
			pokerGameScreen.uiForPreFlop();
			notifyYourTurn(players.get(currentPlayer));
		}
	}

	private List<Integer> getPictueValuesForCards(PlayerStats player) {
		final List<Integer> cardPictureValues = Lists.newArrayList();
		for (Card card : player.getPrivateCards()) {
			cardPictureValues.add(card.getImageNumber());
		}
		return cardPictureValues;
	}

	private void notifyYourTurn(PlayerStats playerStats) {
		pokerGameScreen.setPlayerTurn(playerStats.getName(), true); // make the chevrons visible
		pokerGameScreen.clearPlayerAction(playerStats.getName());
		pokerGameScreen.getSocketIO().swanEmit(PokerLib.YOUR_TURN, playerStats.getName(), playerStats.getBet(), playerStats.getMoney(), callValue, playerStats.getTotalBet());
	}

	private int nextUnfoldedAlivePlayerThatCanAct(int playerNumber) {
		int nextUnfoldedAlivePlayer = (playerNumber + 1) % players.size();
		while (!players.get(nextUnfoldedAlivePlayer).isAlive() || players.get(nextUnfoldedAlivePlayer).isFolded() || players.get(nextUnfoldedAlivePlayer).isAllIn()) {
			nextUnfoldedAlivePlayer = (nextUnfoldedAlivePlayer + 1) % players.size();
		}
		return nextUnfoldedAlivePlayer;
	}

	private PokerHand getBestHand(PlayerStats player) {
		List<Card> combinedHand = Lists.newArrayList(player.getPrivateCards());
		combinedHand.addAll(getTableCards());
		return PokerHand.getBestHandFromSeven(combinedHand);
	}

	private int getNumRemainingPlayersInRound() {
		int remainingPlayers = 0;
		for (PlayerStats player : players) {
			if (player.isAlive() && !player.isFolded()) {
				remainingPlayers++;
			}
		}
		return remainingPlayers;
	}

	public void foldPlayer(PlayerStats player) {
		player.setFolded(true);
		pokerGameScreen.setPlayerAction(player.getName(), "FOLD");
		pokerGameScreen.clearPlayerCards(player.getName());
		numChecksOrFoldsRequiredToAdvanceRounds--;
		nextPlayer();
	}

	public void betPlayer(PlayerStats currentPlayer, int amount) {
		String action = "";
		if (amount == PokerLib.BET_CHECK) {
			numChecksOrFoldsRequiredToAdvanceRounds--;
			action = "CHECK";
		} else {
			numChecksOrFoldsRequiredToAdvanceRounds = Integer.MAX_VALUE;
			currentPlayer.placeBet(amount, pot);
			action = currentPlayer.getBet() == callValue ? "CALL" : "RAISE";
			callValue = Math.max(callValue, currentPlayer.getBet());
			if (currentPlayer.isAllIn()) {
				action = "ALL IN";
			}
		}
		pokerGameScreen.setPlayerAction(currentPlayer.getName(), action);
		pokerGameScreen.getSocketIO().swanEmit(PokerLib.ACTION_ACKNOWLEDGE, currentPlayer.getName(), currentPlayer.getBet(), currentPlayer.getMoney(), callValue, currentPlayer.getTotalBet());
		nextPlayer();
	}

	private void nextPlayer() {
		// clear the current player's turn
		pokerGameScreen.setPlayerTurn(players.get(currentPlayer).getName(), false);
		if (shouldAdvanceRounds()) {
			while (shouldAdvanceRounds()) {
				nextRound();
			}
		} else {
			currentPlayer = nextUnfoldedAlivePlayerThatCanAct(currentPlayer);
			PlayerStats playerStats = players.get(currentPlayer);
			notifyYourTurn(playerStats);
		}
	}

	private boolean shouldAdvanceRounds() {
		// If everyone has checked (ie call value is 0 and the player who just played was last alive closest to dealer)
		boolean shouldAdvance = false;
		if (numChecksOrFoldsRequiredToAdvanceRounds == 0) {
			Gdx.app.log("POKER", "Advancing rounds because everyone has checked or folded");
			shouldAdvance = true;
		} else {
			// If everyone alive still in has bet the same (non-zero) amount or is all in, the round should end
			final List<Integer> bets = Lists.newArrayList();
			for (PlayerStats player : players) {
				if (player.isAlive() && !player.isFolded() && player.getBet() > 0 && !player.isAllIn()) {
					bets.add(player.getBet());
				}
			}
			if (bets.size() == getNumRemainingPlayersInRound() - getNumAllIn() && Sets.newHashSet(bets).size() == 1) {
				shouldAdvance = true;
			}
		}
		return shouldAdvance;
	}

	private void nextRound() {
		pokerGameScreen.clearPlayerActions();
		if (round == PokerRound.RIVER) {
			endHand();
		} else {
			round = PokerRound.values()[round.ordinal() + 1];
			Gdx.app.log("POKER", "Advancing to round " + round);
			pokerGameScreen.uiForDrawCards(round);
			for (PlayerStats player : players) {
				player.clearBet();
			}
			callValue = 0;
			numChecksOrFoldsRequiredToAdvanceRounds = getNumRemainingPlayersInRound() - getNumAllIn();
			currentPlayer = nextUnfoldedAlivePlayerThatCanAct(dealer);
			notifyYourTurn(players.get(currentPlayer));
		}
	}

	private int getNumAllIn() {
		int numAllIn = 0;
		for (PlayerStats player : players) {
			if (player.isAllIn()) {
				numAllIn++;
			}
		}
		return numAllIn;
	}

	public void endHand() {
		final List<PlayerStats> showdownPlayers = Lists.newArrayList();
		final List<PlayerStats> foldedList = Lists.newArrayList();
		for (PlayerStats player : players) {
			if (player.isAlive()) {
				List<PlayerStats> list = player.isFolded() ? foldedList : showdownPlayers;
				list.add(player);
			}
		}
		Collections.sort(showdownPlayers, new Comparator<PlayerStats>() {

			@Override
			public int compare(PlayerStats o1, PlayerStats o2) {
				return o1.getHand().compareTo(o2.getHand());
			}
		});
		Collections.reverse(showdownPlayers);
		List<PlayerStats> winners = pot.payout(showdownPlayers, foldedList);
		Gdx.app.log("poker", "Winning hands " + showdownPlayers.get(0).getHand());
		for (PlayerStats player : players) {
			player.setAlive(player.getMoney() > PokerLib.ANTE);
			pokerGameScreen.getSocketIO().swanEmit(PokerLib.HAND_COMPLETE, player.getName(), 0, player.getMoney(), 0, 0, winners.contains(player));
		}
		pokerGameScreen.clearPlayerActions();
		pokerGameScreen.uiBetweenHands();
	}

}