package cz.cvut.fel.hernaosc.dp.msgr.javaclient;

import cz.cvut.fel.hernaosc.dp.msgr.javaclient.ws.WsSocket;
import cz.cvut.fel.hernaosc.dp.msgr.messagecommon.dto.ConnectionRequest;
import cz.cvut.fel.hernaosc.dp.msgr.messagecommon.dto.message.MessageDto;
import cz.cvut.fel.hernaosc.dp.msgr.messagecommon.dto.message.NotificationDto;
import cz.cvut.fel.hernaosc.dp.msgr.messagecommon.dto.util.MsgrMessageUtils;
import groovy.json.JsonBuilder;
import groovy.json.JsonSlurper;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MsgrClient {
	private static Logger log = Logger.getLogger("MSGR-CLIENT");

	private static final String JSON = "application/json";
	private static final String POST = "POST";
	private static final int HTTP_OK = 200;

	private boolean websocket = false;
	private boolean connected = false;
	private WsSocket wsSocket = null;
	private Consumer<String> wsMessageListener = null;

	private String url;
	private int serverRefresh = 30;

	private String deviceId;
	private String deviceToken;
	private String userId;
	private String userName;

	private String platformName;

	private List<String> addresses;
	private long lastUpdate;

	private Function<Object, String> toJson = (obj) -> new JsonBuilder(obj).toString();

	private Map<String, Object> response = null;

	public void init() throws IOException {

		log.info("Connecting to " + url);
		refreshNodes();

		if (addresses == null || addresses.isEmpty()) {
			log.log(Level.SEVERE, "Could not find any nodes to connect to");
			throw new IOException("Could not find any nodes to connect to");
		}

		if (websocket) {
			connectWs();
		}
	}

	public void disconnect() {
		wsSocket.close();
	}

	public boolean send(MessageDto message) {
		String type = message instanceof NotificationDto ? "notification" : "message";
		boolean sent = false;
		response = null;

		try {
			if (websocket) {
				if (!connected) {
					throw new IOException("Not connected to websocket!");
				}

				sendWebsocket(message);
			} else {
				String address = getRandomAddress();
				address = "http://" + address + "/send/" + type;

				response = doRequest(POST, address, message);
			}

			if (response != null) {
				log.info("Message successfully sent");
				sent = true;
			}
		} catch (IOException ex) {
			log.log(Level.SEVERE, "Could not send due to exception: " + ex.getMessage());
		}

		return sent;
	}

	private String getRandomAddress() throws IOException {
		if (new Date().getTime() - lastUpdate > serverRefresh * 1000) {
			refreshNodes();
		}

		if (addresses == null || addresses.isEmpty()) {
			log.log(Level.SEVERE, "Could not find any nodes to connect to");
			throw new IOException("Could not find any nodes to connect to");
		}

		return MsgrMessageUtils.randomElement(addresses);
	}

	private void refreshNodes() throws IOException {
		String fullIUrl = "http://" + url + "/connect";
		response = doRequest(POST, fullIUrl, buildConnectionRequest());
		if (response != null) {
			log.info("Received result " + response);

			addresses = (List<String>) response.get("addresses");
			lastUpdate = new Date().getTime();

			setData((Map) response.get("deviceData"));
			log.info("Received node IPs: " + addresses);
		} else {
			throw new IOException("Could not connect to " + url);
		}
	}

	private void connectWs() throws IOException {
		String fullUrl = "ws://" + url + "/ws/" + getDeviceId();
		WebSocketClient webSocketClient = new WebSocketClient();
		wsSocket = new WsSocket(wsMessageListener, (closeCode) -> connected = false);

		try {
			webSocketClient.start();
			URI endpoint = new URI(fullUrl);
			ClientUpgradeRequest upgradeRequest = new ClientUpgradeRequest();
			Future<Session> connectionFuture = webSocketClient.connect(wsSocket, endpoint, upgradeRequest);
			//block until we are connected
			connectionFuture.get(10, TimeUnit.SECONDS);

			connected = true;
		} catch (Exception ex) {
			throw new IOException("Failed to connect to websocket. Reason: " + ex.getMessage(), ex);
		}

	}

	private Map<String, Object> doRequest(String method, String fullUrl, Object objToSend) throws IOException {
		String payload = toJson.apply(objToSend);

		log.info("Sending payload: " + payload);

		URL endpoint = new URL(fullUrl);
		HttpURLConnection connection = (HttpURLConnection) endpoint.openConnection();
		connection.setRequestMethod(method);
		connection.setRequestProperty("Accept", JSON);
		connection.setRequestProperty("Content-Type", JSON);
		if (POST.equals(method)) {
			connection.setDoOutput(true);
			connection.getOutputStream().write(payload.getBytes());
		}

		if (connection.getResponseCode() == HTTP_OK) {
			return (Map) new JsonSlurper().parse(connection.getInputStream());
		} else {
			throw new IOException("Connection attempt to " + fullUrl + " returned status code " + connection.getResponseCode());
		}
	}

	private void sendWebsocket(MessageDto objToSend) throws IOException {
		Map<String, Object> msgMap = new HashMap<>();
		msgMap.put("payload", objToSend);
		msgMap.put("notification", objToSend instanceof NotificationDto);
		String payload = toJson.apply(msgMap);

		log.info("Sending payload: " + payload);

		wsSocket.send(payload);
	}

	private ConnectionRequest buildConnectionRequest() {
		if (!isWebsocket() && (platformName == null || platformName.length() == 0)) {
			throw new RuntimeException("Platform is not set! You must set a platform before connecting.");
		}

		log.info("Building ConnectinRequest...");

		ConnectionRequest request = new ConnectionRequest();
		request.setDeviceId(deviceId);
		request.setDeviceToken(deviceToken);
		request.setUserId(userId);
		request.setUserName(userName);
		request.setPlatformName(getPlatformName());

		log.info("Done building ConnectionRequest: " + request);

		return request;
	}

	private void setData(Map<String, String> data) {
		deviceId = data.get("deviceId");
		deviceToken = data.get("deviceToken");
		userId = data.get("userId");
		userName = data.get("userName");
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getServerRefresh() {
		return serverRefresh;
	}

	public void setServerRefresh(int serverRefresh) {
		this.serverRefresh = serverRefresh;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getDeviceToken() {
		return deviceToken;
	}

	public void setDeviceToken(String deviceToken) {
		this.deviceToken = deviceToken;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPlatformName() {
		return isWebsocket() ? "websocket" : platformName;
	}

	public void setPlatformName(String platformName) {
		this.platformName = platformName;
	}

	public void setToJson(Function<Object, String> toJson) {
		this.toJson = toJson;
	}

	public boolean isWebsocket() {
		return websocket;
	}

	public void setWebsocket(boolean websocket) {
		this.websocket = websocket;
	}

	public boolean isConnected() {
		return connected;
	}

	public Object getResponse() {
		return response;
	}

	public void setWsMessageListener(Consumer<String> wsMessageListener) {
		this.wsMessageListener = wsMessageListener;
	}
}
