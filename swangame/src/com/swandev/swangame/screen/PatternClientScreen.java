package com.swandev.swangame.screen;

import io.socket.IOAcknowledge;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.json.JSONArray;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.google.common.collect.Lists;
import com.swandev.swangame.PatternClientGame;
import com.swandev.swangame.socket.EventCallback;
import com.swandev.swangame.socket.EventEmitter;
import com.swandev.swangame.socket.SocketIOEvents;
import com.swandev.swangame.util.LogTags;
import com.swandev.swangame.util.PatternCommon;
import com.swandev.swangame.util.SwanUtil;

public class PatternClientScreen extends SwanScreen {
	
	private final PatternClientGame game;
	private final Stage stage;
	@Getter
	@Setter
	List<String> pattern;
	List<PatternButton> buttons = Lists.newArrayList();

	public PatternClientScreen(PatternClientGame game) {
		super(game.getSocketIO());
		this.game = game;
		this.stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false, game.getSpriteBatch());

		final Skin skin = game.getAssets().getSkin();

		Table table = new Table(skin);

		for (String colour : PatternCommon.getStringToColour().keySet()) {
			PatternButton patternButton = new PatternButton(colour, skin);
			table.add(patternButton).width(Gdx.graphics.getWidth() * 0.5f).height(Gdx.graphics.getHeight() * 0.2f);
			table.row();
			buttons.add(patternButton);
		}

		table.setFillParent(true);
		stage.addActor(table);

	}

	@Override
	public void render(float delta) {
		super.render(delta);
		stage.draw();
		stage.act(delta);
	}

	@Override
	public void resize(int width, int height) {
		stage.setViewport(width, height, true);
	}

	@Override
	public void show() {
		super.show();
		setButtonDisables(true);
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	protected void registerEvents() {
		getSocketIO().on(SocketIOEvents.PATTERN_REQUESTED, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				JSONArray jsonArray = (JSONArray) args[0];
				List<String> pattern = SwanUtil.parseJsonList(jsonArray);
				setPattern(pattern);
				setButtonDisables(false);
			}
		});

		getSocketIO().on(SocketIOEvents.GAME_OVER, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				Gdx.app.log(LogTags.SOCKET_IO, "WOMP WOMP");
				// TODO:
//				game.setScreen(game.getConnectScreen());
			}
		});
	}

	@Override
	public void hide() {
		super.hide();
		pattern.clear();
	}

	@Override
	protected void unregisterEvents(EventEmitter eventEmitter) {
		eventEmitter.unregisterEvent(SocketIOEvents.PATTERN_REQUESTED);
		eventEmitter.unregisterEvent(SocketIOEvents.GAME_OVER);
	}

	@Override
	public void dispose() {
		stage.dispose();
	}

	public void setButtonDisables(boolean isDisabled) {
		for (Button button : buttons) {
			button.setDisabled(isDisabled);
		}
	}

	public class PatternButton extends TextButton {

		final String colour;

		public PatternButton(final String colour, Skin skin) {
			super(colour, skin);
			this.colour = colour;
			setColor(PatternCommon.getStringToColour().get(colour)); // lol, clean this up
			addListener(new ChangeListener() {

				@Override
				public void changed(ChangeEvent event, Actor actor) {
					final List<String> pattern = getPattern();
					final String nextPatternElement = getPattern().get(0);
					boolean isValid = true;
					pattern.remove(0); // pop
					if (!colour.equals(nextPatternElement)) {
						isValid = false;
					}
					if (!isValid || pattern.isEmpty()) {
						if (isValid) {
							game.getSocketIO().emitToScreen(SocketIOEvents.UPDATE_SEQUENCE);
						} else {
							game.getSocketIO().emitToScreen(SocketIOEvents.INVALID_PATTERN, game.getSocketIO().getNickname());
						}
						setButtonDisables(true);
					}
				}
			});
		}
	}

}
