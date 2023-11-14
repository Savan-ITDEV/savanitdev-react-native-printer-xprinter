import BleManager, {
  BleDisconnectPeripheralEvent,
  BleManagerDidUpdateValueForCharacteristicEvent,
  BleScanCallbackType,
  BleScanMatchMode,
  BleScanMode,
  Peripheral,
} from "react-native-ble-manager";
import {
  Button,
  NativeEventEmitter,
  NativeModules,
  PermissionsAndroid,
  Platform,
  ScrollView,
  StyleSheet,
  Text,
  TouchableHighlight,
  TouchableOpacity,
  View,
} from "react-native";

import React from "react";

const SECONDS_TO_SCAN_FOR = 7;
const SERVICE_UUIDS: string[] = [];
const ALLOW_DUPLICATES = true;
const BleManagerModule = NativeModules.BleManager;
const bleManagerEmitter = new NativeEventEmitter(BleManagerModule);

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
export function connectBt(macAddress: string) {
  try {
    return AwesomeLibrary.connectBt(macAddress);
  } catch (error) {
    console.log("please check your bluetooth!");
  }
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

declare module "react-native-ble-manager" {
  // enrich local contract with custom state properties needed by App.tsx
  interface Peripheral {
    connected?: boolean;
    connecting?: boolean;
  }
}

export const BLUETOOTH_SCAN = ({ styles, children, update }) => {
  const [isScanning, setIsScanning] = React.useState(false);
  const [peripherals, setPeripherals] = React.useState([]);
  const [mac, setMac] = React.useState("");

  const handleDiscoverPeripheral = (
    peripheral: Peripheral,
    addOrUpdatePeripheral: any
  ) => {
    console.debug("[handleDiscoverPeripheral] new BLE peripheral=");
    if (!peripheral.name) {
      peripheral.name = "NO NAME";
    }
    addOrUpdatePeripheral(peripheral.id, peripheral);
  };
  const handleStopScan = (setIsScanning: any) => {
    setIsScanning(false);
    console.debug("[handleStopScan] scan is stopped.");
  };
  const handleDisconnectedPeripheral = (
    event: BleDisconnectPeripheralEvent,
    peripherals: any,
    setPeripherals: any
  ) => {
    let peripheral = peripherals.get(event.peripheral);
    if (peripheral) {
      console.debug(
        `[handleDisconnectedPeripheral][${peripheral.id}] previously connected peripheral is disconnected.`,
        event.peripheral
      );
      addOrUpdatePeripheral(
        peripheral.id,
        { ...peripheral, connected: false },
        setPeripherals
      );
    }
    console.debug(
      `[handleDisconnectedPeripheral][${event.peripheral}] disconnected.`
    );
  };
  const handleUpdateValueForCharacteristic = (
    data: BleManagerDidUpdateValueForCharacteristicEvent
  ) => {
    console.debug(
      `[handleUpdateValueForCharacteristic] received data from '${data.peripheral}' with characteristic='${data.characteristic}' and value='${data.value}'`
    );
  };

  // Function to add or update a key-value pair in the array
  const addOrUpdateKeyValuePairInArray = (key, newValue) => {
    setPeripherals((prevArray) => {
      // Find the index of the object with the specified key
      const indexOfObjectToUpdate = prevArray.findIndex(
        (obj: { key: any }) => obj.key === key
      );

      if (indexOfObjectToUpdate !== -1) {
        // If the object exists, update the value
        const updatedArray = [...prevArray];
        updatedArray[indexOfObjectToUpdate] = {
          ...updatedArray[indexOfObjectToUpdate],
          value: newValue,
        };
        return updatedArray;
      } else {
        // If the object doesn't exist, add a new object to the array
        return [
          ...prevArray,
          {
            id: prevArray.length + 1, // Assuming each object has a unique id
            key,
            value: newValue,
          },
        ];
      }
    });
  };

  const addOrUpdatePeripheral = (
    id: string,
    updatedPeripheral: Peripheral,
    setPeripherals: any
  ) => {
    addOrUpdateKeyValuePairInArray(id, updatedPeripheral);
  };
  React.useEffect(() => {
    try {
      BleManager.start({ showAlert: false })
        .then(() => console.debug("BleManager started."))
        .catch((error) =>
          console.error("BeManager could not be started.", error)
        );
    } catch (error) {
      console.error("unexpected error starting BleManager.", error);
      return;
    }

    const listeners = [
      bleManagerEmitter.addListener(
        "BleManagerDiscoverPeripheral",
        (peripheral: Peripheral) =>
          handleDiscoverPeripheral(peripheral, addOrUpdatePeripheral)
      ),
      bleManagerEmitter.addListener("BleManagerStopScan", () =>
        handleStopScan(setIsScanning)
      ),
      bleManagerEmitter.addListener(
        "BleManagerDisconnectPeripheral",
        (e: BleDisconnectPeripheralEvent) =>
          handleDisconnectedPeripheral(e, peripherals, setPeripherals)
      ),
      bleManagerEmitter.addListener(
        "BleManagerDidUpdateValueForCharacteristic",
        (data: BleManagerDidUpdateValueForCharacteristicEvent) =>
          handleUpdateValueForCharacteristic(data)
      ),
    ];

    return () => {
      console.debug("[app] main component unmounting. Removing listeners...");
      for (const listener of listeners) {
        listener.remove();
      }
    };
  }, []);
  React.useEffect(() => {
    update(peripherals);
  }, [peripherals]);
  const startScan = () => {
    if (!isScanning) {
      // reset found peripherals before scan
      setPeripherals([]);

      try {
        console.debug("[startScan] starting scan...");
        setIsScanning(true);
        BleManager.scan(SERVICE_UUIDS, SECONDS_TO_SCAN_FOR, ALLOW_DUPLICATES, {
          matchMode: BleScanMatchMode.Sticky,
          scanMode: BleScanMode.LowLatency,
          callbackType: BleScanCallbackType.AllMatches,
        })
          .then(() => {
            console.debug("[startScan] scan promise returned successfully.");
          })
          .catch((err) => {
            // console.error("[startScan] ble scan returned in error", err);
          });
      } catch (error) {
        // console.error("[startScan] ble scan error thrown", error);
      }
    }
  };

  return (
    <View>
      <TouchableOpacity style={{ ...styles }} onPress={startScan}>
        {children}
      </TouchableOpacity>
    </View>
  );
};
