package com.swandev.swangame;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class MyGdxGame extends Game {

	@Getter
	private SpriteBatch spriteBatch;

	@Getter
	private ShapeRenderer shapeRenderer;

	@Getter
	private Assets assets;

	@Getter
	private SocketIOState socketIO;

	@Getter
	@Setter
	private List<String> playerNames;

	@Override
	public void create() {
		this.spriteBatch = new SpriteBatch();
		this.assets = new Assets();
		this.socketIO = new SocketIOState();
		this.shapeRenderer = new ShapeRenderer();
		setScreen(new SplashScreen(this));
	}

	@Override
	public void dispose() {
		super.dispose();
		spriteBatch.dispose();
		assets.dispose();
	}

}
