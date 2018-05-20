package cz.cvut.fel.hernaosc.dp.msgr.chattr;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cz.cvut.fel.hernaosc.dp.msgr.chattr.service.FcmService;
import cz.cvut.fel.hernaosc.dp.msgr.javaclient.MsgrClient;
import cz.cvut.fel.hernaosc.dp.msgr.messagecommon.dto.message.DataMessageDto;

public class MainActivity extends Activity {

    static {
        Logger.addLogAdapter(new AndroidLogAdapter());
    }

    private static String get(Bundle bundle, String key) {
        return bundle.getString("msgr." + key);
    }

    private BroadcastReceiver messageReceived = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.d("Received intent " + intent);

            Bundle content = intent.getExtras();
            TextView tv = findViewById(R.id.chatWindow);
            String text = tv.getText() + "\n" +
                    new SimpleDateFormat().format(new Date()) + " @" + get(content, "from") +
                    ": " + get(content, "text");

            runOnUiThread(() -> tv.setText(text));
        }
    };

    private MsgrClient msgrClient = new MsgrClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                messageReceived, new IntentFilter(FcmService.BROADCAST_ACTION));

        setContentView(R.layout.activity_main);

        Button msgButton = findViewById(R.id.msgButton);
        msgButton.setOnClickListener((view) -> {
            String targetUser = ((EditText) findViewById(R.id.targetUser)).getText().toString();
            String messageText = ((EditText) findViewById(R.id.messageBody)).getText().toString();

            List<String> targetUsers = new LinkedList<>();
            targetUsers.add(targetUser);
            if (!targetUser.equals(msgrClient.getUserId())) {
                targetUsers.add(msgrClient.getUserId());
            }

            Map<String, String> data = new HashMap<>();
            data.put("from", msgrClient.getUserName());
            data.put("text", messageText);

            DataMessageDto message = new DataMessageDto();
            message.setContent(data);
            message.setTargetUsers(targetUsers);

            AsyncTask.execute(() -> msgrClient.send(message));

            ((EditText) findViewById(R.id.messageBody)).setText("");
        });

        connectMsgr();
    }

    private void connectMsgr() {
        TextView tv1 = findViewById(R.id.statusText);
        tv1.setText("Chattr is connecting...");

        String firebaseToken = FirebaseInstanceId.getInstance().getToken();
        Logger.i("Got FCM token: " + firebaseToken);

        msgrClient.setUrl("http://192.168.1.2:8080");
        msgrClient.setPlatformName("FCM");
        msgrClient.setDeviceToken(firebaseToken);
        msgrClient.setUserName("oscar");
        msgrClient.setToJson(obj -> new Gson().toJson(obj));

        AsyncTask.execute(() -> {
            try {
                msgrClient.init();
                runOnUiThread(() -> tv1.setText(
                        "Chattr is connected.\n User: " + msgrClient.getUserName() + " (" + msgrClient.getUserId() + ")"
                ));
            } catch (IOException e) {
                Logger.e("Could not connect to " + msgrClient.getUrl() + ". Exception " + e.getMessage(), e);
                runOnUiThread(() -> tv1.setText("Chattr is disconnected (ERROR: " + e.getMessage() + ")"));
            }
        });
    }
}
