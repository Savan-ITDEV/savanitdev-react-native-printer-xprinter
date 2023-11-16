import { NativeModules, Platform } from "react-native";

const LINKING_ERROR =
  `The package 'react-native-awesome-library' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: "" }) +
  "- You rebuilt the app after installing the package\n" +
  "- You are not using Expo Go\n";

const AwesomeLibrary = NativeModules.AwesomeLibrary
  ? NativeModules.AwesomeLibrary
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export function onCreate() {
  return AwesomeLibrary.onCreate();
}
export function connectNet3(ip: string, img: string) {
  return AwesomeLibrary.connectNet3(ip, img, 576, 576);
}
export function disConnectNet() {
  return AwesomeLibrary.disConnectNet();
}
