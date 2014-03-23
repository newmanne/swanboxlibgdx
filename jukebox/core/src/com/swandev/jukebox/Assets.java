package com.swandev.jukebox;

import lombok.Getter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class Assets {

	@Getter
	private final Skin skin;

	@Getter
	private final FreeTypeFontGenerator fontGenerator;

	public Assets() {
		this.skin = new Skin(Gdx.files.internal("uiskin.json"));
		fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("arial.ttf"));
	}

	public void dispose() {
		skin.dispose();
	}
}
