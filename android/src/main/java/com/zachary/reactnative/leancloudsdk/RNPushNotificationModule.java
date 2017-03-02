package com.zachary.reactnative.leancloudsdk;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVPush;
import com.avos.avoscloud.PushService;
import com.avos.avoscloud.SendCallback;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by zachary on 2017/2/24.
 */

public class RNPushNotificationModule extends BaseModule implements ActivityEventListener {
    public static final String LOG_TAG = "RNPushNotification";// all logging should use this tag

    private RNPushNotificationHelper mRNPushNotificationHelper;
    private final Random mRandomNumberGenerator = new Random(System.currentTimeMillis());
    private RNPushNotificationJsDelivery mJsDelivery;

    RNPushNotificationModule(ReactApplicationContext reactContext) {
        super(reactContext);

        reactContext.addActivityEventListener(this);

        Application applicationContext = (Application) reactContext.getApplicationContext();
        // The @ReactNative methods use this
        mRNPushNotificationHelper = new RNPushNotificationHelper(applicationContext);
        // This is used to delivery callbacks to JS
        mJsDelivery = new RNPushNotificationJsDelivery(reactContext);

        registerNotificationsRegistration();

//        //必须要设置默认的通知回调或者订阅一个频道，否则接收不到通知
//        PushService.setDefaultPushCallback(reactContext.getApplicationContext(), mRNPushNotificationHelper.getMainActivityClass());
//        //this.subscribeChannel("public");
    }

    @Override
    public String getName() {
        return "RNPushNotification";
    }

    private void registerNotificationsRegistration() {
        IntentFilter intentFilter = new IntentFilter(getReactApplicationContext().getPackageName() + ".RNPushNotificationRegisteredToken");

        getReactApplicationContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String token = intent.getStringExtra("token");
                WritableMap params = Arguments.createMap();
                params.putString("deviceToken", token);
                mJsDelivery.sendEvent("remoteNotificationsRegistered", params);
            }
        }, intentFilter);
    }

    private void registerNotificationsReceiveNotificationActions(ReadableArray actions) {
        IntentFilter intentFilter = new IntentFilter();
        // Add filter for each actions.
        for (int i = 0; i < actions.size(); i++) {
            String action = actions.getString(i);
            intentFilter.addAction(getReactApplicationContext().getPackageName() + "." + action);
        }
        getReactApplicationContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getBundleExtra("notification");

                // Notify the action.
                mJsDelivery.notifyNotificationAction(bundle);

                // Dismiss the notification popup.
                NotificationManager manager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
                int notificationID = Integer.parseInt(bundle.getString("id"));
                manager.cancel(notificationID);
            }
        }, intentFilter);
    }

    @ReactMethod
    public void registerNotificationActions(ReadableArray actions) {
        registerNotificationsReceiveNotificationActions(actions);
    }

    @ReactMethod
    public void subscribe(ReadableArray channels) {
        if(channels != null && channels.size() > 0) {
            for(int i = 0; i < channels.size(); i++) {
                String channel = channels.getString(i);
                this.subscribeChannel(channel);
            }
        }
    }

    private void subscribeChannel(String channel) {
        PushService.subscribe(AVOSCloud.applicationContext, channel, mRNPushNotificationHelper.getMainActivityClass());
    }

    @ReactMethod
    public void push(ReadableMap params) {
        AVPush push = new AVPush();
        // 设置频道
//        push.setChannel(params.getString("channel"));
        // 设置消息
        push.setMessage(params.getString("message"));
        push.setPushToAndroid(true);
        // 设置查询条件，只推送给自己，不要打扰别人啦，这是 demo
//        push.setQuery(AVInstallation.getQuery().whereEqualTo("installationId",
//                AVInstallation.getCurrentInstallation().getInstallationId()));
        // 推送
        push.sendInBackground(new SendCallback() {
            @Override
            public void done(AVException e) {
                Log.i(LOG_TAG, "发送成功");
            }
        });
    }

    @ReactMethod
    public void requestPermissions(String leancloudAppId, String leancloudAppKey) {
        ReactContext reactContext = getReactApplicationContext();
        Intent AVService = new Intent(reactContext, RNPushNotificationRegistrationService.class);
        AVService.putExtra("leancloudAppId", leancloudAppId);
        AVService.putExtra("leancloudAppKey", leancloudAppKey);
        reactContext.startService(AVService);
    }

    @ReactMethod
    public void presentLocalNotification(ReadableMap details) {
        Bundle bundle = Arguments.toBundle(details);
        // If notification ID is not provided by the user, generate one at random
        if (bundle.getString("id") == null) {
            bundle.putString("id", String.valueOf(mRandomNumberGenerator.nextInt()));
        }
        mRNPushNotificationHelper.sendToNotificationCentre(bundle);
    }

    @ReactMethod
    public void scheduleLocalNotification(ReadableMap details) {
        Bundle bundle = Arguments.toBundle(details);
        // If notification ID is not provided by the user, generate one at random
        if (bundle.getString("id") == null) {
            bundle.putString("id", String.valueOf(mRandomNumberGenerator.nextInt()));
        }
        mRNPushNotificationHelper.sendNotificationScheduled(bundle);
    }

    @ReactMethod
    public void getInitialNotification(Promise promise) {
        WritableMap params = Arguments.createMap();
        Activity activity = getCurrentActivity();
        if (activity != null) {
            Intent intent = activity.getIntent();
            Bundle bundle = intent.getBundleExtra("notification");
            if (bundle != null) {
                bundle.putBoolean("foreground", false);
                String bundleString = mJsDelivery.convertJSON(bundle);
                params.putString("dataJSON", bundleString);
            }
        }
        promise.resolve(params);
    }

    @ReactMethod
    /**
     * Cancels all scheduled local notifications, and removes all entries from the notification
     * centre.
     *
     * We're attempting to keep feature parity with the RN iOS implementation in
     * <a href="https://github.com/facebook/react-native/blob/master/Libraries/PushNotificationIOS/RCTPushNotificationManager.m#L289">RCTPushNotificationManager</a>.
     *
     * @see <a href="https://facebook.github.io/react-native/docs/pushnotificationios.html">RN docs</a>
     */
    public void cancelAllLocalNotifications() {
        mRNPushNotificationHelper.cancelAllScheduledNotifications();
        mRNPushNotificationHelper.clearNotifications();
    }

    @ReactMethod
    /**
     * Cancel scheduled notifications, and removes notifications from the notification centre.
     *
     * Note - as we are trying to achieve feature parity with iOS, this method cannot be used
     * to remove specific alerts from the notification centre.
     *
     * @see <a href="https://facebook.github.io/react-native/docs/pushnotificationios.html">RN docs</a>
     */
    public void cancelLocalNotifications(ReadableMap userInfo) {
        mRNPushNotificationHelper.cancelScheduledNotification(userInfo);
    }

    @ReactMethod
    public void setApplicationIconBadgeNumber(int number) {
        ApplicationBadgeHelper.INSTANCE.setApplicationIconBadgeNumber(getReactApplicationContext(), number);
    }


    // removed @Override temporarily just to get it working on different versions of RN
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        onActivityResult(requestCode, resultCode, data);
    }

    // removed @Override temporarily just to get it working on different versions of RN
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Ignored, required to implement ActivityEventListener for RN 0.33
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();

        return constants;
    }

    public void onNewIntent(Intent intent) {
        if (intent.hasExtra("notification")) {//用户点击通知栏消息
            Bundle bundle = intent.getBundleExtra("notification");
            bundle.putBoolean("foreground", false);
            intent.putExtra("notification", bundle);
            mJsDelivery.notifyNotification(bundle);
        }
    }


}
