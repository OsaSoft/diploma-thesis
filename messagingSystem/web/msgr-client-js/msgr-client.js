function randomElement(items) {
    return items[~~(items.length * Math.random())];
}

const WS_CONNECTED = 1;
const WS_RECEIVED = 2;
const WS_ERROR_SEND = 10;
const WS_BAD_MESSAGE = 11;

class MessageDto {
    constructor(targetGroups, targetUsers, targetDevices, senderDeviceId, senderUsername) {
        this.targetGroups = targetGroups;
        this.targetUsers = targetUsers;
        this.targetDevices = targetDevices;
        this.senderDeviceId = senderDeviceId;
        this.senderUsername = senderUsername;
    }
}

class NotificationDto extends MessageDto {
    constructor(title, body, targetGroups, targetUsers, targetDevices, senderDeviceId, senderUsername) {
        super(targetGroups, targetUsers, targetDevices, senderDeviceId, senderUsername);
        this.title = title;
        this.body = body;
    }
}

class DataMessageDto extends MessageDto {
    constructor(content, targetGroups, targetUsers, targetDevices, senderDeviceId, senderUsername) {
        super(targetGroups, targetUsers, targetDevices, senderDeviceId, senderUsername);
        this.content = content;
    }
}

class ConnectionRequest {
    constructor(deviceId = null, deviceName = null, userId = null, userName = null) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.userId = userId;
        this.userName = userName;
        this.platformName = "websocket";
    }
}

class MsgrClient {
    static get defaultUrl() {
        return "http://localhost:8080/connect";
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

    disconnect() {
        if(this.connected) {
            this.websocket.close();
        }
    }

    onConnect(e) {
        let json = JSON.parse(e.target.response);
        let addresses = json.addresses;
        let data = json.deviceData;

        console.log(addresses);
        console.log(data);

        this.setDeviceData(data);

        this.nodeAddress = randomElement(addresses);
        this.websocket = new WebSocket("ws://" + this.nodeAddress + "/ws/" + this.deviceId);

        this.websocket.onopen = e => {
            this.connected = true;
        };

        this.websocket.onclose = e => {
            console.log("Connection was closed.");
            console.log(e);

            this.connected = false;
            this.onClose();
        };

        this.websocket.onerror = e => {
            console.log("Websocket error!");
            console.log(e);

            this.connected = false;
            this.onFailure(e);
        };

        this.websocket.onmessage = e => this.onMessage(e);
    }

    onMessage(e) {
        let data = JSON.parse(e.data);
        console.log("Received message:");
        console.log(data);

        switch (data.code) {
            case WS_CONNECTED:
                this.onSuccess();
                break;
            case WS_RECEIVED:
                if (data.notification) {
                    this.onNotification(data.title, data.body);
                } else {
                    this.onDataMessage(data.payload);
                }
                break;
            case WS_BAD_MESSAGE:
                console.log("Message was sent in bad format");
                break;
            case WS_ERROR_SEND:
                console.log("An error occured while sending the message");
                break;
            default:
                console.log("Unknown ws message type " + data.code);
                break;
        }
    }

    onError(e) {
        console.log("Failed to initialize connection to server. Exception:");
        console.log(e);
        this.connected = false;
        this.onFailure(e);
    }

    setDeviceData(data) {
        this.deviceId = data.deviceId;
        this.deviceToken = data.deviceToken;
        this.userId = data.userId;
        this.userName = data.userName;
    }

    send(messageDto) {
        if (this.connected) {
            let message = {payload: messageDto};
            message.notification = messageDto instanceof NotificationDto;

            this.websocket.send(JSON.stringify(message));
            return true;
        } else {
            return false;
        }
    }
}

const MSGR = new MsgrClient();
