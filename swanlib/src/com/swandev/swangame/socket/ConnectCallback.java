package com.swandev.swangame.socket;

import io.socket.SocketIOException;

public interface ConnectCallback {

	void onConnect(SocketIOException ex);
	
}
