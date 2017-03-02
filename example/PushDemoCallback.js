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

export default class PushDemoCallback extends Component {

  constructor(props) {
    super(props)
  }

  componentWillMount() {

  }

  componentDidMount() {

  }

  componentWillUnmount() {

  }

  render() {
    return (
      <View style={styles.container}>
        <View style={{marginBottom:30}}>
          <Text>接收到的消息通知</Text>
        </View>
        <View style={{marginBottom:30}}>
          <Text>用户id:{this.props.userInfo.userId}</Text>
        </View>
        <View style={{marginBottom:30}}>
          <Text>用户名:{this.props.userInfo.userName}</Text>
        </View>
        <TouchableOpacity onPress={()=>{Actions.pop()}}>
          <Text>返回</Text>
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
  },

});

