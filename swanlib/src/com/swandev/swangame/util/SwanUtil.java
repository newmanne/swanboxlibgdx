package com.swandev.swangame.util;

import java.util.List;
import java.util.Random;

import lombok.Getter;

import org.json.JSONArray;
import org.json.JSONException;

import com.google.common.collect.Lists;

public class SwanUtil {

	@Getter
	private static Random random = new Random();

	public static boolean isDebug() {
		return true;
	}

	public static List<String> parseJsonList(JSONArray jsonArray) {
		final List<String> list = Lists.newArrayList();
		for (int i = 0; i < jsonArray.length(); i++) {
			try {
				list.add(jsonArray.getString(i));
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

	public static String toAddress(String ip, String port) {
		return "http://" + ip + ":" + port;
	}
}
