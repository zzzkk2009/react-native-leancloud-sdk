/**
 * Created by zachary on 2017/2/24.
 */

'use strict';

var RNNotificationsComponent = require('./component');

var AppState = RNNotificationsComponent.state;
var RNNotifications = RNNotificationsComponent.component;

var Platform = require('react-native').Platform;

var Notifications = {
  handler: RNNotifications,
  onRegister: false,
  onError: false,
  onNotification: false,
  isLoaded: false,
  hasPoppedInitialNotification: false,

  isPermissionsRequestPending: false,

  permissions: {
    alert: true,
    badge: true,
    sound: true
  }
}

Notifications.callNative = function(name: String, params: Array) {
  if(typeof this.handler[name] === 'function') {
    if(typeof params !== 'array' && typeof params !== 'object') {
      params = []
    }
    // console.log('Notifications.callNative.handler[name]======', name)
    return this.handler[name](...params);
  }else {
    return null;
  }
}

Notifications.configure = function(options: Object) {
  if(typeof options.onRegister !== 'undefined') {
    this.onRegister = options.onRegister;
  }

  if ( typeof options.onError !== 'undefined' ) {
    this.onError = options.onError;
  }

  if ( typeof options.onNotification !== 'undefined' ) {
    this.onNotification = options.onNotification;
  }

  if ( typeof options.permissions !== 'undefined' ) {
    this.permissions = options.permissions;
  }

  if ( typeof options.leancloudAppId !== 'undefined' ) {
    this.leancloudAppId = options.leancloudAppId;
  }

  if ( typeof options.leancloudAppKey !== 'undefined' ) {
    this.leancloudAppKey = options.leancloudAppKey;
  }

  if ( typeof options.onRemoteFetch !== 'undefined' ) {
    this.onRemoteFetch = options.onRemoteFetch;
  }

  if(this.isLoaded === false) {
    this._onRegister = this._onRegister.bind(this);
    this._onNotification = this._onNotification.bind(this);
    this._onRemoteFetch = this._onRemoteFetch.bind(this);

    this.callNative('addEventListener', ['register', this._onRegister]);
    this.callNative('addEventListener', ['notification', this._onNotification ] );
    Platform.OS === 'android' ? this.callNative( 'addEventListener', [ 'remoteFetch', this._onRemoteFetch ] ) : null

    this.isLoaded = true;
  }

  if ( this.hasPoppedInitialNotification === false &&
    ( options.popInitialNotification === undefined || options.popInitialNotification === true ) ) {
    this.popInitialNotification(function(firstNotification) {
      if ( firstNotification !== null ) {
        this._onNotification(firstNotification, true);
      }
    }.bind(this));
    this.hasPoppedInitialNotification = true;
  }

  if( options.requestPermissions !== false) {
    this._requestPermissions();
  }

}

/* Unregister */
Notifications.unregister = function() {
  this.callNative( 'removeEventListener', [ 'register', this._onRegister ] )
  this.callNative( 'removeEventListener', [ 'notification', this._onNotification ] )
  this.callNative( 'removeEventListener', [ 'localNotification', this._onNotification ] )
  Platform.OS === 'android' ? this.callNative( 'removeEventListener', [ 'remoteFetch', this._onRemoteFetch ] ) : null
  this.isLoaded = false;
};

Notifications._onRegister = function(token: String) {
  if(this.onRegister !== false) {
    // console.log('Notifications._onRegister.onRegister==token====', token)
    this.onRegister({
      token: token,
      os: Platform.OS
    })
  }
}

/**
 * Local Notifications
 * @param {Object}		details
 * @param {String}		details.message - The message displayed in the notification alert.
 * @param {String}		details.title  -  ANDROID ONLY: The title displayed in the notification alert.
 * @param {String}		details.ticker -  ANDROID ONLY: The ticker displayed in the status bar.
 * @param {Object}		details.userInfo -  iOS ONLY: The userInfo used in the notification alert.
 */
Notifications.localNotification = function(details: Object) {
  if ( Platform.OS === 'ios' ) {
    // https://developer.apple.com/reference/uikit/uilocalnotification

    let soundName = details.soundName ? details.soundName : 'default'; // play sound (and vibrate) as default behaviour

    if (details.hasOwnProperty('playSound') && !details.playSound) {
      soundName = ''; // empty string results in no sound (and no vibration)
    }

    // for valid fields see: https://developer.apple.com/library/ios/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/Chapters/IPhoneOSClientImp.html
    // alertTitle only valid for apple watch: https://developer.apple.com/library/ios/documentation/iPhone/Reference/UILocalNotification_Class/#//apple_ref/occ/instp/UILocalNotification/alertTitle

    this.handler.presentLocalNotification({
      alertTitle: details.title,
      alertBody: details.message,
      alertAction: details.alertAction,
      category: details.category,
      soundName: soundName,
      applicationIconBadgeNumber: details.number,
      userInfo: details.userInfo
    });
  } else {
    this.handler.presentLocalNotification(details);
  }
};

/**
 * Local Notifications Schedule
 * @param {Object}		details (same as localNotification)
 * @param {Date}		details.date - The date and time when the system should deliver the notification
 */
Notifications.localNotificationSchedule = function(details: Object) {
  if ( Platform.OS === 'ios' ) {

    let soundName = details.soundName ? details.soundName : 'default'; // play sound (and vibrate) as default behaviour

    if (details.hasOwnProperty('playSound') && !details.playSound) {
      soundName = ''; // empty string results in no sound (and no vibration)
    }

    this.handler.scheduleLocalNotification({
      fireDate: details.date.toISOString(),
      alertBody: details.message,
      soundName: soundName,
      applicationIconBadgeNumber: parseInt(details.number, 10),
      userInfo: details.userInfo
    });
  } else {
    details.fireDate = details.date.getTime();
    delete details.date;
    this.handler.scheduleLocalNotification(details);
  }
};

/* onResultPermissionResult */
Notifications._onPermissionResult = function() {
  this.isPermissionsRequestPending = false;
};

// Prevent requestPermissions called twice if ios result is pending
Notifications._requestPermissions = function() {
  if ( Platform.OS === 'ios' ) {
    if ( this.isPermissionsRequestPending === false ) {
      this.isPermissionsRequestPending = true;
      return this.callNative( 'requestPermissions', [ this.permissions ])
        .then(this._onPermissionResult.bind(this))
        .catch(this._onPermissionResult.bind(this));
    }
  } else {
    // console.log('Notifications._requestPermissions==leancloudAppId====', this.leancloudAppId)
    // console.log('Notifications._requestPermissions==leancloudAppKey====', this.leancloudAppKey)
    return this.callNative( 'requestPermissions', [this.leancloudAppId,  this.leancloudAppKey]);
  }
};

// Stock requestPermissions function
Notifications.requestPermissions = function() {
  if ( Platform.OS === 'ios' ) {
    return this.callNative( 'requestPermissions', [ this.permissions ]);
  } else {
    return this.callNative( 'requestPermissions', [ ]);
  }
};

Notifications._onNotification = function(data, isFromBackground = null) {
  if ( isFromBackground === null ) {
    isFromBackground = (
      data.foreground === false ||
      AppState.currentState === 'background'
    );
  }

  if ( this.onNotification !== false ) {
    if ( Platform.OS === 'ios' ) {
      this.onNotification({
        foreground: ! isFromBackground,
        userInteraction: isFromBackground,
        message: data.getMessage(),
        data: data.getData(),
        badge: data.getBadgeCount(),
        alert: data.getAlert(),
        sound: data.getSound()
      });
    } else {
      var notificationData = {
        foreground: ! isFromBackground,
        ...data
      };

      if ( typeof notificationData.data === 'string' ) {
        try {
          notificationData.data = JSON.parse(notificationData.data);
        } catch(e) {
          /* void */
        }
      }

      this.onNotification(notificationData);
    }
  }
};

Notifications._onRemoteFetch = function(notificationData: Object) {
  if ( this.onRemoteFetch !== false ) {
    this.onRemoteFetch(notificationData)
  }
};

/* Fallback functions */
Notifications.presentLocalNotification = function() {
  return this.callNative('presentLocalNotification', arguments);
};

Notifications.cancelLocalNotifications = function() {
  return this.callNative('cancelLocalNotifications', arguments);
};

Notifications.cancelAllLocalNotifications = function() {
  return this.callNative('cancelAllLocalNotifications', arguments);
};

Notifications.push = function() {
  return this.callNative('push', arguments);
};

Notifications.registerNotificationActions = function() {
  return this.callNative('registerNotificationActions', arguments)
}

Notifications.subscribe = function() {
  return this.callNative('subscribe', arguments)
}

Notifications.popInitialNotification = function(handler) {
  this.callNative('getInitialNotification').then(function(result){
    handler(result);
  });
};

Notifications.setApplicationIconBadgeNumber = function() {
  return this.callNative('setApplicationIconBadgeNumber', arguments);
};

Notifications.getApplicationIconBadgeNumber = function() {
  return this.callNative('getApplicationIconBadgeNumber', arguments);
};

Notifications.abandonPermissions = function() {
  return this.callNative('abandonPermissions', arguments);
};

Notifications.checkPermissions = function() {
  return this.callNative('checkPermissions', arguments);
};

module.exports = Notifications;