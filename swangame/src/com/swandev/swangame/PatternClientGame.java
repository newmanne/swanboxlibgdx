package com.swandev.swangame;

import lombok.Getter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.swandev.swangame.screen.PatternClientScreen;
import com.swandev.swangame.socket.SocketIOState;

public class PatternClientGame extends Game {

	@Getter
	private SpriteBatch spriteBatch;

	@Getter
	private ShapeRenderer shapeRenderer;

	@Getter
	private Assets assets;

	@Getter
	private SocketIOState socketIO;

	@Getter
	private PatternClientScreen patternClientScreen;

	@Override
	public void create() {
		spriteBatch = new SpriteBatch();
		assets = new Assets();
		socketIO = new SocketIOState();
		shapeRenderer = new ShapeRenderer();
		patternClientScreen = new PatternClientScreen(this);
		setScreen(patternClientScreen);
	}

	@Override
	public void dispose() {
		super.dispose();
		patternClientScreen.dispose();
		spriteBatch.dispose();
		assets.dispose();
	}

}
