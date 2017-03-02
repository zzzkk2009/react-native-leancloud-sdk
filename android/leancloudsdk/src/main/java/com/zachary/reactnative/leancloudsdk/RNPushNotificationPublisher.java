package com.zachary.reactnative.leancloudsdk;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static com.zachary.reactnative.leancloudsdk.RNPushNotificationModule.LOG_TAG;

/**
 * Created by zachary on 2017/2/28.
 */

public class RNPushNotificationPublisher extends BroadcastReceiver {

    final static String NOTIFICATION_ID = "notificationId";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            int id = intent.getIntExtra(NOTIFICATION_ID, 0);
            long currentTime = System.currentTimeMillis();

            Log.i(LOG_TAG, "NotificationPublisher: Prepare To Publish: " + id + ", Now Time: " + currentTime);

            Application applicationContext = (Application) context.getApplicationContext();

            new RNPushNotificationHelper(applicationContext)
                    .sendToNotificationCentre(intent.getExtras());
        } catch (Exception e) {

        }
    }
}
