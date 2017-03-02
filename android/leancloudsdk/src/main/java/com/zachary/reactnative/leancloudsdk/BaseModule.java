package com.zachary.reactnative.leancloudsdk;

import android.content.Context;
import android.support.annotation.Nullable;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

/**
 * Created by zachary on 2017/2/24.
 */

abstract public class BaseModule extends ReactContextBaseJavaModule {
    protected ReactApplicationContext mReactContext;
    protected Context mContext;

    public BaseModule(ReactApplicationContext reactContext) {
        super(reactContext);
        mReactContext = reactContext;
        mContext = reactContext.getApplicationContext();
    }

    /**
     *
     * @param eventName
     * @param params
     */
    protected void sendEvent(String eventName,@Nullable WritableMap params) {
        mReactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }
}
