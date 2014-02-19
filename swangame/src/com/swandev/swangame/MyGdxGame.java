package com.swandev.swangame;

import lombok.Getter;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

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
