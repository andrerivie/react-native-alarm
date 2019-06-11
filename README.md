# react-native-alarm

This project was forked from (a fork of) the abandoned react-native-alarm and refactored/updated to work with Android API 26+ (and below) and iOS. The use case is something like an alarm clock feature where you want the user to be able to mute other notifications/ringer and still wake device/notify/make a sound (using media volume) from your app at a specific time.

## Getting started

`$ npm install https://github.com/andrerivie/react-native-alarm --save`

### Mostly automatic installation

`$ react-native link react-native-alarm`

### Manual installation

#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-alarm` and add `RNAlarm.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNAlarm.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)

#### Android

Most of this should be handled by react-native link above, but verify the following:

1. In `android/app/src/main/java/[...]/MainActivity.java`
    - Add `import com.liang.RNAlarmPackage;` to the imports at the top of the file
2. In `android/app/src/main/java/[...]/MainApplication.java`
    - Add `new RNAlarmPackage()` to the list returned by the `getPackages()` method
3. Append the following lines to `android/settings.gradle`:
```
include ':react-native-alarm'
project(':react-native-alarm').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-alarm/android')
```
4. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
```
implementation project(':react-native-alarm')
```

## Usage
```javascript
RNAlarm.setAlarm(
  'Alarm fire time'  // string in unix epoch time
  'Notification Title',
  'custom_sound',  // a filename for a .mp3 that needs to be added to Android/iOS projects separately
  () => {
    // Success callback function
  },
  () => {
    // Fail callback function
  }
);
```
EXAMPLE:
```javascript
import RNAlarm from 'react-native-alarm';

RNAlarm.setAlarm(
  (Date.now() + 60000).toString(),  // alarm will fire in one minute (60000 milliseconds)
  'Timer has elapsed!',
  'my_custom_sound_file',   // leave this as empty string for default sound
  () => {
    console.log('Success!')
  },
  () => {
    console.log('Failure!')
  }
);
