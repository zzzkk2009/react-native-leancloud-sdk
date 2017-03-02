/**
 * Created by zachary on 2017/3/1.
 */
import React, { Component } from 'react';
import {Actions, Scene, Router} from 'react-native-router-flux';
import PushDemo from './PushDemo'
import PushDemoCallback from './PushDemoCallback'

const scenes = Actions.create(
  <Scene key="root">
    <Scene key="PushDemo" initial={true} component={PushDemo} title="PushDemo"/>
    <Scene key="PushDemoCallback" component={PushDemoCallback} title="PushDemoCallback"/>
  </Scene>
);


export default class App extends React.Component {
  render() {
    return <Router scenes={scenes}/>
  }
}