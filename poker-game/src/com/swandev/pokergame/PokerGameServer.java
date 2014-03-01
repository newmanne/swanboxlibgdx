package com.swandev.pokergame;

import lombok.Getter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class PokerGameServer extends Game {
	
	@Getter
	private SpriteBatch spriteBatch;
	
	@Override
	public void create() {		
		spriteBatch = new SpriteBatch();
		//setScreen(new HandScreen());
		setScreen(new HandRanking(this));
	}
}
