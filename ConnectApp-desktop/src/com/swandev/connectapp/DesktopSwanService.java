package com.swandev.connectapp;

import java.io.File;
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
		// TODO: this is super hacky, most of the problem is we can't get logs of newly started process
		Gdx.app.log("Switching", "Switching to " + game);
		try {
			Process exec = Runtime.getRuntime().exec("java -jar " + game + ".jar " + nickname + " " + address, null, new File("/home/newmanne/swanboxLIBGDX"));
		} catch (IOException e) {
			throw new RuntimeException("Couldn't start a new game", e);
		}
	}

}
