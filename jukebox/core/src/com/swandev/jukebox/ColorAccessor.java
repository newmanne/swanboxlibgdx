package com.swandev.jukebox;

import aurelienribon.tweenengine.TweenAccessor;

import com.badlogic.gdx.graphics.Color;

public class ColorAccessor implements TweenAccessor<Color> {

	public final static int R = 1;
	public final static int G = 2;
	public final static int B = 3;
	public final static int A = 4;

	@Override
	public int getValues(Color target, int tweenType, float[] returnValues) {
		switch (tweenType) {
		case R:
			returnValues[0] = target.r;
			break;
		case G:
			returnValues[0] = target.g;
			break;
		case B:
			returnValues[0] = target.b;
			break;
		case A:
			returnValues[0] = target.a;
			break;
		default:
			throw new IllegalStateException();
		}
		return 1;
	}

	@Override
	public void setValues(Color target, int tweenType, float[] newValues) {
		switch (tweenType) {
		case R:
			target.r = newValues[0];
			break;
		case G:
			target.g = newValues[0];
			break;
		case B:
			target.b = newValues[0];
			break;
		case A:
			target.a = newValues[0];
			break;
		default:
			throw new IllegalStateException();
		}

	}

}
