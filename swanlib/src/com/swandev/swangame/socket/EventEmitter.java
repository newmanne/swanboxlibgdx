package com.swandev.swangame.socket;

import io.socket.IOAcknowledge;

import java.util.List;
import java.util.Map;

import lombok.Data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class EventEmitter {

	Map<String, EventCallback> callbacks = Maps.newHashMap();
	List<SocketEvent> events = Lists.newArrayList();

	private void processEvent(SocketEvent event) {
		final EventCallback callback = callbacks.get(event.getEvent());
		if (callback != null) {
			callback.onEvent(event.getAck(), event.getArguments());
		}
	}

	void recordEvent(String event, IOAcknowledge ack, Object... arguments) {
		final SocketEvent socketEvent = new SocketEvent(event, ack, arguments);
		synchronized (this) {
			events.add(socketEvent);
		}
	}

	public void on(String event, EventCallback callback) {
		callbacks.put(event, callback);
	}

	public void clear() {
		callbacks.clear();
	}

	public void unregisterEvent(String event) {
		if (callbacks.get(event) == null) {
			throw new IllegalStateException("Trying to unregister an event that was never regsitered: " + event);
		}
		callbacks.remove(event);
	}

	@Data
	public static class SocketEvent {
		final String event;
		final IOAcknowledge ack;
		final Object[] arguments;
	}

	public void flushEvents() {
		final List<SocketEvent> eventsToFlush;
		synchronized (this) {
			eventsToFlush = Lists.newArrayList(events);
			events.clear();
		}
		for (SocketEvent event : eventsToFlush) {
			processEvent(event);
		}
	}

}
