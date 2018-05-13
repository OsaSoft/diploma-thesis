function randomElement(items) {
    return items[~~(items.length * Math.random())];
}

class MessageDto {
    constructor(targetGroups, targetUsers, targetDevices) {
        this.targetGroups = targetGroups;
        this.targetUsers = targetUsers;
        this.targetDevices = targetDevices;
    }
}

class NotificationDto extends MessageDto {
    constructor(title, body, targetGroups, targetUsers, targetDevices) {
        super(targetGroups, targetUsers, targetDevices);
        this.title = title;
        this.body = body;
    }
}

class DataMessageDto extends MessageDto {
    constructor(content, targetGroups, targetUsers, targetDevices) {
        super(targetGroups, targetUsers, targetDevices);
        this.content = content
    }
}

class ConnectionRequest {
    constructor(deviceId = null, deviceName = null, userId = null, userName = null) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.userId = userId;
        this.userName = userName;
    }
}

class MsgrClient {
    static get defaultUrl() {
        return "http://localhost:8080/wsConnect";
    }

    constructor() {
        this.connected = false;
    }

    init(url = MsgrClient.defaultUrl) {
        let request = new ConnectionRequest(this.deviceId, this.deviceToken, this.userId, this.userName);

        let restReq = new XMLHttpRequest();
        restReq.onload = e => this.onConnect(e);
        restReq.onerror = e => this.onError(e);
        restReq.open("POST", url, true);
        restReq.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
        restReq.setRequestHeader("Accept", "application/json");

        restReq.send(JSON.stringify(request));
    }

    onConnect(e) {
        let json = JSON.parse(e.target.response);
        let addresses = json.addresses;
        let data = json.deviceData;

        console.log(addresses);
        console.log(data);

        this.setDeviceData(data);

        //TODO connect to websocket
        // this.websocket = new WebSocket("http://" + randomElement(addresses) + "/ws/" + this.deviceId);

        this.connected = true;
        this._onSuccess();
    }

    onError(e) {
        console.log("Failed to initialize connection to server. Exception:");
        console.log(e);
        this.connected = false;
        this._onFailure();
    }

    setDeviceData(data) {
        this.deviceId = data.deviceId;
        this.deviceToken = data.deviceToken;
        this.userId = data.userId;
        this.userName = data.userName;
    }

    sendMessage(messageDto) {
        if (this.connected) {

        }
    }

    sendNotification(notificationDto) {
        if (this.connected) {

        }
    }

    set onFailure(onFailure) {
        this._onFailure = onFailure;
    }

    set onSuccess(onSuccessFunc) {
        this._onSuccess = onSuccessFunc;
    }

    set deviceId(deviceId) {
        this._deviceId = deviceId;
    }

    get deviceId() {
        return this._deviceId;
    }

    set deviceToken(deviceToken) {
        this._deviceToken = deviceToken;
    }

    get deviceToken() {
        return this._deviceToken;
    }

    set userId(userId) {
        this._userId = userId;
    }

    get userId() {
        return this._userId;
    }

    set userName(userName) {
        this._userName = userName;
    }

    get userName() {
        return this._userName;
    }
}

const MSGR = new MsgrClient();
