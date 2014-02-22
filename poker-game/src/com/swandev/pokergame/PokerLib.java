package com.swandev.pokergame;

import java.util.Map;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Maps;

public class PokerLib {

	static public final int CARD_BACK = -1;
	
	static public Map<Integer, TextureRegion> getCardTextures(){
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
		cardList.put(201, cardAtlas.findRegion("1"));
		cardList.put(401, cardAtlas.findRegion("2"));
		cardList.put(301, cardAtlas.findRegion("3"));
		cardList.put(101, cardAtlas.findRegion("4"));
		cardList.put(CARD_BACK, cardAtlas.findRegion("b1fv"));
		return cardList;
	}
}
