package com.swandev.swangame;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import com.google.common.collect.Lists;

public class SwanUtil {

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

}
