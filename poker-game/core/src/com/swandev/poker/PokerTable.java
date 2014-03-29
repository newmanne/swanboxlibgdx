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

	public static final String POKER_LOG_TAG = "POKER";

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
		Gdx.app.log(POKER_LOG_TAG, "Starting a new round of poker!");
		callValue = 0;
		deck.reset();
		round = PokerRound.PREFLOP;
		pot.reset();
		for (PlayerStats player : players) {
			player.resetBetweenRounds();
		}
		deck.shuffle();
		tableCards = deck.dealTable();
		Gdx.app.log(POKER_LOG_TAG, "Table cards are " + tableCards);
		for (PlayerStats player : players) {
			if (player.isAlive()) {
				deck.deal(player);
				Gdx.app.log(POKER_LOG_TAG, "Dealt " + player.getPrivateCards() + " to player " + player.getName());
				player.setHand(getBestHand(player));
				player.placeBet(PokerLib.ANTE, pot);
				player.clearBet();
				pokerGameScreen.getSocketIO().swanEmit(PokerLib.DEAL_HAND, player.getName(), getPictueValuesForCards(player), 0, player.getMoney(), 0, player.getTotalBet());
			} else {
				pokerGameScreen.getSocketIO().swanEmit(PokerLib.GAMEOVER, player.getName());
			}
		}
		if (getNumRemainingPlayersInRound() == 1) {
			pokerGameScreen.getSocketIO().swanBroadcast(PokerLib.GAMEOVER);
			Gdx.app.log(POKER_LOG_TAG, "Game over detected, switching to server connect screen");
			pokerGameScreen.game.setScreen(pokerGameScreen.game.getServerConnectScreen());
		} else {
			numChecksOrFoldsRequiredToAdvanceRounds = getNumRemainingPlayersInRound();
			dealer = nextUnfoldedAlivePlayer(dealer);
			currentPlayer = nextUnfoldedAlivePlayerThatCanAct(dealer);
			pokerGameScreen.uiForPreFlop();
			// everyone but one person is all in
			if (nextUnfoldedAlivePlayerThatCanAct(currentPlayer) == currentPlayer) {
				endHand();
			}
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
		int nextUnfoldedAlivePlayerThatCanAct = (playerNumber + 1) % players.size();
		while (!players.get(nextUnfoldedAlivePlayerThatCanAct).isAlive() || players.get(nextUnfoldedAlivePlayerThatCanAct).isFolded() || players.get(nextUnfoldedAlivePlayerThatCanAct).isAllIn()) {
			nextUnfoldedAlivePlayerThatCanAct = (nextUnfoldedAlivePlayerThatCanAct + 1) % players.size();
		}
		return nextUnfoldedAlivePlayerThatCanAct;
	}

	private int nextUnfoldedAlivePlayer(int playerNumber) {
		int nextUnfoldedAlivePlayerThatCanAct = (playerNumber + 1) % players.size();
		while (!players.get(nextUnfoldedAlivePlayerThatCanAct).isAlive() || players.get(nextUnfoldedAlivePlayerThatCanAct).isFolded()) {
			nextUnfoldedAlivePlayerThatCanAct = (nextUnfoldedAlivePlayerThatCanAct + 1) % players.size();
		}
		return nextUnfoldedAlivePlayerThatCanAct;
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
		final String action;
		if (amount == PokerLib.BET_CHECK) {
			numChecksOrFoldsRequiredToAdvanceRounds--;
			action = "CHECK";
		} else {
			// if at least one person bets, it isn't possible to use this condition to advance rounds anymore
			numChecksOrFoldsRequiredToAdvanceRounds = Integer.MAX_VALUE;

			currentPlayer.placeBet(amount, pot);
			action = currentPlayer.getBet() == callValue ? "CALL" : currentPlayer.isAllIn() ? "ALL IN" : "RAISE";
			callValue = Math.max(callValue, currentPlayer.getBet());
		}
		pokerGameScreen.setPlayerAction(currentPlayer.getName(), action);
		pokerGameScreen.getSocketIO().swanEmit(PokerLib.ACTION_ACKNOWLEDGE, currentPlayer.getName(), currentPlayer.getBet(), currentPlayer.getMoney(), callValue, currentPlayer.getTotalBet());
		nextPlayer();
	}

	private void nextPlayer() {
		// clear the current player's turn
		pokerGameScreen.setPlayerTurn(players.get(currentPlayer).getName(), false);
		if (shouldEndHand()) {
			endHand();
		} else if (shouldAdvanceRounds()) {
			nextRound();
		} else {
			currentPlayer = nextUnfoldedAlivePlayerThatCanAct(currentPlayer);
			PlayerStats playerStats = players.get(currentPlayer);
			notifyYourTurn(playerStats);
		}
	}

	private boolean shouldEndHand() {
		return (shouldAdvanceRounds() && round == PokerRound.RIVER) || getNumRemainingPlayersInRound() == 1 || getNumRemainingPlayersInRound() == getNumAllIn();
	}

	private boolean shouldAdvanceRounds() {
		// If everyone has checked (ie call value is 0 and the player who just played was last alive closest to dealer)
		boolean shouldAdvance = false;
		if (numChecksOrFoldsRequiredToAdvanceRounds == 0) {
			Gdx.app.log(POKER_LOG_TAG, "Advancing rounds because everyone has checked or folded");
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
		round = PokerRound.values()[round.ordinal() + 1];
		Gdx.app.log(POKER_LOG_TAG, "Advancing to round " + round);
		pokerGameScreen.uiForDrawCards(round);
		for (PlayerStats player : players) {
			player.clearBet();
		}
		callValue = 0;
		numChecksOrFoldsRequiredToAdvanceRounds = getNumRemainingPlayersInRound() - getNumAllIn();
		currentPlayer = nextUnfoldedAlivePlayerThatCanAct(dealer);
		notifyYourTurn(players.get(currentPlayer));
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
		pokerGameScreen.uiForDrawCards(PokerRound.RIVER);
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
		pot.payout(showdownPlayers, foldedList);
		Gdx.app.log(POKER_LOG_TAG, "The winning hand was " + showdownPlayers.get(0).getHand());
		for (PlayerStats player : players) {
			player.setAlive(player.getMoney() >= PokerLib.ANTE);
			pokerGameScreen.getSocketIO().swanEmit(PokerLib.HAND_COMPLETE, player.getName(), 0, player.getMoney(), 0, 0, player.getMoney() >= player.getMoneyAtHandStart());
		}
		pokerGameScreen.clearPlayerActions();
		pokerGameScreen.uiBetweenHands();
	}
}