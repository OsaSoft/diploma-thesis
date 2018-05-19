package cz.cvut.fel.hernaosc.dp.msgr.chattr.service;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import java.util.Map;

/**
 * Created by Osa-S on 17.05.2018.
 */

public class FcmService extends FirebaseMessagingService {
    public static final String BROADCAST_ACTION = "fcmMessageAction";

    static {
        Logger.addLogAdapter(new AndroidLogAdapter());
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Logger.d("Received message from: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Logger.d("Message data payload: " + remoteMessage.getData());
            sendMessageToActivity(remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Logger.d("Message Notification Title: " + remoteMessage.getNotification().getTitle() +
                    "\nBody: " + remoteMessage.getNotification().getBody());
        }
    }

    private void sendMessageToActivity(Map<String, String> data) {
        Intent intent = new Intent(BROADCAST_ACTION);
        data.forEach((key, val) -> {
            intent.putExtra(key, val);
        });

        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }
}
