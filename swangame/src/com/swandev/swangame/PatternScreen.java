package com.swandev.swangame;

import java.util.List;
import java.util.Map;

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
import com.google.common.collect.Lists;

public class PatternScreen implements Screen {

	private static final float PATTERN_DELAY = 1;
	final MyGdxGame game;
	final OrthographicCamera camera;
	ShapeRenderer patternDisp;
	float r;
	int index;
	float timePassed;
	List<String> pattern = Lists.newArrayList("red","red", "green", "blue");
	Map<String, Color> stringToColour = ImmutableMap.of("red", Color.RED, "green", Color.GREEN, "blue", Color.BLUE);

	public PatternScreen(MyGdxGame game) {
		this.game = game;
		this.camera = new OrthographicCamera();
		camera.setToOrtho(false);
		// CircleInfo a = new CircleInfo(Color.GREEN);
		patternDisp = new ShapeRenderer(); // initialize shape renderer for
											// patterns
		r = 0; // starting radius for the circle;
		timePassed = 0;
		index = 0;
	}

	public void drawCircle(Color colour) {
		patternDisp.begin(ShapeType.Line);
		patternDisp.setColor(0, 0, 0, 1);
		patternDisp.circle(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, r);
		patternDisp.end();

		patternDisp.begin(ShapeType.Filled);
		patternDisp.setColor(colour);
		patternDisp.circle(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, r);
		patternDisp.end();
	}

	public void drawPrev(Color colour) {
		patternDisp.begin(ShapeType.Line);
		patternDisp.setColor(0, 0, 0, 1);
		patternDisp.circle(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, Math.max(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
		patternDisp.end();

		patternDisp.begin(ShapeType.Filled);
		patternDisp.setColor(colour);
		patternDisp.circle(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, Math.max(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
		patternDisp.end();
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glLineWidth(Math.max(Gdx.graphics.getWidth(), Gdx.graphics.getHeight())*0.1f);
		camera.update();
		patternDisp.setProjectionMatrix(camera.combined);
		// shape renders
		if (index == 0) {
			drawCircle(stringToColour.get(pattern.get(0)));
		} 
		else {
			drawPrev(stringToColour.get(pattern.get(index -1)));
			drawCircle(stringToColour.get(pattern.get(index)));
		}
		
		r = (timePassed/PATTERN_DELAY)*Math.max(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		timePassed += delta;
		if (timePassed > PATTERN_DELAY) {
			index += 1;
			if (index >= pattern.size()){
				index = 0;
			}
			timePassed = 0;
		}
		System.out.println(delta);

		final SpriteBatch spriteBatch = game.getSpriteBatch();
		spriteBatch.setProjectionMatrix(camera.combined);

		spriteBatch.begin();
		renderCenteredText(StringAssets.WELCOME_TO_PATTERN);
		spriteBatch.end();
		
		game.getSocketIO().flushEvents();
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
		// TODO Auto-generated method stub

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
