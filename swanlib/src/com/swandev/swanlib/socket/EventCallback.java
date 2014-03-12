package com.swandev.swanlib.socket;

import io.socket.IOAcknowledge;

public interface EventCallback {
	
	void onEvent(IOAcknowledge ack, Object... args);

}
