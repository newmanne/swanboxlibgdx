package com.swandev.connectapp;

import com.badlogic.gdx.Game;
import com.swandev.swangame.socket.SocketIOState;

public class ConnectApp extends Game {

	private final SwanService swanService;
	private Assets assets;
	private SocketIOState socketIOState;

	public ConnectApp(SwanService swanService) {
		super();
		this.swanService = swanService;
	}

	@Override
	public void create() {
		this.socketIOState = new SocketIOState();
		assets = new Assets();
		setScreen(new ClientConnectScreen(this, socketIOState, assets.getSkin(), swanService));
	}

}
