import { NativeModules, Platform } from "react-native";

const LINKING_ERROR =
  `The package '/savanitdev-react-native-printer-xprinter' doesn't seem to be linked. Make sure: \n\n` +
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

export function multiply(a: number, b: number): Promise<number> {
  return AwesomeLibrary.multiply(a, b);
}
export function onCreate() {
  return AwesomeLibrary.onCreate();
}
export function connectNet(ip: string) {
  return AwesomeLibrary.connectNet(ip);
}
export function printText() {
  return AwesomeLibrary.printText();
}
export function printImg(base64: string, contrast: number, brightness: number) {
  return AwesomeLibrary.printImg(base64, contrast, brightness);
}
export function disConnect() {
  return AwesomeLibrary.disConnectNet();
}
