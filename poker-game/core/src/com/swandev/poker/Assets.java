package com.swandev.poker;

import lombok.Getter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class Assets {

	@Getter
	private Skin skin;
	@Getter
	private FreeTypeFontGenerator fontGenerator;

	public Assets() {
		fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("arial.ttf"));
		this.skin = new Skin(Gdx.files.internal("uiskin.json"));
	}

	public void dispose() {
		fontGenerator.dispose();
		skin.dispose();
	}
}
