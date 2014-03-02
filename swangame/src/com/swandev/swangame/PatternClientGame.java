package com.swandev.swangame;

import java.util.Map;

import lombok.Getter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.swandev.swangame.screen.ClientConnectSplashScreen;
import com.swandev.swangame.screen.PatternClientScreen;
import com.swandev.swangame.socket.SocketIOState;

public class PatternClientGame extends Game {
	
	final Map<String, String> params;
	
	public PatternClientGame(Map<String, String> params) {
		this.params = params;
	}

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
	
	@Getter
	private ClientConnectSplashScreen connectClientScreen;

	@Override
	public void create() {
		spriteBatch = new SpriteBatch();
		assets = new Assets();
		socketIO = new SocketIOState();
		shapeRenderer = new ShapeRenderer();
		patternClientScreen = new PatternClientScreen(this);
		connectClientScreen = new ClientConnectSplashScreen(this, socketIO, params) {
			
			@Override
			protected Screen getGameScreen() {
				return getPatternClientScreen();
			}
		};
		setScreen(connectClientScreen);
	}

	@Override
	public void dispose() {
		super.dispose();
		patternClientScreen.dispose();
		connectClientScreen.dispose();
		spriteBatch.dispose();
		assets.dispose();
	}

}
