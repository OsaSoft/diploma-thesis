package cz.cvut.fel.hernaosc.dp.msgr.chattr;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import java.io.IOException;

import cz.cvut.fel.hernaosc.dp.msgr.javaclient.MsgrClient;

public class MainActivity extends Activity {

    static {
        Logger.addLogAdapter(new AndroidLogAdapter());
    }

    private BroadcastReceiver messageReceived = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            TextView tv = findViewById(R.id.textView2);
            String text = "Message title: " + intent.getExtras().getString("messageTitle") +
                    "\n\nMessage body: " + intent.getExtras().getString("messageBody");
            tv.setText(text);
        }
    };

    private MsgrClient msgrClient = new MsgrClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tv1 = findViewById(R.id.textView);
        tv1.setText("Chattr is connecting...");

        String firebaseToken = FirebaseInstanceId.getInstance().getToken();
        Logger.i("Got FCM token: " + firebaseToken);

//        registerReceiver(messageReceived, new IntentFilter(FcmService.BROADCAST_ACTION));
        msgrClient.setUrl("http://192.168.1.2:8080");
        msgrClient.setPlatformName("FCM");
        msgrClient.setDeviceToken(firebaseToken);
        msgrClient.setUserName("oscar");
        msgrClient.setToJson((obj) -> new Gson().toJson(obj));

        AsyncTask.execute(() -> {
            try {
                msgrClient.init();
                tv1.setText(
                        "Chattr is connected.\n User: " + msgrClient.getUserName() + " (" + msgrClient.getUserId() + ")"
                );
            } catch (IOException e) {
                Logger.e("Could not connect to " + msgrClient.getUrl() + ". Exception " + e.getMessage(), e);
                tv1.setText("Chattr is disconnected (ERROR: " + e.getMessage() + ")");
            }
        });
    }
}
