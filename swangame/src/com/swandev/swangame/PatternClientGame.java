package com.swandev.swangame;

import lombok.Getter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.swandev.swangame.screen.ClientConnectScreen;
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
	private ClientConnectScreen connectScreen;

	@Getter
	private PatternClientScreen patternClientScreen;

	@Override
	public void create() {
		spriteBatch = new SpriteBatch();
		assets = new Assets();
		socketIO = new SocketIOState();
		shapeRenderer = new ShapeRenderer();
		connectScreen = new ClientConnectScreen(this);
		patternClientScreen = new PatternClientScreen(this);
		setScreen(connectScreen);
	}

	@Override
	public void dispose() {
		super.dispose();
		connectScreen.dispose();
		patternClientScreen.dispose();
		spriteBatch.dispose();
		assets.dispose();
	}

}
