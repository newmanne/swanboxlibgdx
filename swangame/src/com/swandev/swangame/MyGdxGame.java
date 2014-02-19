package com.swandev.swangame;

import lombok.Getter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class MyGdxGame extends Game {

	@Getter
	private SpriteBatch spriteBatch;

	@Getter
	private Assets assets;

	@Getter
	private SocketIOState socketIO;

	@Override
	public void create() {
		this.spriteBatch = new SpriteBatch();
		this.assets = new Assets();
		this.socketIO = new SocketIOState();
		setScreen(new SplashScreen(this));
	}

	@Override
	public void dispose() {
		super.dispose();
		spriteBatch.dispose();
		assets.dispose();
	}

}
