package com.zachary.reactnative.leancloudsdk;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.PushService;
import com.avos.avoscloud.SaveCallback;
import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zachary on 2017/2/24.
 */

public class AvOsCloudPackage implements ReactPackage {
    private Context mContext;
    private Application mApplication;

    private RNPushNotificationHelper mRNPushNotificationHelper;

    public AvOsCloudPackage() {

    }

    public AvOsCloudPackage(Context context, Application application) {
        this.mContext = context;
        this.mApplication = application;
        this.initAVSDK(context);
    }

    private void initAVSDK(Context context) {
        Map map = this.getAppInfo();
        if(map != null && map.get("appId") != null && map.get("appKey") != null) {
            // 初始化应用信息
            String appId = map.get("appId").toString();
            String appKey = map.get("appKey").toString();
            AVOSCloud.initialize(context, appId, appKey);
            // 启用崩溃错误统计
            AVAnalytics.enableCrashReport(context.getApplicationContext(), true);
            AVOSCloud.setLastModifyEnabled(true);
            AVOSCloud.setDebugLogEnabled(true);

            //必须要设置默认的通知回调或者订阅一个频道，否则接收不到通知
            mRNPushNotificationHelper = new RNPushNotificationHelper(this.mApplication);
            PushService.setDefaultPushCallback(context.getApplicationContext(), mRNPushNotificationHelper.getMainActivityClass());
            //this.subscribeChannel("public");

            // 保存 installation 到服务器
            AVInstallation.getCurrentInstallation().saveInBackground(new SaveCallback() {
                public void done(AVException e) {
                    String installationId = AVInstallation.getCurrentInstallation().getInstallationId();
                    sendRegistrationToken(installationId);
                }
            });
        }
    }

    private void sendRegistrationToken(String token) {
        Intent intent = new Intent(this.mContext.getPackageName() + ".RNPushNotificationRegisteredToken");
        intent.putExtra("token", token);
        this.mContext.sendBroadcast(intent);
    }

    private Map getAppInfo() {
        Map map = new HashMap();
        try {
            ApplicationInfo appInfo = this.mContext.getPackageManager().getApplicationInfo(this.mContext.getPackageName(),
                    PackageManager.GET_META_DATA);
            String appId = appInfo.metaData.getString("leancloud.APP_ID");
            String appKey = appInfo.metaData.getString("leancloud.APP_KEY");
            map.put("appId", appId);
            map.put("appKey", appKey);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public List<NativeModule> createNativeModules(
            ReactApplicationContext reactContext) {
        List<NativeModule> modules = new ArrayList<>();
        modules.add(new RNPushNotificationModule(reactContext));
        return modules;
    }

    @Override
    public List<Class<? extends JavaScriptModule>> createJSModules() {
        return Collections.emptyList();
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }
}
