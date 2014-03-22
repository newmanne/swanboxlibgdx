package com.swandev.swanlib.screen;

import io.socket.IOAcknowledge;

import com.swandev.swanlib.socket.CommonSocketIOEvents;
import com.swandev.swanlib.socket.EventCallback;
import com.swandev.swanlib.socket.SocketIOState;

/** Use this when you need to coordinate that everyone is ready */
public abstract class SwanGameStartScreen extends SwanScreen {

	boolean everyoneReady;

	public SwanGameStartScreen(SocketIOState socketIO) {
		super(socketIO);
	}

	@Override
	public void render(float delta) {
		super.render(delta);
		if (everyoneReady) {
			doRender(delta);
		}
	}

	protected abstract void doRender(float delta);

	protected abstract void doShow();

	@Override
	public void show() {
		super.show();
		everyoneReady = false;
		getSocketIO().on(CommonSocketIOEvents.EVERYONE_READY, new EventCallback() {

			@Override
			public void onEvent(IOAcknowledge ack, Object... args) {
				getSocketIO().getEventEmitter().unregisterEvent(CommonSocketIOEvents.EVERYONE_READY);
				everyoneReady = true;
				onEveryoneReady();
			}
		});
		doShow();
		ackReady();
	}

	private void ackReady() {
		getSocketIO().getClient().emit(CommonSocketIOEvents.PLAYER_READY, getSocketIO().getNickname());
	}

	protected void onEveryoneReady() {
	};

}
