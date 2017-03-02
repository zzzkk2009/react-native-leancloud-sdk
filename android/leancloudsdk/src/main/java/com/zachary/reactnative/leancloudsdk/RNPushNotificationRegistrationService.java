package com.zachary.reactnative.leancloudsdk;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.PushService;
import com.avos.avoscloud.SaveCallback;

import static com.zachary.reactnative.leancloudsdk.RNPushNotificationModule.LOG_TAG;

/**
 * Created by zachary on 2017/2/25.
 */

public class RNPushNotificationRegistrationService extends IntentService {

    private static final String TAG = "RNPushNotification";
    private String leancloudAppId = "";
    private String leancloudAppKey = "";
    private RNPushNotificationHelper mRNPushNotificationHelper;

    public RNPushNotificationRegistrationService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            if(intent.hasExtra("leancloudAppId") && intent.hasExtra("leancloudAppKey")) {
                this.leancloudAppId = intent.getStringExtra("leancloudAppId");
                this.leancloudAppKey = intent.getStringExtra("leancloudAppKey");
                this.initAVSDK();
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, TAG + " failed to process intent " + intent, e);
        }
    }

    private void initAVSDK() {
        // We need to run this on the main thread, as the React code assumes that is true.
        // Namely, DevServerHelper constructs a Handler() without a Looper, which triggers:
        // "Can't create handler inside thread that has not called Looper.prepare()"
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                // 初始化应用信息
                AVOSCloud.initialize(getApplication().getApplicationContext(), leancloudAppId, leancloudAppKey);
                // 启用崩溃错误统计
                AVAnalytics.enableCrashReport(getApplication().getApplicationContext(), true);
                AVOSCloud.setLastModifyEnabled(true);
                AVOSCloud.setDebugLogEnabled(true);

                //必须要设置默认的通知回调或者订阅一个频道，否则接收不到通知
                mRNPushNotificationHelper = new RNPushNotificationHelper(getApplication());
                PushService.setDefaultPushCallback(getApplication().getApplicationContext(), mRNPushNotificationHelper.getMainActivityClass());
                //this.subscribeChannel("public");

                // 保存 installation 到服务器
                AVInstallation.getCurrentInstallation().saveInBackground(new SaveCallback() {
                    public void done(AVException e) {
                        String installationId = AVInstallation.getCurrentInstallation().getInstallationId();
                        sendRegistrationToken(installationId);
                    }
                });


            }
        });
    }

    private void sendRegistrationToken(String token) {
        Intent intent = new Intent(this.getPackageName() + ".RNPushNotificationRegisteredToken");
        intent.putExtra("token", token);
        sendBroadcast(intent);
    }
}
