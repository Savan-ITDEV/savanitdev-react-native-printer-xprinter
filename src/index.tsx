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
export function connectNet(ip: string): Promise<boolean> {
  return AwesomeLibrary.connectNet(ip);
}
export function printImg(
  img: string,
  w1: number,
  w2: number
): Promise<boolean> {
  return AwesomeLibrary.printImg(img, w1, w2);
}
export async function connectNetImg(
  ip: string,
  img: string,
  w1: number,
  w2: number
) {
  return await AwesomeLibrary.connectNetImg(ip, img, w1, w2);
}
export function printBitmap(
  img: string,
  w1: number,
  w2: number
): Promise<string> {
  return AwesomeLibrary.printBitmap(img, w1, w2);
}
export function disConnectNet(): Promise<boolean> {
  return AwesomeLibrary.disConnectNet();
}
export function printText(): Promise<boolean> {
  return AwesomeLibrary.printText();
}

export const printPOS = {
  printText: function () {
    return AwesomeLibrary.printText();
  },
  initializeText: function () {
    return AwesomeLibrary.initializeText();
  },
  cut: function () {
    return AwesomeLibrary.cut();
  },
  clearPaper: function (): Promise<boolean> {
    return AwesomeLibrary.clearPaper();
  },
  printAndFeedLine: function () {
    return AwesomeLibrary.printAndFeedLine();
  },
  CancelChineseCharModel: function () {
    return AwesomeLibrary.CancelChineseCharModel();
  },
  selectAlignment: function selectAlignment(align: string) {
    let num = 1;
    if (align === "center") {
      num = 1;
    }
    if (align === "right") {
      num = 2;
    }
    if (align === "left") {
      num = 0;
    }
    return AwesomeLibrary.selectAlignment(num);
  },

  text: function (text: string, charset: string) {
    return AwesomeLibrary.text(text, charset);
  },
  selectCharacterSize: function (size: number) {
    return AwesomeLibrary.selectCharacterSize(size);
  },
  selectOrCancelBoldModel: function (size: number) {
    return AwesomeLibrary.selectOrCancelBoldModel(size);
  },
  selectCharacterCodePage: function (hex: number) {
    return AwesomeLibrary.selectCharacterCodePage(hex);
  },
  selectInternationalCharacterSets: function (hex: number) {
    return AwesomeLibrary.selectInternationalCharacterSets(hex);
  },
  setAbsolutePrintPosition: function (n: number, m: number) {
    return AwesomeLibrary.setAbsolutePrintPosition(n, m);
  },
};
