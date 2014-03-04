package com.swandev.pokergame;

import io.socket.IOAcknowledge;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.swandev.swangame.screen.SwanScreen;
import com.swandev.swangame.socket.EventCallback;
import com.swandev.swangame.socket.EventEmitter;
import com.swandev.swangame.util.CommonLogTags;

public class PokerGameScreen extends SwanScreen {

	public class playerStats {
		private int money;
		private List<Integer> hand;
		public boolean isFolded;
		public int potPool;
		public int bet;
		private String name;
		private List<Integer> bestHand;
		public boolean isActive;

		public playerStats(String name, int money) {
			this.name = name;
			this.money = money;
			this.potPool = 1;
			this.hand = Lists.newArrayList();
			this.bet = 0;
			this.isFolded = false;
			this.isActive = true;
		}

		public List<Integer> getHand() {
			return hand;
		}

		public void dealHand(int card) {
			if (hand.size() == 2) {
				hand.remove(0);
				hand.remove(0);
			}
			hand.add(card);
		}

		public void placeBet(int amount) {
			if (amount + bet > money) {
				bet = money;
			} else {
				bet += amount;
				money -= amount;
			}
		}

		public void fold() {
			isFolded = true;
		}

		public int myMoney() {
			return money;
		}

		public String toString() {
			return "Player: " + name + "\nhand: " + hand + "\nmoney: " + money + "\ncurrent bet: " + bet + "\nbest hand: " + bestHand.get(5);
		}

		public void setBest(List<Integer> hand) {
			bestHand = Lists.newArrayList(hand);
		}

	}

	private static final float INPUT_DELAY = 0.1f;
	private static final int STRAIGHT_FLUSH = 1000;
	private static final int QUAD = 900;
	private static final int FULL_HOUSE = 800;
	private static final int FLUSH = 700;
	private static final int STRAIGHT = 600;
	private static final int TRIPLE = 500;
	private static final int TWO_PAIRS = 400;
	private static final int PAIRS = 300;
	private static final int HIGH_CARD = 200;

	private static final int CARD_BACK = -1;

	private static final int HALF_CARD_HEIGHT = 96 / 2;
	private static final int HALF_CARD_WIDTH = 72 / 2;

	final PokerGameServer game;
	final OrthographicCamera camera;
	float timePassed;

	int round; // round of current poker hand
	int dealer; // indicates which position on the list is dealer
	int activePlayer; // indicates who is currently betting
	int callValue; // indicates minimum value
	int remainingActive; // remaining active players
	int nextRound; //
	int pot;
	
	private TextureAtlas cardAtlas;
	Map<Integer, TextureRegion> cardList = Maps.newHashMap();
	Map<String, playerStats> playerList = Maps.newHashMap();

	List<Integer> deck = Lists.newArrayList();
	List<Integer> table = Lists.newArrayList();
	List<String> playerNames = Lists.newArrayList();
	List<String> activePlayers = Lists.newArrayList();

	float xMid;
	float yMid;
	int indexKey;

	public PokerGameScreen(PokerGameServer game) {
		super(game.getSocketIO());
		this.game = game;
		this.camera = new OrthographicCamera();
		camera.setToOrtho(false);
		cardAtlas = new TextureAtlas("images/cards/CardImages.pack");
		timePassed = 0;
		round = 0;
		for (int i = 5; i <= 52; i++) {
			int key = ((i % 4) + 1) * 100;
			key = key + (14 - (i - 1) / 4);
			if (((i % 4) + 1) == 3) {
				cardList.put(key, cardAtlas.findRegion(Integer.toString(i + 1)));
			} else if (((i % 4) + 1) == 4) {
				cardList.put(key, cardAtlas.findRegion(Integer.toString(i - 1)));
			} else {
				cardList.put(key, cardAtlas.findRegion(Integer.toString(i)));
			}
		}
		cardList.put(214, cardAtlas.findRegion("1"));
		cardList.put(414, cardAtlas.findRegion("2"));
		cardList.put(314, cardAtlas.findRegion("3"));
		cardList.put(114, cardAtlas.findRegion("4"));
		cardList.put(CARD_BACK, cardAtlas.findRegion("b1fv"));
		xMid = Gdx.graphics.getWidth() / 2;
		yMid = Gdx.graphics.getHeight() / 2;
		indexKey = 101;
		for (int i = 1; i <= 4; i++) {
			for (int j = 2; j <= 14; j++) {
				deck.add(i * 100 + j);
			}
		}

		// System.out.println(deck);
	}

	@Override
	public void render(float delta) {
		super.render(delta);
		timePassed += delta;

		final SpriteBatch spriteBatch = game.getSpriteBatch();
		spriteBatch.setProjectionMatrix(camera.combined);
		spriteBatch.begin();

		switch (round) {
		case 0:
			spriteBatch.end();
			return;
		case 1:
			for (int i = 0; i < 3; i++) {
				spriteBatch.draw(cardList.get(table.get(i)), xMid - HALF_CARD_WIDTH + 13 * i - 13 * 4.5f, yMid - HALF_CARD_HEIGHT + 100);
			}
			spriteBatch.end();
			return;
		case 2:
			for (int i = 0; i < 4; i++) {
				spriteBatch.draw(cardList.get(table.get(i)), xMid - HALF_CARD_WIDTH + 13 * i - 13 * 4.5f, yMid - HALF_CARD_HEIGHT + 100);
			}
			spriteBatch.end();
			return;
		case 3:
			for (int i = 0; i < 5; i++) {
				spriteBatch.draw(cardList.get(table.get(i)), xMid - HALF_CARD_WIDTH + 13 * i - 13 * 4.5f, yMid - HALF_CARD_HEIGHT + 100);
			}
			spriteBatch.end();
			return;
		default:
			spriteBatch.end();
			return;
		}
	}

	public void testAndDeal(List<String> players, List<Integer> table) {
		Collections.shuffle(deck);
		playerStats currentPlayer;

		table.addAll(Lists.newArrayList(208, 310, 210, 110, 414));
		playerList.get(playerNames.get(0)).dealHand(308);
		playerList.get(playerNames.get(0)).dealHand(413);
		playerList.get(playerNames.get(1)).dealHand(311);
		playerList.get(playerNames.get(1)).dealHand(214);

		for (int i = 0; i < playerList.size(); i++) {
			currentPlayer = playerList.get(players.get(i));
			List<Integer> rankHand = Lists.newArrayList(currentPlayer.hand);
			rankHand.addAll(table);
			currentPlayer.setBest(rank(rankHand));
		}

	}

	public void shuffleAndDeal(List<String> players, List<Integer> table) {
		Collections.shuffle(deck);
		playerStats currentPlayer;
		int card = 0;
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < players.size(); j++) {
				currentPlayer = playerList.get(players.get(j));
				currentPlayer.dealHand(deck.get(card));
				card++;
			}
		}

		for (int i = 0; i < 5; i++) {
			table.add(deck.get(card));
			card++;
		}
		// rank(hand);
		for (int i = 0; i < playerList.size(); i++) {
			currentPlayer = playerList.get(players.get(i));
			List<Integer> rankHand = Lists.newArrayList(currentPlayer.hand);
			rankHand.addAll(table);
			currentPlayer.setBest(rank(rankHand));
			getSocketIO().swanEmit(PokerLib.DEAL_HAND, players.get(i), currentPlayer.hand, currentPlayer.bet, currentPlayer.money, 0);
		}
		System.out.println("table: " + table);
		// result = rank(hand);

	}

	public List<playerStats> compareHands(List<String> names) {
		if (names.size() == 1) {
			return Lists.newArrayList(playerList.get(names.get(0)));
		}
		playerStats winningPlayer = playerList.get(names.get(0));
		List<playerStats> tieList = Lists.newArrayList(winningPlayer);

		for (int i = 1; i < names.size(); i++) {
			playerStats tmp = playerList.get(names.get(i));
			if (winningPlayer.bestHand.get(5).equals(tmp.bestHand.get(5))) {
				tieList.add(tmp);
			} else if (winningPlayer.bestHand.get(5) < tmp.bestHand.get(5)) {
				tieList = Lists.newArrayList(tmp);
			}
		}
		List<playerStats> winner = Lists.newArrayList(tieBreaking(tieList, tieList.get(0).bestHand.get(5)));
		return winner;
	}

	public List<playerStats> tieBreaking(List<playerStats> hands, int rank) {
		List<playerStats> currentWinners = Lists.newArrayList(hands.get(0));
		playerStats tmp;
		boolean isEqual;
		for (int i = 1; i < hands.size(); i++) {
			tmp = hands.get(i);
			isEqual = true;
			switch (rank) {
			case HIGH_CARD:
				for (int j = 4; j > -1; j--) {
					if (tmp.bestHand.get(j) % 100 > currentWinners.get(0).bestHand.get(j) % 100) {
						currentWinners = Lists.newArrayList(tmp);
						isEqual = false;
						break;
					} else if (tmp.bestHand.get(j) % 100 != currentWinners.get(0).bestHand.get(j) % 100) {
						isEqual = false;
						break;
					}
				}
				if (isEqual) {

					currentWinners.add(tmp);
				}
				continue;
			case PAIRS:
				if (tmp.bestHand.get(0) % 100 > currentWinners.get(0).bestHand.get(0) % 100) {
					currentWinners = Lists.newArrayList(tmp);
					continue;
				} else if (tmp.bestHand.get(0) % 100 != currentWinners.get(0).bestHand.get(0) % 100) {
					continue;
				}

				for (int j = 4; j > 1; j--) {
					if (tmp.bestHand.get(j) % 100 > currentWinners.get(0).bestHand.get(j) % 100) {
						currentWinners = Lists.newArrayList(tmp);
						isEqual = false;
						break;
					} else if (tmp.bestHand.get(j) % 100 != currentWinners.get(0).bestHand.get(j) % 100) {
						isEqual = false;
						break;
					}
				}
				if (isEqual) {
					currentWinners.add(tmp);
				}
				continue;
			case TWO_PAIRS:
				if (tmp.bestHand.get(2) % 100 > currentWinners.get(0).bestHand.get(2) % 100) {
					currentWinners = Lists.newArrayList(tmp);
					continue;
				} else if (tmp.bestHand.get(2) % 100 != currentWinners.get(0).bestHand.get(2) % 100) {
					continue;
				}

				if (tmp.bestHand.get(0) % 100 > currentWinners.get(0).bestHand.get(0) % 100) {
					currentWinners = Lists.newArrayList(tmp);
					continue;
				} else if (tmp.bestHand.get(0) % 100 != currentWinners.get(0).bestHand.get(0) % 100) {
					continue;
				}

				if (tmp.bestHand.get(4) % 100 > currentWinners.get(0).bestHand.get(4) % 100) {
					currentWinners = Lists.newArrayList(tmp);
					continue;
				} else if (tmp.bestHand.get(4) % 100 != currentWinners.get(0).bestHand.get(4) % 100) {
					continue;
				}
				currentWinners.add(tmp);
				continue;
			case TRIPLE:

				if (tmp.bestHand.get(0) % 100 > currentWinners.get(0).bestHand.get(0) % 100) {
					currentWinners = Lists.newArrayList(tmp);
				} else if (tmp.bestHand.get(0) % 100 != currentWinners.get(0).bestHand.get(0) % 100) {
					continue;
				}

				for (int j = 4; j > 2; j--) {
					if (tmp.bestHand.get(j) % 100 > currentWinners.get(0).bestHand.get(j) % 100) {
						currentWinners = Lists.newArrayList(tmp);
						isEqual = false;
						break;
					} else if (tmp.bestHand.get(j) % 100 != currentWinners.get(0).bestHand.get(j) % 100) {
						isEqual = false;
						break;
					}
				}
				if (isEqual) {
					currentWinners.add(tmp);
				}
				continue;
			case STRAIGHT:
			case FLUSH:
			case STRAIGHT_FLUSH:
				if (tmp.bestHand.get(4) % 100 > currentWinners.get(0).bestHand.get(4) % 100) {
					currentWinners = Lists.newArrayList(tmp);
				} else if (tmp.bestHand.get(4) % 100 == currentWinners.get(0).bestHand.get(4) % 100) {
					currentWinners.add(tmp);
				}
				continue;
			case FULL_HOUSE:
			case QUAD:
				if (tmp.bestHand.get(0) % 100 > currentWinners.get(0).bestHand.get(0) % 100) {
					currentWinners = Lists.newArrayList(tmp);
					continue;
				} else if (tmp.bestHand.get(0) % 100 != currentWinners.get(0).bestHand.get(0) % 100) {
					continue;
				}
				if (tmp.bestHand.get(4) % 100 > currentWinners.get(0).bestHand.get(4) % 100) {
					currentWinners = Lists.newArrayList(tmp);
					continue;
				} else if (tmp.bestHand.get(4) % 100 == currentWinners.get(0).bestHand.get(4) % 100) {
					currentWinners.add(tmp);
					continue;
				}
				continue;
			default:
				System.out.println("Some kind of error");
			}

		}
		return currentWinners;
	}

	public List<Integer> rank(List<Integer> hand) {
		List<Integer> handRank = Lists.newArrayList(hand);
		swapRank(handRank);
		Collections.sort(handRank);
		// straight
		boolean straight = false;
		List<Integer> straightChecker = Lists.newArrayList(hand);
		List<Integer> straightList = Lists.newArrayList();
		swapRank(straightChecker);
		removePairs(straightChecker);
		Collections.sort(straightChecker);

		if (straightChecker.get(0) / 10 == 2 && straightChecker.get(straightChecker.size() - 1) / 10 == 14) {
			straightList.add(straightChecker.get(straightChecker.size() - 1));
		}
		straightList.add(straightChecker.get(0));
		for (int i = 0; i < straightChecker.size() - 1; i++) {
			if (straightChecker.get(i) / 10 == straightChecker.get(i + 1) / 10 - 1) {
				straightList.add(straightChecker.get(i + 1));
			} else {
				if (straightList.size() > 4) {
					break;
				}
				straightList.clear();
				straightList.add(straightChecker.get(i + 1));
			}
		}

		if (straightList.size() > 4) {
			straight = true;
		}

		swapSuit(straightList);
		while (straightList.size() > 5) {
			straightList.remove(0);
		}

		// flush
		boolean flush = false;
		boolean straightFlush = false;
		List<Integer> flushChecker = Lists.newArrayList(hand);
		List<Integer> flushList = Lists.newArrayList();
		List<Integer> straightFlushList = Lists.newArrayList();
		Collections.sort(flushChecker);
		flushList.add(flushChecker.get(0));
		for (int i = 0; i < flushChecker.size() - 1; i++) {
			if (flushChecker.get(i) / 100 == flushChecker.get(i + 1) / 100) {
				flushList.add(flushChecker.get(i + 1));
			} else {
				if (flushList.size() > 4) {
					break;
				}
				flushList.clear();
				flushList.add(flushChecker.get(i + 1));
			}
		}

		if (flushList.size() > 4) {
			flush = true;
			if (flushList.get(0) % 100 == 2 && flushList.get(flushList.size() - 1) % 100 == 14) {
				straightFlushList.add(flushList.get(flushList.size() - 1));
			}
			straightFlushList.add(flushList.get(0));
			for (int i = 0; i < flushList.size() - 1; i++) {
				if (flushList.get(i) % 100 == flushList.get(i + 1) % 100 - 1) {
					straightFlushList.add(flushList.get(i + 1));
				} else {
					if (straightFlushList.size() > 4) {
						break;
					}
					straightFlushList.clear();
					straightFlushList.add(flushList.get(i + 1));
				}
			}
			if (straightFlushList.size() > 4) {
				straightFlush = true;
			}

			while (flushList.size() > 5) {
				flushList.remove(0);
			}
			while (straightFlushList.size() > 5) {
				straightFlushList.remove(0);
			}
		}

		List<Integer> sortedHand = Lists.newArrayList(hand);
		swapRank(sortedHand);
		Collections.sort(sortedHand);
		List<Integer> highCard = Lists.newArrayList();
		List<Integer> pairs = Lists.newArrayList();
		List<Integer> triples = Lists.newArrayList();
		List<Integer> quads = Lists.newArrayList();

		int count = 1;
		int j;
		for (int i = 0; i < sortedHand.size(); i++) {
			if (i != sortedHand.size() - 1 && sortedHand.get(i) / 10 == sortedHand.get(i + 1) / 10) {
				count++;
			} else {
				switch (count) {
				case 1:
					highCard.add(sortedHand.get(i));
					break;
				case 2:
					pairs.add(sortedHand.get(i - 1));
					pairs.add(sortedHand.get(i));
					break;
				case 3:
					triples.add(sortedHand.get(i - 2));
					triples.add(sortedHand.get(i - 1));
					triples.add(sortedHand.get(i));
					break;
				case 4:
					quads.add(sortedHand.get(i - 3));
					quads.add(sortedHand.get(i - 2));
					quads.add(sortedHand.get(i - 1));
					quads.add(sortedHand.get(i));
					break;
				default:
					System.out.println("Invalid deck error");
					break;
				}
				count = 1;
			}
		}
		swapSuit(highCard);
		swapSuit(pairs);
		swapSuit(triples);
		swapSuit(quads);
		// straight flush

//		System.out.println("High Card is: " + highCard);
//		System.out.println("List of pairs is: " + pairs);
//		System.out.println("List of triples is: " + triples);
//		System.out.println("List of quads is: " + quads);
//		System.out.println("There is a straight " + straight);
//		
//		if (straight) {
//			System.out.println(straightList);
//		}
//
//		System.out.println("There is a flush " + flush);
//		if (flush) {
//			System.out.println(flushList);
//		}
//
//		System.out.println("There is a straight flush " + straightFlush);
//		if (straightFlush) {
//			System.out.println(straightFlushList);
//		}

		swapSuit(handRank);
		if (straightFlush) {
			straightFlushList.add(STRAIGHT_FLUSH);
			return straightFlushList; // return STRAIGHT_FLUSH
		} else if (quads.size() > 0) {
			for (int i = handRank.size() - 1; i > -1; i--) {
				if (handRank.get(i) % 100 != quads.get(0) % 100) {
					quads.add(handRank.get(i));
					quads.add(QUAD);
					return quads; // return QUAD
				}
			}
		} else if (triples.size() > 0) {
			if (triples.size() > 3) {
				if (pairs.size() != 0 && pairs.get(pairs.size() - 1) % 100 > triples.get(0) % 100) {
					triples.add(pairs.get(pairs.size() - 2));
					triples.add(pairs.get(pairs.size() - 1));
				}
				while (triples.size() > 5) {
					triples.remove(0);
				}
				if (triples.get(0) / 100 > triples.get(4) / 100) {
					triples.addAll(triples.subList(0, 1));
					while (triples.size() > 5) {
						triples.remove(0);
					}
				}
				triples.add(TRIPLE);
				return triples; // FULL_HOUSE
			} else if (pairs.size() > 0) {
				triples.add(pairs.get(pairs.size() - 2));
				triples.add(pairs.get(pairs.size() - 1));
				while (triples.size() > 5) {
					triples.remove(0);
				}
				triples.add(FULL_HOUSE);
				return triples; // FULL_HOUSE
			} else if (!flush && !straight) {
				triples.add(highCard.get(highCard.size() - 2));
				triples.add(highCard.get(highCard.size() - 1));
				while (triples.size() > 5) {
					triples.remove(0);
				}
				triples.add(TRIPLE);
				return triples; // TRIPLES
			}
		} else if (flush) {
			flushList.add(FLUSH);
			return flushList; // FLUSH
		} else if (straight) {
			straightList.add(STRAIGHT);
			return straightList; // STRAIGHT_LIST
		} else if (pairs.size() > 4) {
			for (int i = handRank.size() - 1; i > -1; i--) {
				if (handRank.get(i) % 100 != pairs.get(3) % 100 && handRank.get(i) % 100 != pairs.get(5) % 100) {
					pairs.add(handRank.get(i));
					if (pairs.size() > 5) {
						break;
					}
				}
			}
			while (pairs.size() > 5) {
				pairs.remove(0);
			}
			pairs.add(TWO_PAIRS);
			return pairs; // TWO_PAIR
		} else if (pairs.size() > 2) {
			for (int i = handRank.size() - 1; i > -1; i--) {
				if (handRank.get(i) % 100 != pairs.get(1) % 100 && handRank.get(i) % 100 != pairs.get(3) % 100) {
					pairs.add(handRank.get(i));
					if (pairs.size() == 5) {
						break;
					}
				}
			}
			pairs.add(TWO_PAIRS);
			return pairs; // TWO_PAIR

		} else if (pairs.size() > 0) {
			for (int i = handRank.size() - 1; i > -1; i--) {
				if (handRank.get(i) % 100 != pairs.get(1) % 100) {
					pairs.add(handRank.get(i));
					if (pairs.size() == 5) {
						break;
					}
				}
			}
			pairs.add(2, pairs.get(4));
			pairs.remove(5);
			pairs.add(pairs.get(3));
			pairs.remove(3);
			pairs.add(PAIRS);
			return pairs; // PAIR
		} else {
			while (highCard.size() > 5) {
				highCard.remove(0);
			}
			highCard.add(HIGH_CARD);
			return highCard; // HIGHCARD
		}

		return hand; // should not happen
	}

	public void swapRank(List<Integer> hand) {
		for (int i = 0; i < hand.size(); i++) {
			hand.set(i, hand.get(i) / 100 + hand.get(i) % 100 * 10);
		}
	}

	public void swapSuit(List<Integer> hand) {
		for (int i = 0; i < hand.size(); i++) {
			hand.set(i, hand.get(i) / 10 + hand.get(i) % 10 * 100);
		}
	}

	public void removePairs(List<Integer> hand) {
		List<Integer> removeList = Lists.newArrayList();
		for (int i = 0; i < hand.size(); i++) {
			for (int j = i + 1; j < hand.size(); j++) {
				if (hand.get(i) / 10 == hand.get(j) / 10) {
					if (hand.get(i) % 10 > hand.get(j) % 10) {
						removeList.add(hand.get(j));
					} else {
						removeList.add(hand.get(i));
					}
				}
			}
		}

		for (int i = 0; i < removeList.size(); i++) {
			hand.remove(removeList.get(i));
		}
	}

	public void dealHands() {

	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	public void startRound() {

		playerStats tmp;
		activePlayers = Lists.newArrayList();
		for (int i = 0; i < playerNames.size(); i++){
			tmp = playerList.get(playerNames.get(i));
			if (tmp.isActive){
				activePlayers.add(playerNames.get(i));
			}
		}
		table = Lists.newArrayList();
		shuffleAndDeal(activePlayers, table);
		dealer = activePlayers.size() - 1;
		activePlayer = 0;
		remainingActive = activePlayers.size();
		nextRound = remainingActive;
		callValue = 0;
		round = 0;
		pot = 0;
		getSocketIO().swanEmit(PokerLib.YOUR_TURN, activePlayers.get(activePlayer), callValue);
	}

	public void nextRound() {
		round++;
		if (round == 3) {
			Timer timer = new Timer("hi");
			timer.schedule(new TimerTask() {
				
				@Override
				public void run() {
					endRound();
					
				}
			}, 5000);
			return;
		}
		
		playerStats tmp;
		
		
		for (int i = 0; i < playerNames.size(); i++) {
			tmp = playerList.get(playerNames.get(i));
			if (tmp.isFolded) {
				continue;
			}
			activePlayer = i;
			getSocketIO().swanEmit(PokerLib.YOUR_TURN, playerNames.get(activePlayer), callValue);
			return;
		}

	}

	public void endRound() {
		// TODO msg everyone whether they won or not reset all values
		/*
		 * activePlayer should be one beyond dealer (optional for now)
		 * remainingPlayers should be reset to full value 
		 * nextRound is reinitalized 
		 * callValue is cleared 
		 * all folded players are brought back
		 */
		playerStats tmp;
		List<String> winningList = Lists.newArrayList();

		for (int i = 0; i < activePlayers.size(); i++){
			tmp = playerList.get(activePlayers.get(i));
			if (tmp.isFolded){
				tmp.isFolded = false;
			}else{
				winningList.add(playerNames.get(i));
			}
		}
		List<playerStats> winner = compareHands(winningList);
		System.out.println(winner);
		System.out.println("Pot size: " + pot);
		for (int i = 0; i < activePlayers.size();i++){
			tmp = playerList.get(activePlayers.get(i));
			tmp.bet = 0;
			if(winner.contains(tmp)){
				tmp.money += pot/winner.size();
				getSocketIO().swanEmit(PokerLib.HAND_COMPLETE, activePlayers.get(i), tmp.bet, tmp.money, 0, true);
			}
			getSocketIO().swanEmit(PokerLib.HAND_COMPLETE, activePlayers.get(i), tmp.bet, tmp.money, 0, false);
		}
		startRound();
	}

	public void nextPlayer() {
		activePlayer++;
		playerStats tmp;
		List<Integer> nextActive = Lists.newArrayList();

		for (int i = 0; i < playerNames.size(); i++) {
			tmp = playerList.get(playerNames.get(i));
			if (tmp.isFolded) {
				continue;
			}
			if (i >= activePlayer) {
				activePlayer = i;
				getSocketIO().swanEmit(PokerLib.YOUR_TURN, playerNames.get(activePlayer), callValue);
				return;
			}

			nextActive.add(i);
		}
		activePlayer = nextActive.get(0);
		getSocketIO().swanEmit(PokerLib.YOUR_TURN, playerNames.get(activePlayer), callValue);
		return;

	}

	@Override
	public void show() {
		super.show();
		playerNames = Lists.newArrayList(getSocketIO().getNicknames());
		for (int i = 0; i < playerNames.size(); i++) {
			playerList.put(playerNames.get(i), new playerStats(playerNames.get(i), 100000));
		}
		startRound();
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void registerEvents() {
		// TODO Auto-generated method stub
		getSocketIO().on(PokerLib.FOLD_REQUEST, new EventCallback() {
			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				// TODO Auto-generated method stub
				String player = (String) args[0];
				playerStats currentPlayer = playerList.get(player);
				currentPlayer.fold();
				currentPlayer.bet = 0;
				// TODO Send acks?
				// next playernextPlayer
				remainingActive--;
				nextRound--;
				if (remainingActive == 1){
					endRound();
				} else if (nextRound == 0){
					nextRound = remainingActive;
					nextRound();
				}else{
					nextPlayer();
				}


			}
		});

		getSocketIO().on(PokerLib.BET_REQUEST, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				String player = (String) args[0];
				Integer newBet = (Integer) args[1];
				playerStats currentPlayer = playerList.get(player);
				currentPlayer.placeBet(newBet);
				pot += newBet;
				if (currentPlayer.bet > callValue) {
					callValue = currentPlayer.bet;
					nextRound = remainingActive;
				} else if (currentPlayer.bet < callValue) {
					Gdx.app.error(CommonLogTags.SERVER_CONNECT_SCREEN, "Invalid Game State, bet less than call");
				} else {
					nextRound--;
				}
				// TODO send ack?
				getSocketIO().swanEmit(PokerLib.ACTION_ACKNOWLEDGE, player, currentPlayer.bet, currentPlayer.money, callValue);
				// Pot split if value is all in?
				if (nextRound == 0){
					nextRound = remainingActive;
					nextRound();
				}else{
					nextPlayer();
				}
			}
		});

	}

	@Override
	protected void unregisterEvents(EventEmitter eventEmitter) {
		// TODO Auto-generated method stub
		eventEmitter.unregisterEvent(PokerLib.FOLD_REQUEST);
		eventEmitter.unregisterEvent(PokerLib.BET_REQUEST);
	}

}
