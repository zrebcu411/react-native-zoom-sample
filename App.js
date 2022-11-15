/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow strict-local
 */

import React from 'react';
import type {Node} from 'react';
import {
  SafeAreaView,
  ScrollView,
  StatusBar,
  Text,
  StyleSheet,
  View,
  useColorScheme,
  Button,
  NativeModules,
  TextInput,
} from 'react-native';

import {Colors} from 'react-native/Libraries/NewAppScreen';

const App: () => Node = () => {
  const [jwtToken, setJwtToken] = React.useState('');
  const [appKey, setAppKey] = React.useState('');
  const [appSecret, setAppSecret] = React.useState('');
  const [meetingNumber, setMeetingNumber] = React.useState('');
  const [passcode, setPasscode] = React.useState('');
  const [loading, setLoading] = React.useState(false);
  const [error, setError] = React.useState('');

  const isDarkMode = useColorScheme() === 'dark';

  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
    flex: 1,
  };

  const handlePress = async () => {
    setError(false);
    setLoading(true);

    try {
      await NativeModules.ZoomSDK.initialize({
        appKey,
        appSecret,
        meetingNumber,
        jwtToken,
        passcode,
      });

      setLoading(false);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <SafeAreaView style={backgroundStyle}>
      <StatusBar
        barStyle={isDarkMode ? 'light-content' : 'dark-content'}
        backgroundColor={backgroundStyle.backgroundColor}
      />
      <ScrollView
        contentInsetAdjustmentBehavior="automatic"
        contentContainerStyle={styles.content}
        style={backgroundStyle}>
        <Text style={styles.text}>Provide either</Text>
        <TextInput
          style={styles.input}
          value={appKey}
          onChangeText={setAppKey}
          placeholder="App key"
        />
        <TextInput
          style={styles.input}
          value={appSecret}
          onChangeText={setAppSecret}
          placeholder="App secret"
        />
        <Text style={styles.text}>or</Text>
        <TextInput
          style={styles.input}
          value={jwtToken}
          onChangeText={setJwtToken}
          placeholder="JWT Token"
        />
        <Text style={styles.text}>and</Text>
        <TextInput
          style={styles.input}
          value={meetingNumber}
          onChangeText={setMeetingNumber}
          placeholder="Meeting ID"
        />
        <TextInput
          style={styles.input}
          value={passcode}
          onChangeText={setPasscode}
          placeholder="Meeting passcode"
        />
        {!!error && <Text style={styles.error}>{error}</Text>}
        <Button
          title={loading ? 'Starting...' : 'Start meeting'}
          disabled={loading}
          onPress={handlePress}
        />
      </ScrollView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  content: {
    justifyContent: 'center',
    flex: 1,
    padding: 20,
  },
  text: {
    marginBottom: 15,
  },
  error: {
    color: 'red',
    marginBottom: 15,
  },
  input: {
    backgroundColor: '#444444',
    marginBottom: 20,
  },
  sectionContainer: {
    marginTop: 32,
    paddingHorizontal: 24,
  },
  sectionTitle: {
    fontSize: 24,
    fontWeight: '600',
  },
  sectionDescription: {
    marginTop: 8,
    fontSize: 18,
    fontWeight: '400',
  },
  highlight: {
    fontWeight: '700',
  },
});

export default App;
