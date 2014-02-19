package com.swandev.swangame;

import io.socket.IOAcknowledge;

public interface EventCallback {
	
	void onEvent(IOAcknowledge ack, Object... args);

}
