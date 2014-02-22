package com.swandev.pokergame;

import com.badlogic.gdx.Game;

public class PokerGame extends Game {
	
	@Override
	public void create() {		
		setScreen(new HandScreen());
	}
}
