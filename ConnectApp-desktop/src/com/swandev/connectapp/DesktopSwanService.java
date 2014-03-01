package com.swandev.connectapp;

import java.io.IOException;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.google.common.collect.Lists;

public class DesktopSwanService implements SwanService {

	@Override
	public List<String> getAvailableGames() {
		// TODO: could probably hack something out with the find command, but w/e
		return Lists.newArrayList("poker", "pattern");
	}

	@Override
	public void switchGame(String game, String nickname, String address) {
		Gdx.app.log("Switching", "Switching to " + game);
		try {
			Runtime.getRuntime().exec("java -jar " + game + ".jar " + nickname + " " + address);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
