package com.swandev.swangame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class PatternScreen implements Screen {
	
	final MyGdxGame game;
	final OrthographicCamera camera;
	
	public PatternScreen(MyGdxGame game) {
		this.game = game;
		this.camera = new OrthographicCamera();
		camera.setToOrtho(true);
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.update();

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
