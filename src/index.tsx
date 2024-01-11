import {
  NativeModules,
  Platform,
  PermissionsAndroid,
  Alert,
  Linking,
} from "react-native";

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
  handleAndroidPermissions();
  return AwesomeLibrary.onCreate();
}
export function findAvailableDevice() {
  const arr = [];
  return AwesomeLibrary.findAvailableDevice()
    .then(
      (bluetoothDevices: {
        [x: string]: any;
        hasOwnProperty: (arg0: string) => any;
      }) => {
        for (const deviceName in bluetoothDevices) {
          if (bluetoothDevices.hasOwnProperty(deviceName)) {
            const deviceAddress = bluetoothDevices[deviceName];
            arr.push({ name: deviceName, deviceAddress: deviceAddress });
            // console.log(
            //   `Device Name: ${deviceName}, Device Address: ${deviceAddress}`
            // );
            // Do something with the deviceName and deviceAddress
          }
        }
        // console.log("Lists device found : ", arr);
        return arr;
      }
    )
    .catch(async (e: any) => {
      const result = await PermissionsAndroid.check(
        PermissionsAndroid.PERMISSIONS.BLUETOOTH_SCAN
      );
      console.debug("error => ", e);

      if (result) {
        return [];
      } else {
        Alert.alert(
          "แจ้งเตือนระบบ",
          "หากต้องการเชื่อมต่อเครื่องพิมพ์แบบบลูทูธ กรุณาเปิดใช้บลูทูธ!!",
          [
            {
              text: "ปิด",
              onPress: () => console.log("Cancel Pressed"),
              style: "cancel",
            },
            {
              text: "ไปหน้าตั้งค่า",
              onPress: () => {
                Linking.sendIntent("android.settings.BLUETOOTH_SETTINGS");
              },
            },
          ]
        );

        return [];
      }
    });
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
export function connectBT(macAddress: string): Promise<string> {
  return AwesomeLibrary.connectBT(macAddress);
}
export function printBitmap(
  img: string,
  w1: number,
  w2: number
): Promise<string> {
  return AwesomeLibrary.printBitmap(img, w1, w2);
}

export function printBitmapBLE(
  img: string,
  w1: number,
  w2: number,
  isBLE: number
): Promise<string> {
  return AwesomeLibrary.printBitmapBLE(img, w1, w2, isBLE);
}
export function disConnectNet(): Promise<boolean> {
  return AwesomeLibrary.disConnectNet();
}
export function printText(): Promise<boolean> {
  return AwesomeLibrary.printText();
}
export function printRawData(encode: string): Promise<string> {
  return AwesomeLibrary.printRawData(encode);
}
export const handleAndroidPermissions = () => {
  try {
    if (Platform.OS === "android" && Platform.Version >= 31) {
      PermissionsAndroid.requestMultiple([
        PermissionsAndroid.PERMISSIONS.BLUETOOTH_SCAN,
        PermissionsAndroid.PERMISSIONS.BLUETOOTH_CONNECT,
      ]).then((result: any) => {
        if (result) {
          console.debug(
            "[handleAndroidPermissions] User accepts runtime permissions android 12+"
          );
        } else {
          console.error(
            "[handleAndroidPermissions] User refuses runtime permissions android 12+"
          );
        }
      });
    } else if (Platform.OS === "android" && Platform.Version >= 23) {
      PermissionsAndroid.check(
        PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION
      ).then((checkResult: any) => {
        if (checkResult) {
          console.debug(
            "[handleAndroidPermissions] runtime permission Android <12 already OK"
          );
        } else {
          PermissionsAndroid.request(
            PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION
          ).then((requestResult: any) => {
            if (requestResult) {
              console.debug(
                "[handleAndroidPermissions] User accepts runtime permission android <12"
              );
            } else {
              console.error(
                "[handleAndroidPermissions] User refuses runtime permission android <12"
              );
            }
          });
        }
      });
    }
  } catch (error) {}
};

export const printPOS = {
  printText: function (): Promise<boolean> {
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
