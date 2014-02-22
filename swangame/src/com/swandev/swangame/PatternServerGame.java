package com.swandev.swangame;

import lombok.Getter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.swandev.swangame.screen.PatternServerScreen;
import com.swandev.swangame.screen.ServerConnectScreen;
import com.swandev.swangame.socket.SocketIOState;

public class PatternServerGame extends Game {

	@Getter
	private SpriteBatch spriteBatch;

	@Getter
	private ShapeRenderer shapeRenderer;

	@Getter
	private Assets assets;

	@Getter
	private SocketIOState socketIO;

	@Getter
	private ServerConnectScreen serverConnectScreen;

	@Getter
	private PatternServerScreen patternServerScreen;

	@Override
	public void create() {
		this.spriteBatch = new SpriteBatch();
		this.assets = new Assets();
		this.socketIO = new SocketIOState();
		this.shapeRenderer = new ShapeRenderer();
		serverConnectScreen = new ServerConnectScreen(this);
		patternServerScreen = new PatternServerScreen(this);
		setScreen(serverConnectScreen);
	}

	@Override
	public void dispose() {
		super.dispose();
		spriteBatch.dispose();
		assets.dispose();
	}

}
