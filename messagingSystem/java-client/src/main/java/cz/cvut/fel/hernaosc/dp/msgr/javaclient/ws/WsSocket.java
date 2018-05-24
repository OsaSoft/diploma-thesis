package cz.cvut.fel.hernaosc.dp.msgr.javaclient.ws;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.logging.Logger;

@WebSocket
public class WsSocket {
	private static Logger log = Logger.getLogger("WsSocket");

	private Session session = null;
	private Consumer<String> messageListener;
	private IntConsumer closeListener;

	public WsSocket(Consumer<String> messageListener, IntConsumer closeListener) {
		this.messageListener = messageListener;
		this.closeListener = closeListener;
	}

	@OnWebSocketConnect
	public void onConnect(Session session) {
		log.info("Connected to websocket session: " + session);
		this.session = session;
	}

	@OnWebSocketClose
	public void onClose(int statusCode, String reason) {
		log.info("Websocket connection closed. " + statusCode + " : " + reason);
		session = null;
		closeListener.accept(statusCode);
	}

	@OnWebSocketMessage
	public void onMessage(String msg) {
		messageListener.accept(msg);
	}

	public void send(String message) throws IOException {
		session.getRemote().sendString(message);
	}

	public void close() {
		session.close();
	}
}
