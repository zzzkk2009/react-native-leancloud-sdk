/**
 * Created by zachary on 2017/2/24.
 */

import React, { Component } from 'react';
import {
  StyleSheet,
  Text,
  View,
  TouchableOpacity,
  DeviceEventEmitter
} from 'react-native';
import {Actions} from 'react-native-router-flux';

import AV from 'leancloud-storage'
import PushNotification from '@zzzkk2009/react-native-leancloud-sdk'

const KM_Dev = {
  appId: 'K5Rltwmfnxd5pYjMsOFFL0kT-gzGzoHsz',
  appKey: 'UseC5jvqLT7TIiQWI8nRPmEl',
}

const KM_PRO = {
  appId: 'K5Rltwmfnxd5pYjMsOFFL0kT-gzGzoHsz',
  appKey: 'UseC5jvqLT7TIiQWI8nRPmEl',
}
//AV.setProduction(false)
AV.init(
  __DEV__ ? KM_Dev : KM_PRO
)
// var Installation = require('leancloud-installation')(AV);

export default class PushDemo extends Component {

  constructor(props) {
    super(props)
    this.state = {
      deviceToken: ''
    }
  }

  componentWillMount() {
    const that = this
    PushNotification.configure({

      leancloudAppId: KM_Dev.appId,
      leancloudAppKey: KM_Dev.appKey,

      // (optional) Called when Token is generated (iOS and Android)
      onRegister: function(data) {
        console.log( 'DATA:', data );
        that.setState({
          deviceToken: data.token
        })
      },

      // (required) Called when a remote or local notification is opened or received
      onNotification: function(notification) {
        console.log( 'NOTIFICATION:', notification );
        console.log('DATA:', notification.data)
        if(notification.foreground) {//应用程序在前台
          Actions.PushDemoCallback({
            userInfo: notification.data.userInfo
          })
        }else {
          if(notification.userInteraction) {
            //用户点击通知栏消息
            Actions.PushDemoCallback()
          }else {
            //程序接收到远程或本地通知

          }
        }
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
    })

    // PushNotification.subscribe(['private']) //订阅频道,

    //注册本地通知action事件
    // PushNotification.registerNotificationActions(['action1', 'action2'])
    // DeviceEventEmitter.addListener('notificationActionReceived', function(action){
    //   console.log ('Notification action received: ', action);
    //   const info = JSON.parse(action.dataJSON);
    //   if (info.action == 'action1') {
    //     console.log('action1')
    //   } else if (info.action == 'action2') {
    //     console.log('action2')
    //   }
    //   // Add all the required actions handlers
    // });


  }

  componentDidMount() {

  }

  componentWillUnmount() {

  }

  push() {
    console.log('this.state.deviceToken===', this.state.deviceToken)
    var query = new AV.Query('_Installation');
    query.equalTo('installationId', this.state.deviceToken);
    AV.Push.send({
      // channels: ['private'], //注册了频道后,可通过设置频道进行推送
      where: query,
      data: {
        action: 'com.zachary.leancloud.push.action', //自定义推送,不需要设置频道
        alert: '您有新的订单,请及时处理',
        title: '邻家优店发来的通知',
        userInfo: {
          userId: 1,
          userName: 'zachary'
        }
      }
    });


    //发送本地通知
    // PushNotification.localNotification({
    //   message: 'hello localNotification',
    //   actions: '["action1", "action2"]' //通知按钮,下拉通知出现
    // })
  }

  render() {
    return (
      <View style={styles.container}>
        <TouchableOpacity style={{marginBottom:30}} onPress={()=>{this.push()}}>
          <Text>推送</Text>
        </TouchableOpacity>

        <TouchableOpacity onPress={()=>{Actions.PushDemoCallback()}}>
          <Text>goto PushDemoCallback</Text>
        </TouchableOpacity>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  }
});

