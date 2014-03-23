package com.swandev.swanlib.util;

import java.util.List;
import java.util.Random;

import lombok.Getter;

import org.json.JSONArray;
import org.json.JSONException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.google.common.collect.Lists;

public class SwanUtil {

	@Getter
	private static Random random = new Random();

	public static boolean isDebug() {
		return true;
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> parseJsonList(JSONArray jsonArray) {
		final List<T> list = Lists.newArrayList();
		for (int i = 0; i < jsonArray.length(); i++) {
			try {
				list.add((T) jsonArray.get(i));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	public static <T> T getNextRoundRobin(List<T> playerNames, T currentObj) {
		int nextIndex = playerNames.indexOf(currentObj) + 1;
		if (nextIndex >= playerNames.size()) {
			nextIndex = 0;
		}
		return playerNames.get(nextIndex);
	}

	public static String toAddress(String ip, String string) {
		return "http://" + ip + ":" + string;
	}

	public static BitmapFont generateFont(FreeTypeFontGenerator fontGenerator, int size) {
		FreeTypeFontParameter freeTypeFontParameter = new FreeTypeFontParameter();
		freeTypeFontParameter.size = size;
		freeTypeFontParameter.magFilter = TextureFilter.Linear;
		freeTypeFontParameter.minFilter = TextureFilter.Linear;
		BitmapFont generatedFont = fontGenerator.generateFont(freeTypeFontParameter);
		return generatedFont;
	}

	public static void resizeFonts(List<Actor> actors, FreeTypeFontGenerator fontGenerator, int size, float virtualWidth, float virtualHeight, Skin skin) {
		float wScale = 1.0f * Gdx.graphics.getWidth() / virtualWidth;
		float hScale = 1.0f * Gdx.graphics.getHeight() / virtualHeight;
		if (wScale < 1) {
			wScale = 1;
		}
		if (hScale < 1) {
			hScale = 1;
		}
		BitmapFont generatedFont = generateFont(fontGenerator, (int) (size * Math.max(wScale, hScale)));
		generatedFont.setScale((float) (1.0 / wScale), (float) (1.0 / hScale));

		// TODO: this implicitly assumes that everything uses a skin, uses the "default" style, and that every object with text has the same size text. Probably should challenge some of these assumptions...
		// TODO: there may be more types we need to add to this list
		TextFieldStyle textFieldStyle = skin.get(TextFieldStyle.class);
		LabelStyle labelStyle = skin.get(LabelStyle.class);
		TextButtonStyle textButtonStyle = skin.get(TextButtonStyle.class);

		textFieldStyle.font = generatedFont;
		labelStyle.font = generatedFont;
		textButtonStyle.font = generatedFont;

		for (Actor actor : actors) {
			if (actor instanceof TextField) {
				TextField textfield = (TextField) actor;
				textfield.setStyle(textFieldStyle);
				// for some reason doing this makes it render properly
				textfield.setText(textfield.getText());
			} else if (actor instanceof Label) {
				((Label) actor).setStyle(labelStyle);
			} else if (actor instanceof TextButton) {
				((TextButton) actor).setStyle(textButtonStyle);
			}
		}
	}
}
