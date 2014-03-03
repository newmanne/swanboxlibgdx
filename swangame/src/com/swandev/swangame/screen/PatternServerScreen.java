package com.swandev.swangame.screen;

import io.socket.IOAcknowledge;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.swandev.swangame.PatternServerGame;
import com.swandev.swangame.socket.EventCallback;
import com.swandev.swangame.socket.EventEmitter;
import com.swandev.swangame.socket.SocketIOEvents;
import com.swandev.swangame.util.LogTags;
import com.swandev.swangame.util.PatternCommon;
import com.swandev.swangame.util.SwanUtil;

public class PatternServerScreen extends SwanScreen {

	private static final float PATTERN_DELAY = 1;
	final PatternServerGame game;
	final OrthographicCamera camera;
	String currentPlayer;
	float currentRadius;
	int index;
	float timePassed;
	List<String> pattern;
	final List<String> patternColors = ImmutableList.of("red", "green", "blue");
	boolean shouldDisplayPattern;

	public PatternServerScreen(PatternServerGame game) {
		super(game.getSocketIO());
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
		super.render(delta);
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
	}

	private void displayPattern(float delta) {
		ShapeRenderer shapeRenderer = game.getShapeRenderer();
		shapeRenderer.setProjectionMatrix(camera.combined);

		drawCircle(PatternCommon.getStringToColour().get(pattern.get(index)), currentRadius);

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
	public void show() {
		super.show();
		pattern = Lists.newArrayList(getRandomColour());
		shouldDisplayPattern = true;
		currentPlayer = game.getSocketIO().getNicknames().get(0);
	}

	private void advanceGameAfterLosingPlayer(String player) {
		getSocketIO().getNicknames().remove(player);
		if (getSocketIO().getNicknames().isEmpty()) {
			getSocketIO().swanBroadcast(SocketIOEvents.GAME_OVER);
			game.setScreen(game.getServerConnectScreen());
		} else {
			takeTurn(false);
		}
	}

	@Override
	protected void registerEvents() {
		getSocketIO().on(SocketIOEvents.UPDATE_SEQUENCE, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				takeTurn(true);
			}

		});
		EventCallback removeAPlayer = new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				String removeName = (String) args[0];
				advanceGameAfterLosingPlayer(removeName);
			}

		};
		getSocketIO().on(SocketIOEvents.GAME_OVER, new EventCallback() {
			
			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				getSocketIO().getClient().disconnect();
				Gdx.app.log(LogTags.SOCKET_IO, "Received game over signal, exiting");
				Gdx.app.exit();
			}
		});
		getSocketIO().on(SocketIOEvents.INVALID_PATTERN, removeAPlayer);
		getSocketIO().on(SocketIOEvents.CLIENT_DISCONNECT, removeAPlayer);
	}

	private void takeTurn(boolean addNewColour) {
		if (addNewColour) {
			pattern.add(getRandomColour());
		}
		currentPlayer = SwanUtil.getNextRoundRobin(game.getSocketIO().getNicknames(), currentPlayer);
		shouldDisplayPattern = true;
	}

	private String getRandomColour() {
		return patternColors.get(SwanUtil.getRandom().nextInt(patternColors.size()));
	}

	@Override
	protected void unregisterEvents(EventEmitter eventEmitter) {
		eventEmitter.unregisterEvent(SocketIOEvents.UPDATE_SEQUENCE);
		eventEmitter.unregisterEvent(SocketIOEvents.GAME_OVER);
		eventEmitter.unregisterEvent(SocketIOEvents.INVALID_PATTERN);
		eventEmitter.unregisterEvent(SocketIOEvents.CLIENT_DISCONNECT);
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

}
