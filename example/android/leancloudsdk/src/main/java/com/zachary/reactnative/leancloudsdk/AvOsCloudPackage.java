package com.zachary.reactnative.leancloudsdk;

import android.content.Context;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by zachary on 2017/2/24.
 */

public class AvOsCloudPackage implements ReactPackage {
    private Context mContext;

    public AvOsCloudPackage(Context context) {
        this.mContext = context;
//        this.initAVSDK(context);
    }

//    private void initAVSDK(Context context) {
//        Map map = this.getAppInfo();
//        if(map != null && map.get("appId") != null) {
//            // 初始化应用信息
//            AVOSCloud.initialize(context, map.get("appId").toString(), map.get("appKey").toString());
//            // 启用崩溃错误统计
//            AVAnalytics.enableCrashReport(context.getApplicationContext(), true);
//            AVOSCloud.setLastModifyEnabled(true);
//            AVOSCloud.setDebugLogEnabled(true);
//        }
//    }
//
//    private Map getAppInfo() {
//        Map map = new HashMap();
//        try {
//            ApplicationInfo appInfo = this.mContext.getPackageManager().getApplicationInfo(this.mContext.getPackageName(),
//                    PackageManager.GET_META_DATA);
//            String appId = appInfo.metaData.getString("leancloud.APP_ID");
//            String appKey = appInfo.metaData.getString("leancloud.APP_KEY");
//            map.put("appId", appId);
//            map.put("appKey", appKey);
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//        return map;
//    }

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
