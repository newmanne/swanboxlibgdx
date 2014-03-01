package com.swandev.connectapp;

import lombok.Getter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class Assets {

	@Getter
	private final Skin skin;

	public Assets() {
		this.skin = new Skin(Gdx.files.internal("uiskin.json"));
	}

	public void dispose() {
		skin.dispose();
	}
}
