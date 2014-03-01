package com.swandev.connectapp;

import java.util.List;

public interface SwanService {

	List<String> getAvailableGames();
	
	void switchGame(String game, String nickname, String address);
	
}
