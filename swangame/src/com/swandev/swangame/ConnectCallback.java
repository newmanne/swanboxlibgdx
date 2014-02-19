package com.swandev.swangame;

import io.socket.SocketIOException;

public interface ConnectCallback {

	void onConnect(SocketIOException ex);
	
}
