const { withAndroidManifest } = require("@expo/config-plugins");

// please add this code to  "plugins": ["./android-manifest.plugin.js"] in app.json
module.exports = function androiManifestPlugin(config) {
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
