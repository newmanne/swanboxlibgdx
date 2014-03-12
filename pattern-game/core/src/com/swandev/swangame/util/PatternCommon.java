package com.swandev.swangame.util;

import java.util.Map;

import lombok.Getter;

import com.badlogic.gdx.graphics.Color;
import com.google.common.collect.ImmutableMap;

public class PatternCommon {

	@Getter
	private final static Map<String, Color> stringToColour = ImmutableMap.of("red", Color.RED, "green", Color.GREEN, "blue", Color.BLUE);

}
