package com.swandev.swangame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

import lombok.Getter;

public class Assets {

	@Getter
	private BitmapFont font;

	public Assets() {
		this.font = new BitmapFont();
		font.setColor(Color.WHITE);
	}

	public void dispose() {
		font.dispose();
	}
}
