package com.swandev.swangame;

import lombok.Getter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class PatternClientGame extends Game {

	@Getter
	private SpriteBatch spriteBatch;

	@Getter
	private ShapeRenderer shapeRenderer;

	@Getter
	private Assets assets;

	@Getter
	private SocketIOState socketIO;

	@Override
	public void create() {
		this.spriteBatch = new SpriteBatch();
		this.assets = new Assets();
		this.socketIO = new SocketIOState();
		this.shapeRenderer = new ShapeRenderer();
		setScreen(new ConnectScreen(this));
	}

	@Override
	public void dispose() {
		super.dispose();
		spriteBatch.dispose();
		assets.dispose();
	}

}
