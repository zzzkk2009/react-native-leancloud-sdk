# React Native Push Notifications
[![npm version](https://badge.fury.io/js/@zzzkk2009/react-native-leancloud-sdk.svg?update=6)](https://www.npmjs.com/package/@zzzkk2009/react-native-leancloud-sdk)
[![npm downloads](https://img.shields.io/npm/dm/@zzzkk2009/react-native-leancloud-sdkn.svg?update=6)](https://www.npmjs.com/package/@zzzkk2009/react-native-leancloud-sdk)

基于Leancloud的React Native Local and Remote Notifications for iOS and Android

## Supported React Native Versions
| Component Version     | RN Versions    | README     |
|-----------------------|---------------|------------|
| **1.1.0**          | **0.41**   | [Open](https://github.com/zzzkk2009/react-native-leancloud-sdk/blob/master/README.md)   |


## Installation
`npm install @zzzkk2009/react-native-leancloud-sdk --save`

`react-native link`

## Issues

Having a problem?  Read the [troubleshooting](./trouble-shooting.md) guide before raising an issue.


## iOS manual Installation
The component uses PushNotificationIOS for the iOS part.

[Please see: PushNotificationIOS](https://facebook.github.io/react-native/docs/pushnotificationios.html#content)

## Android manual Installation

```gradle
...

dependencies {
    ...

    compile project(':zzzkk2009-react-native-leancloud-sdk')
}
```


In `android/settings.gradle`
```gradle
...

include ':zzzkk2009-react-native-leancloud-sdk'
project(':zzzkk2009-react-native-leancloud-sdk').projectDir = file('../node_modules/@zzzkk2009/react-native-leancloud-sdk/android')
```

Manually register module in `MainApplication.java` (if you did not use `react-native link`):

```java
import com.zachary.reactnative.leancloudsdk.AvOsCloudPackage;  // <--- Import Package

public class MainApplication extends Application implements ReactApplication {

  private final ReactNativeHost mReactNativeHost = new ReactNativeHost(this) {
      @Override
      protected boolean getUseDeveloperSupport() {
        return BuildConfig.DEBUG;
      }

      @Override
      protected List<ReactPackage> getPackages() {

      return Arrays.<ReactPackage>asList(
          new MainReactPackage(),
          new AvOsCloudPackage() // <---- Add the Package
      );
    }
  };

  ....
}
```

## Usage
```javascript
var PushNotification = require('react-native-push-notification');

PushNotification.configure({

    // (required) ANDROID ONLY
    leancloudAppId: '',
    leancloudAppKey: '',

    // (optional) Called when Token is generated (iOS and Android)
    onRegister: function(token) {
        console.log( 'TOKEN:', token );
    },

    // (required) Called when a remote or local notification is opened or received
    onNotification: function(notification) {
        console.log( 'NOTIFICATION:', notification );
    },

    // IOS ONLY (optional): default: all - Permissions to register.
    permissions: {
        alert: true,
        badge: true,
        sound: true
    },

    // Should the initial notification be popped automatically
    // default: true
    popInitialNotification: true,

    /**
      * (optional) default: true
      * - Specified if permissions (ios) and token (android and ios) will requested or not,
      * - if not, you must call PushNotificationsHandler.requestPermissions() later
      */
    requestPermissions: true,
});
```

## Handling Notifications
When any notification is opened or received the callback `onNotification` is called passing an object with the notification data.

Notification object example:
```javascript
{
    foreground: false, // BOOLEAN: If the notification was received in foreground or not
    userInteraction: false, // BOOLEAN: If the notification was opened by the user from the notification area or not
    message: 'My Notification Message', // STRING: The notification message
    data: {}, // OBJECT: The push data
}
```

## Local Notifications
`PushNotification.localNotification(details: Object)`

EXAMPLE:
```javascript
PushNotification.localNotification({
    /* Android Only Properties */
    id: '0', // (optional) Valid unique 32 bit integer specified as string. default: Autogenerated Unique ID
    ticker: "My Notification Ticker", // (optional)
    autoCancel: true, // (optional) default: true
    largeIcon: "ic_launcher", // (optional) default: "ic_launcher"
    smallIcon: "ic_notification", // (optional) default: "ic_notification" with fallback for "ic_launcher"
    bigText: "My big text that will be shown when notification is expanded", // (optional) default: "message" prop
    subText: "This is a subText", // (optional) default: none
    color: "red", // (optional) default: system default
    vibrate: true, // (optional) default: true
    vibration: 300, // vibration length in milliseconds, ignored if vibrate=false, default: 1000
    tag: 'some_tag', // (optional) add tag to message
    group: "group", // (optional) add group to message
    ongoing: false, // (optional) set whether this is an "ongoing" notification

    /* iOS only properties */
    alertAction: // (optional) default: view
    category: // (optional) default: null
    userInfo: // (optional) default: null (object containing additional notification data)

    /* iOS and Android properties */
    title: "My Notification Title", // (optional, for iOS this is only used in apple watch, the title will be the app name on other iOS devices)
    message: "My Notification Message", // (required)
    playSound: false, // (optional) default: true
    soundName: 'default', // (optional) Sound to play when the notification is shown. Value of 'default' plays the default sound. It can be set to a custom sound such as 'android.resource://com.xyz/raw/my_sound'. It will look for the 'my_sound' audio file in 'res/raw' directory and play it. default: 'default' (default sound is played)
    number: '10', // (optional) Valid 32 bit integer specified as string. default: none (Cannot be zero)
    repeatType: 'day', // (Android only) Repeating interval. Could be one of `week`, `day`, `hour`, `minute, `time`. If specified as time, it should be accompanied by one more parameter 'repeatTime` which should the number of milliseconds between each interval
    actions: '["Yes", "No"]',  // (Android only) See the doc for notification actions to know more
});
```

## Scheduled Notifications
`PushNotification.localNotificationSchedule(details: Object)`

EXAMPLE:
```javascript
PushNotification.localNotificationSchedule({
  message: "My Notification Message", // (required)
  date: new Date(Date.now() + (60 * 1000)) // in 60 secs
});
```

## Custom sounds

In android, add your custom sound file to `[project_root]/android/app/src/main/res/raw`

In iOS, add your custom sound file to the project `Resources` in xCode.

In the location notification json specify the full file name:

    soundName: 'my_sound.mp3'

## Cancelling notifications

### 1) cancelLocalNotifications

`PushNotification.cancelLocalNotifications(details);` 

The the `details` parameter allows you to specify a `userInfo` dictionary that can be used to match one or more *scheduled* notifications.  Each
matched notification is cancelled and its alerts removed from the notification centre.  The RN docs suggest this is an optional parameter, but
it is not.

```javascript
PushNotification.cancelLocalNotifications({id: '123'});
```

### 2) cancelAllLocalNotifications

`PushNotification.cancelAllLocalNotifications()` 

Cancels all scheduled notifications AND clears the notifications alerts that are in the notification centre.

*NOTE: there is currently no api for removing specific notification alerts from the notification centre.*

## Repeating Notifications ##

(Android only) Specify `repeatType` and optionally `repeatTime` while scheduling the local notification. Check the local notification example above.

For iOS, the repeating notification should land soon. It has already been merged to the [master](https://github.com/facebook/react-native/pull/10337)

## Notification Actions ##

(Android only) [Refer](https://github.com/zo0r/react-native-push-notification/issues/151) to this issue to see an example of a notification action.

Two things are required to setup notification actions.

### 1) Specify notification actions for a notification
This is done by specifying an `actions` parameters while configuring the local notification. This is an array of strings where each string is a notificaiton action that will be presented with the notification.

For e.g. `actions: '["Accept", "Reject"]'  // Must be in string format` 
 
The array itself is specified in string format to circumvent some problems because of the way JSON arrays are handled by react-native android bridge.

### 2) Specify handlers for the notification actions
For each action specified in the `actions` field, we need to add a handler that is called when the user clicks on the action. This can be done in the `componentWillMount` of your main app file or in a separate file which is imported in your main app file. Notification actions handlers can be configured as below:

```
import PushNotificationAndroid from 'react-native-push-notification'

(function() {
  // Register all the valid actions for notifications here and add the action handler for each action
  PushNotificationAndroid.registerNotificationActions(['Accept','Reject','Yes','No']);
  DeviceEventEmitter.addListener('notificationActionReceived', function(action){
    console.log ('Notification action received: ' + action);
    const info = JSON.parse(action.dataJSON);
    if (info.action == 'Accept') {
      // Do work pertaining to Accept action here
    } else if (info.action == 'Reject') {
      // Do work pertaining to Reject action here
    }
    // Add all the required actions handlers
  });
})();
```

For iOS, you can use this [package](https://github.com/holmesal/react-native-ios-notification-actions) to add notification actions.

## Set application badge icon

`PushNotification.setApplicationIconBadgeNumber(number: number)` 

Works natively in iOS.

Uses the [ShortcutBadger](https://github.com/leolin310148/ShortcutBadger) on Android, and as such will not work on all Android devices.

## Sending Notification Data From Server
Same parameters as `PushNotification.localNotification()`

## iOS Only Methods
`PushNotification.checkPermissions(callback: Function)` Check permissions

`PushNotification.getApplicationIconBadgeNumber(callback: Function)` get badge number

`PushNotification.abandonPermissions()` Abandon permissions
