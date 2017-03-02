/**
 * Created by zachary on 2017/2/24.
 */

var {
  NativeModules,
  DeviceEventEmitter,
} = require('react-native');

var RNPushNotification = NativeModules.RNPushNotification;
var _notifHandlers = new Map();

var NOTIF_REGISTER_EVENT = 'remoteNotificationsRegistered';
var DEVICE_NOTIF_EVENT = 'remoteNotificationReceived';
var REMOTE_FETCH_EVENT = 'remoteFetch';

var NotificationsComponent = function() {

}

NotificationsComponent.prototype.getInitialNotification = function () {
  return RNPushNotification.getInitialNotification()
    .then(function (notification) {
      if (notification && notification.dataJSON) {
        return JSON.parse(notification.dataJSON);
      }
      return null;
    });
};

NotificationsComponent.prototype.requestPermissions = function(leancloudAppId: String, leancloudAppKey: String) {
  RNPushNotification.requestPermissions(leancloudAppId, leancloudAppKey);
};


NotificationsComponent.prototype.cancelLocalNotifications = function(details: Object) {
  RNPushNotification.cancelLocalNotifications(details);
};

NotificationsComponent.prototype.cancelAllLocalNotifications = function() {
  RNPushNotification.cancelAllLocalNotifications();
};

NotificationsComponent.prototype.presentLocalNotification = function(details: Object) {
  RNPushNotification.presentLocalNotification(details);
};

NotificationsComponent.prototype.scheduleLocalNotification = function(details: Object) {
  RNPushNotification.scheduleLocalNotification(details);
};

NotificationsComponent.prototype.addEventListener = function(type: String, handler: Function) {
  var listener;
  if(type === 'register') {
    // console.log('NotificationsComponent.addEventListener.register======')
    listener = DeviceEventEmitter.addListener(
      NOTIF_REGISTER_EVENT,
      function(registrationInfo) {
        // console.log('remoteNotificationsRegistered===registrationInfo===', registrationInfo)
        // console.log('remoteNotificationsRegistered===handler===', handler)
        handler(registrationInfo.deviceToken);
      }
    )
  }else if(type === 'notification') {
    listener =  DeviceEventEmitter.addListener(
      DEVICE_NOTIF_EVENT,
      function(notifData) {
        var data = JSON.parse(notifData.dataJSON);
        handler(data);
      }
    );
  }else if (type === 'remoteFetch') {
    listener = DeviceEventEmitter.addListener(
      REMOTE_FETCH_EVENT,
      function(notifData) {
        var notificationData = JSON.parse(notifData.dataJSON)
        handler(notificationData);
      }
    );
  }

  _notifHandlers.set(type, listener);
}

NotificationsComponent.prototype.removeEventListener = function(type: String, handler: Function) {
  var listener = _notifHandlers.get(type);
  if(!listener) {
    return;
  }
  listener.remove();
  _notifHandlers.delete(type);
}

NotificationsComponent.prototype.push = function(params: Object) {
  RNPushNotification.push(params);
};

NotificationsComponent.prototype.registerNotificationActions = function(details: Object) {
  RNPushNotification.registerNotificationActions(details);
}

NotificationsComponent.prototype.subscribe = function(channels: Object) {
  RNPushNotification.subscribe(channels);
}

NotificationsComponent.prototype.setApplicationIconBadgeNumber = function(number: number) {
  if (!RNPushNotification.setApplicationIconBadgeNumber) {
    return;
  }
  RNPushNotification.setApplicationIconBadgeNumber(number);
};

NotificationsComponent.prototype.abandonPermissions = function() {
  /* Void */
};

NotificationsComponent.prototype.checkPermissions = function(callback: Function) {
  /* Void */
};

module.exports = {
  state: false,
  component: new NotificationsComponent()
}