package com.swandev.swangame;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

import java.net.MalformedURLException;

import lombok.Getter;
import lombok.Setter;

import org.json.JSONObject;

import com.badlogic.gdx.Gdx;

public class SocketIOState {

	@Getter
	SocketIO client;

	@Getter
	final EventEmitter eventEmitter;

	@Getter
	@Setter
	private String nickname;
	
	public SocketIOState() {
		this.eventEmitter = new EventEmitter();
	}

	public static final String SCREEN_NAME = "Screen";

	public void on(String eventName, EventCallback callback) {
		eventEmitter.on(eventName, callback);
	}

	@Getter
	@Setter
	private boolean host = false;

	public boolean isConnected() {
		return getClient() != null && getClient().isConnected();
	}
	
	/** Call this at the end of the render loop */
	public void flushEvents() {
		this.eventEmitter.flushEvents();
	}

	public void connect(final String serverAddress, final String nickname, final ConnectCallback connectCallback) throws MalformedURLException {
		final SocketIO socketIO = new SocketIO(serverAddress);
		this.client = socketIO;
		client.connect(new IOCallback() {

			@Override
			public void onMessage(JSONObject arg0, IOAcknowledge arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onMessage(String arg0, IOAcknowledge arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onError(SocketIOException ex) {
				Gdx.app.error(LogTags.SOCKET_IO, "Connection error", ex);
				connectCallback.onConnect(ex);
			}

			@Override
			public void onDisconnect() {
				Gdx.app.debug(LogTags.SOCKET_IO, "Disconnected");
				eventEmitter.clear();
			}

			@Override
			public void onConnect() {
				Gdx.app.debug(LogTags.SOCKET_IO, "Connected to " + serverAddress);
				client.emit(SocketIOEvents.NICKNAME_SET, nickname);
				setNickname(nickname);
				connectCallback.onConnect(null);
			}

			@Override
			public void on(String event, IOAcknowledge ack, Object... arguments) {
				eventEmitter.recordEvent(event, ack, arguments);
			}
		});
	}
}
