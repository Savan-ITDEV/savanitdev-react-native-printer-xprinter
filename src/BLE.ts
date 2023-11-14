import BleManager, {
  BleDisconnectPeripheralEvent,
  BleManagerDidUpdateValueForCharacteristicEvent,
  BleScanCallbackType,
  BleScanMatchMode,
  BleScanMode,
  Peripheral,
} from "react-native-ble-manager";
import {
  NativeEventEmitter,
  NativeModules,
  PermissionsAndroid,
  Platform,
} from "react-native";
const SECONDS_TO_SCAN_FOR = 7;
const SERVICE_UUIDS: string[] = [];
const ALLOW_DUPLICATES = true;
const BleManagerModule = NativeModules.BleManager;

const bleManagerEmitter = new NativeEventEmitter(BleManagerModule);
export const handleDiscoverPeripheral = (
  peripheral: Peripheral,
  addOrUpdatePeripheral: any
) => {
  console.debug("[handleDiscoverPeripheral] new BLE peripheral=", peripheral);
  if (!peripheral.name) {
    peripheral.name = "NO NAME";
  }
  addOrUpdatePeripheral(peripheral.id, peripheral);
};
export const handleStopScan = (setIsScanning: any) => {
  setIsScanning(false);
  console.debug("[handleStopScan] scan is stopped.");
};
export const handleDisconnectedPeripheral = (
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
export const handleUpdateValueForCharacteristic = (
  data: BleManagerDidUpdateValueForCharacteristicEvent
) => {
  console.debug(
    `[handleUpdateValueForCharacteristic] received data from '${data.peripheral}' with characteristic='${data.characteristic}' and value='${data.value}'`
  );
};
const addOrUpdatePeripheral = (
  id: string,
  updatedPeripheral: Peripheral,
  setPeripherals: any
) => {
  // new Map() enables changing the reference & refreshing UI.
  // TOFIX not efficient.
  setPeripherals((map: any) => new Map(map.set(id, updatedPeripheral)));
};
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

export const startScan = (
  setPeripherals: (arg0: Map<string, Peripheral>) => void,
  setIsScanning: (arg0: boolean) => void,
  isScanning: any
) => {
  if (!isScanning) {
    // reset found peripherals before scan
    setPeripherals(new Map<Peripheral["id"], Peripheral>());

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
          console.error("[startScan] ble scan returned in error", err);
        });
    } catch (error) {
      console.error("[startScan] ble scan error thrown", error);
    }
  }
};

// React.useEffect(() => {
//   try {
//     BleManager.start({ showAlert: false })
//       .then(() => console.debug("BleManager started."))
//       .catch((error) =>
//         console.error("BeManager could not be started.", error)
//       );
//   } catch (error) {
//     console.error("unexpected error starting BleManager.", error);
//     return;
//   }

//   const listeners = [
//     bleManagerEmitter.addListener(
//       "BleManagerDiscoverPeripheral",
//       (peripheral: Peripheral) =>
//         handleDiscoverPeripheral(peripheral, addOrUpdatePeripheral)
//     ),
//     bleManagerEmitter.addListener("BleManagerStopScan", () =>
//       handleStopScan(setIsScanning)
//     ),
//     bleManagerEmitter.addListener(
//       "BleManagerDisconnectPeripheral",
//       (e: BleDisconnectPeripheralEvent) =>
//         handleDisconnectedPeripheral(e, peripherals, setPeripherals)
//     ),
//     bleManagerEmitter.addListener(
//       "BleManagerDidUpdateValueForCharacteristic",
//       (data: BleManagerDidUpdateValueForCharacteristicEvent) =>
//         handleUpdateValueForCharacteristic(data)
//     ),
//   ];

//   return () => {
//     console.debug("[app] main component unmounting. Removing listeners...");
//     for (const listener of listeners) {
//       listener.remove();
//     }
//   };
// }, []);
