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

export function pingPinter(ip: string): Promise<boolean> {
  return AwesomeLibrary.pingPinter(ip);
}
export function onCreate() {
  return AwesomeLibrary.onCreate();
}
export function connectNet(ip: string) {
  return AwesomeLibrary.connectNet(ip);
}
export function printBitmap(img: string, w1: number, w2: number) {
  return AwesomeLibrary.printBitmap(img, w1, w2);
}
export function disConnectNet() {
  return AwesomeLibrary.disConnectNet();
}
