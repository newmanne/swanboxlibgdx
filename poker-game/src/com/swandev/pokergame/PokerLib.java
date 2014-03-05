package com.swandev.pokergame;

import java.util.Map;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Maps;

public class PokerLib {
	// Defines a bunch of static methods, constants, and messages to be used for the Poker game

	// Define a place for the back of the card image to be put
	static public final int CARD_BACK = -1;

	// Define some MAX's for the card codes
	public static int MAX_CARD = 14;
	public static int MAX_SUIT = 400;

	public final static int BET_CHECK = 0;
	public final static int BET_FOLD = -1;

	static public Map<Integer, TextureRegion> getCardTextures() {
		// Load the images from the texture pack into a map of card codes -> TextureRegions
		Map<Integer, TextureRegion> cardList = Maps.newHashMap();
		TextureAtlas cardAtlas = new TextureAtlas("images/cards/CardImages.pack");
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
		return cardList;
	}

	// Define the messages to be passed between client and server

	/*
	 * Note: Many messages from the server to the client come with a triplet of integers representing the player's chip $ (how much they have to bet with), the players bet $ (how much they've already bet) and the current call $.
	 * 
	 * Since this comes up so often we'll refer to this triplet as the $_state
	 */
	public static final String DEAL_HAND = "deal_hand";
	/*
	 * Sender: PokerServer Receiver: HandScreen Purpose: Initialize a new hand of cards Additional data: - card IDs (two Integers) - $_state (three Integers)
	 */

	public static final String YOUR_TURN = "your_turn";
	/*
	 * Sender: PokerServer Receiver: HandScreen Purpose: Enable the action buttons on the client side, wait for response on the server side Additional data: - call $ (Integer)
	 * 
	 * Note: This could, in theory, send the entire $_state but it's not possible for that to have changed since the last message in the actual flow so it would only be for code simplicity.
	 */

	public static final String BET_REQUEST = "bet_request";
	/*
	 * Sender: HandScreen Receiver: PokerServer Purpose: Indicate to the server that a player would like to make a certain bet. Additional data: - bet $ (Integer) - if this is 0, it indicates a check. - On an all-in, it will be the remaining chipValue for the Player. - On a call, it will be the difference between the call value and the bet value - On a raise, it will be anywhere between a check and an all-in
	 */

	public static final String FOLD_REQUEST = "fold_request";
	/*
	 * Sender: HandScreen Receiver: PokerServer Purpose: Indicate to the server that a player wants to fold. Additional data: none
	 */

	public static final String ACTION_ACKNOWLEDGE = "action_acknowledge";
	/*
	 * Sender: PokerServer Receiver: HandScreen Purpose: Indicate that the action requested (bet or fold) was valid and refresh the client's player state. After this, the client will be disabled until the next YOUR_TURN and the server will continue round-robin style. Additional data: - $_state (three Integers)
	 */

	public static final String ACTION_INVALID = "action_invalid";
	/*
	 * Sender: PokerServer Receiver: HandScreen Purpose: Indicate that the action requested was invalid and the client should input another action. Client will be re-enabled and the server will wait for another request (bet/fold). Additional data: none
	 * 
	 * Note: Technically this should never happen as the invalid buttons should be disabled on the client side if they would generate an invalid action. Just here as a failsafe.
	 */

	public static final String HAND_COMPLETE = "hand_complete";
	/*
	 * Sender: PokerServer Receiver: HandScreen Purpose: Indicate that the hand is over and refresh the client's PlayerState. Also indicate to the players whether they won or lost; presumably some animation/pop-up will occur on the client side in response. The Client will wait for the next DEAL_HAND message, and the server begins a new hand. Additional data: - $_state (three Integers) - you_won (boolean)
	 */
}
