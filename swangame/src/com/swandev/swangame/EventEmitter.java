package com.swandev.swangame;

import java.util.Map;

import io.socket.IOAcknowledge;

import com.google.common.collect.Maps;

public class EventEmitter {

	Map<String, EventCallback> callbacks = Maps.newHashMap();

	void onEvent(String event, IOAcknowledge ack, Object... arguments) {
		final EventCallback callback = callbacks.get(event);
		callback.onEvent(ack,arguments);
	}

	public void on(String event, EventCallback callback) {
		callbacks.put(event, callback);
	}
	
	public void clear() {
		callbacks.clear();
	}
	
	public void clearEvent(String event) {
		callbacks.remove(event);
	}

}
