package com.swandev.swanlib.socket;

import io.socket.SocketIOException;

public interface ConnectCallback {

	void onConnect(SocketIOException ex);
	
}
