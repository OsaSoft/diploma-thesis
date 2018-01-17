package hernaosc.fel.cvut.cz.fcmtest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;

public class MainActivity extends AppCompatActivity {

    private BroadcastReceiver messageReceived= new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            TextView tv = (TextView) findViewById(R.id.textView2);
            String text = "Message title: " + intent.getExtras().getString("messageTitle") +
                    "\n\nMessage body: " + intent.getExtras().getString("messageBody");
            tv.setText(text);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tv1 = (TextView) findViewById(R.id.textView);
        tv1.setText("Firebase device id is: \n" + FirebaseInstanceId.getInstance().getToken());


        registerReceiver(messageReceived, new IntentFilter(FcmService.BROADCAST_ACTION));
    }
}
