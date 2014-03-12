package com.swandev.pattern;

import lombok.Getter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class Assets {

	@Getter
	private BitmapFont font;

	@Getter
	private Skin skin;

	public Assets() {
		this.font = new BitmapFont();
		font.setColor(Color.WHITE);

		this.skin = new Skin(Gdx.files.internal("uiskin.json"));

	}

	public void dispose() {
		font.dispose();
		skin.dispose();
	}
}
