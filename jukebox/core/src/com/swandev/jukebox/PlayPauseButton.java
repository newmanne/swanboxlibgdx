package com.swandev.jukebox;

import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.google.common.collect.ImmutableMap;
import com.swandev.swanlib.socket.SocketIOState;

public class PlayPauseButton extends ImageButton {

	final public static String play = "PLAY";
	final public static String pause = "PAUSE";
	private String state;
	Map<String, String> stateToEvents = ImmutableMap.of(play, JukeboxLib.USER_PLAY, pause, JukeboxLib.USER_PAUSE);

	public PlayPauseButton(final SocketIOState socketIO) {
		super(new PlayPauseButtonStyle());
		state = pause;
		addListener(new ChangeListener() {

			@Override
			public void changed(ChangeEvent event, Actor actor) {
				socketIO.emitToScreen(stateToEvents.get(state));
				toggleState();
			}

		});
	}

	private void toggleState() {
		state = state.equals(pause) ? play : pause;
		((PlayPauseButtonStyle) getStyle()).setState(state);
	}

	public static class PlayPauseButtonStyle extends ImageButtonStyle {

		private final Drawable playUp, playDown, pauseUp, pauseDown;

		public PlayPauseButtonStyle() {
			playUp = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("images/play_up.png"))));
			playDown = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("images/play_down.png"))));
			pauseUp = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("images/pause_up.png"))));
			pauseDown = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("images/pause_down.png"))));
			setState(pause);
		}

		public void setState(String state) {
			imageUp = state.equals(pause) ? pauseUp : playUp;
			imageDown = state.equals(pause) ? pauseDown : playDown;
		}

	}

}
