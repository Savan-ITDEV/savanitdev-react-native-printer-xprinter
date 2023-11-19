"use strict";
Object.defineProperty(exports, "__esModule", { value: true });

const config_plugins_1 = require("@expo/config-plugins");

const androiManifestPlugin = (config: any) => {
  return withAndroidManifest(config, async (config) => {
    let androidManifest = config.modResults.manifest;
    androidManifest.application[0]["service"] = {
      $: {
        "android:name": "net.posprinter.service.PosprinterService",
      },
    };
    return config;
  });
};

exports.default = (0, config_plugins_1.createRunOncePlugin)(
  androiManifestPlugin,
  "react-native-awesome-library",
  ""
);
