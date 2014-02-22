package com.swandev.swangame.screen;

import io.socket.IOAcknowledge;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.swandev.swangame.PatternServerGame;
import com.swandev.swangame.socket.EventCallback;
import com.swandev.swangame.socket.EventEmitter;
import com.swandev.swangame.socket.SocketIOEvents;
import com.swandev.swangame.util.SwanUtil;

public class PatternServerScreen implements Screen {

	private static final float PATTERN_DELAY = 1;
	final Random random = new Random();
	final PatternServerGame game;
	final OrthographicCamera camera;
	String currentPlayer;
	float currentRadius;
	int index;
	float timePassed;
	List<String> pattern;
	public final static Map<String, Color> stringToColour = ImmutableMap.of("red", Color.RED, "green", Color.GREEN, "blue", Color.BLUE);
	final List<String> patternColors = ImmutableList.of("red", "green", "blue");
	boolean shouldDisplayPattern;

	public PatternServerScreen(PatternServerGame game) {
		this.game = game;
		this.camera = new OrthographicCamera();
		camera.setToOrtho(false);
	}

	public void drawCircle(Color colour, float radius) {
		ShapeRenderer shapeRenderer = game.getShapeRenderer();
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(Color.BLACK);
		shapeRenderer.circle(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, currentRadius);
		shapeRenderer.end();

		shapeRenderer.begin(ShapeType.Filled);
		shapeRenderer.setColor(colour);
		shapeRenderer.circle(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, currentRadius);
		shapeRenderer.end();
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		Gdx.gl.glLineWidth(Math.max(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()) * 0.1f);

		camera.update();

		if (shouldDisplayPattern) {
			displayPattern(delta);
		}

		final SpriteBatch spriteBatch = game.getSpriteBatch();
		spriteBatch.setProjectionMatrix(camera.combined);

		spriteBatch.begin();
		renderCenteredText(currentPlayer + " it is your turn!");
		spriteBatch.end();

		game.getSocketIO().flushEvents();
	}

	private void displayPattern(float delta) {
		ShapeRenderer shapeRenderer = game.getShapeRenderer();
		shapeRenderer.setProjectionMatrix(camera.combined);

		if (index != 0) {
			drawCircle(stringToColour.get(pattern.get(index - 1)), Math.max(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
		}
		drawCircle(stringToColour.get(pattern.get(index)), currentRadius);

		currentRadius = (timePassed / PATTERN_DELAY) * Math.max(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		timePassed += delta;
		if (timePassed > PATTERN_DELAY) {
			index += 1;
			if (index >= pattern.size()) {
				index = 0;
				shouldDisplayPattern = false;
				game.getSocketIO().swanEmit(SocketIOEvents.PATTERN_REQUESTED, currentPlayer, pattern);
			}
			timePassed = 0;
		}
	}

	private void renderCenteredText(final String text) {
		final SpriteBatch spriteBatch = game.getSpriteBatch();
		final BitmapFont font = game.getAssets().getFont();
		final TextBounds bounds = font.getBounds(text);
		font.draw(spriteBatch, text, camera.viewportWidth / 2 - bounds.width / 2, camera.viewportHeight / 2 + bounds.height / 2);
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void show() {
		pattern = Lists.newArrayList(getRandomColour());
		shouldDisplayPattern = true;
		registerEvents();
		currentPlayer = game.getSocketIO().getNicknames().get(0);
	}

	private void registerEvents() {
		game.getSocketIO().on(SocketIOEvents.UPDATE_SEQUENCE, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				pattern.add(getRandomColour());
				currentPlayer = SwanUtil.getNextRoundRobin(game.getSocketIO().getNicknames(), currentPlayer);
				shouldDisplayPattern = true;
			}
		});
		game.getSocketIO().on(SocketIOEvents.INVALID_PATTERN, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				String removeName = (String) args[0];
				List<String> players = game.getSocketIO().getNicknames();
				players.remove(removeName);
				if (players.isEmpty()) {
					// end the game
					game.getSocketIO().swanBroadcast(SocketIOEvents.GAME_OVER);
					game.setScreen(game.getServerConnectScreen());
				}
			}
		});
	}

	private String getRandomColour() {
		return patternColors.get(random.nextInt(patternColors.size()));
	}

	@Override
	public void hide() {
		unregisterEvents();
	}

	private void unregisterEvents() {
		EventEmitter eventEmitter = game.getSocketIO().getEventEmitter();
		eventEmitter.unregisterEvent(SocketIOEvents.UPDATE_SEQUENCE);
		eventEmitter.unregisterEvent(SocketIOEvents.GAME_OVER);
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

}
