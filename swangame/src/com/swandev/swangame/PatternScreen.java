package com.swandev.swangame;

import io.socket.IOAcknowledge;

import java.util.List;
import java.util.Map;

import lombok.Setter;

import org.json.JSONArray;

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
import com.google.common.collect.ImmutableMap;

public class PatternScreen implements Screen {

	private static final float PATTERN_DELAY = 1;
	final MyGdxGame game;
	final OrthographicCamera camera;
	ShapeRenderer patternDisp;
	float currentRadius;
	int index;
	float timePassed;
	@Setter
	List<String> pattern;
	final Map<String, Color> stringToColour = ImmutableMap.of("red", Color.RED, "green", Color.GREEN, "blue", Color.BLUE);
	boolean shouldDisplayPattern;

	public PatternScreen(MyGdxGame game) {
		this.game = game;
		this.camera = new OrthographicCamera();
		camera.setToOrtho(false);
		patternDisp = new ShapeRenderer();
	}

	public void drawCircle(Color colour, float radius) {
		patternDisp.begin(ShapeType.Line);
		patternDisp.setColor(Color.BLACK);
		patternDisp.circle(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, currentRadius);
		patternDisp.end();

		patternDisp.begin(ShapeType.Filled);
		patternDisp.setColor(colour);
		patternDisp.circle(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, currentRadius);
		patternDisp.end();
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
		renderCenteredText(StringAssets.WELCOME_TO_PATTERN);
		spriteBatch.end();

		game.getSocketIO().flushEvents();
	}

	private void displayPattern(float delta) {
		patternDisp.setProjectionMatrix(camera.combined);

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
				game.getSocketIO().getClient().emit(SocketIOEvents.FINISHED_SEQUENCE);
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
		game.getSocketIO().on(SocketIOEvents.START_SEQUENCE, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				setPattern(SwanUtil.parseJsonList((JSONArray) args[0]));
				shouldDisplayPattern = true;
			}
		});
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

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
