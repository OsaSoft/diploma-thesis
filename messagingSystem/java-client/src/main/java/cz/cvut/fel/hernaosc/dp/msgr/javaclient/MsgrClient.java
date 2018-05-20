package cz.cvut.fel.hernaosc.dp.msgr.javaclient;

import cz.cvut.fel.hernaosc.dp.msgr.messagecommon.dto.ConnectionRequest;
import cz.cvut.fel.hernaosc.dp.msgr.messagecommon.dto.message.MessageDto;
import cz.cvut.fel.hernaosc.dp.msgr.messagecommon.dto.message.NotificationDto;
import cz.cvut.fel.hernaosc.dp.msgr.messagecommon.dto.util.MsgrMessageUtils;
import groovy.json.JsonBuilder;
import groovy.json.JsonSlurper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MsgrClient {
	private static Logger log = Logger.getLogger("MSGR-CLIENT");

	private static final String JSON = "application/json";
	private static final String POST = "POST";
	private static final int HTTP_OK = 200;

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

	public void init() throws IOException {

		log.info("Connecting to " + url);
		refreshNodes();

		if (addresses == null || addresses.isEmpty()) {
			log.log(Level.SEVERE, "Could not find any nodes to connect to");
			throw new IOException("Could not find any nodes to connect to");
		}
	}

	public boolean send(MessageDto message) {
		String type = message instanceof NotificationDto ? "notification" : "message";
		boolean sent = false;

		try {
			String address = getRandomAddress();
			address = "http://" + address + "/send/" + type;

			Object result = doRequest(POST, address, message);
			if (result != null) {
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
		String fullIUrl = url + "/connect";
		Map<String, Object> result = doRequest(POST, fullIUrl, buildConnectionRequest());
		if (result != null) {
			log.info("Received result " + result);

			addresses = (List<String>) result.get("addresses");
			lastUpdate = new Date().getTime();

			setData((Map) result.get("deviceData"));
			log.info("Received node IPs: " + addresses);
		} else {
			throw new IOException("Could not connect to " + url);
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

	private ConnectionRequest buildConnectionRequest() {
		if (platformName == null || platformName.length() == 0) {
			throw new RuntimeException("Platform is not set! You must set a platform before connecting.");
		}

		log.info("Building ConnectinRequest...");

		ConnectionRequest request = new ConnectionRequest();
		request.setDeviceId(deviceId);
		request.setDeviceToken(deviceToken);
		request.setUserId(userId);
		request.setUserName(userName);
		request.setPlatformName(platformName);

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
		return platformName;
	}

	public void setPlatformName(String platformName) {
		this.platformName = platformName;
	}

	public void setToJson(Function<Object, String> toJson) {
		this.toJson = toJson;
	}
}
