package com.awesomelibrary;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Base64;
import android.util.Log;
import androidx.annotation.NonNull;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.module.annotations.ReactModule;
import net.posprinter.IDeviceConnection;
import net.posprinter.IPOSListener;
import net.posprinter.POSConnect;
import net.posprinter.POSConst;
import net.posprinter.POSPrinter;

@ReactModule(name = AwesomeLibraryModule.NAME)
public class AwesomeLibraryModule extends ReactContextBaseJavaModule {
  public static final String NAME = "AwesomeLibrary";
  Context context;
  public AwesomeLibraryModule(ReactApplicationContext reactContext) {
    super(reactContext);
    context = reactContext;
  }
  private IPOSListener connectListener = new IPOSListener() {
    @Override
    public void onStatus(int code, String msg) {
      SharedPreferences.Editor editor = context.getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE).edit();
      switch (code) {
        case POSConnect.CONNECT_SUCCESS:
          Log.e("Notify", "CONNECT_SUCCESS");

          editor.putString("status", "CONNECT_SUCCESS");
          editor.apply();
          break;
        case POSConnect.CONNECT_FAIL:
          Log.e("Notify", "CONNECT_FAIL");

          editor.putString("status", "CONNECT_FAIL");
          editor.apply();
          break;
        case POSConnect.CONNECT_INTERRUPT:
          Log.e("Notify", "CONNECT_INTERRUPT");

          editor.putString("status", "CONNECT_INTERRUPT");
          editor.apply();
          break;
        case POSConnect.SEND_FAIL:
          Log.e("Notify", "SEND_FAIL");

          editor.putString("status", "SEND_FAIL");
          editor.apply();
          break;
        case POSConnect.USB_DETACHED:
          Log.e("Notify", "USB_DETACHED");

          editor.putString("status", "USB_DETACHED");
          editor.apply();
          break;
        case POSConnect.USB_ATTACHED:
          Log.e("Notify", "USB_ATTACHED");

          editor.putString("status", "USB_ATTACHED");
          editor.apply();
          break;
      }

    }
  };
  @Override
  @NonNull
  public String getName() {
    return NAME;
  }


  // Example method
  // See https://reactnative.dev/docs/native-modules-android
  @ReactMethod
  public void multiply(double a, double b, Promise promise) {
    promise.resolve(a - b);
  }
  @ReactMethod
  public void onCreate() {
    Log.e("App Notify", "init Done!");
    POSConnect.init(context);
  }
  IDeviceConnection curConnect = null;
  @ReactMethod
  public void connectNet(String ipAddress,Promise promise) {
    SharedPreferences prefs = context.getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
    String str = prefs.getString("status", "");
    try {
      if (curConnect != null) {
        curConnect.close();
      }
      curConnect = POSConnect.createDevice(POSConnect.DEVICE_TYPE_ETHERNET);
      curConnect.connect(ipAddress, connectListener);

      Log.e("App Notify", "connect printer Done" + str);
      promise.resolve(str);
    } catch (Exception e) {
      Log.e("App Notify", "connect printer fail!");
      promise.resolve(str);

    }
  }

  @ReactMethod
  public void printText() {
    try {
      POSPrinter printer = new POSPrinter(curConnect);
      printer.initializePrinter().feedLine();
      Log.e("App Notify", "connect printer Done" );

    } catch (Exception e) {
      Log.e("App Notify", "connect printer fail!");
    }
  }
  @ReactMethod
  public void disConnectNet(Promise promise) {
    try {
      curConnect.close();
      Log.e("App Notify", "disconnect printer Done");
      promise.resolve(true);
    } catch (Exception e) {
      Log.e("App Notify", "disconnect printer fail!");
      promise.resolve(false);
    }
  }

   @ReactMethod
    public void connectBt(String macAddress,Promise promise) {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        String str = prefs.getString("status", "");
        try {
        if (curConnect != null) {
            curConnect.close();
        }
        Log.e("App Notify",  macAddress);
        curConnect = POSConnect.createDevice(POSConnect.DEVICE_TYPE_BLUETOOTH);
        curConnect.connect(macAddress, connectListener);
            promise.resolve(str);
        } catch (Exception e) {
            Log.e("App Notify", "connect printer fail!");
            promise.resolve(str);
        }
    }
    public static Bitmap decodeBase64ToBitmap(String base64String) {
        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
    public static Bitmap changeBitmapContrastBrightness(Bitmap bmp, float contrast, float brightness)
{
    ColorMatrix cm = new ColorMatrix(new float[]
            {
                contrast, 0, 0, 0, brightness,
                0, contrast, 0, 0, brightness,
                0, 0, contrast, 0, brightness,
                0, 0, 0, 1, 0
            });

    Bitmap ret = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());

    Canvas canvas = new Canvas(ret);

    Paint paint = new Paint();
    paint.setColorFilter(new ColorMatrixColorFilter(cm));
    canvas.drawBitmap(bmp, 0, 0, paint);

    return ret;
}
    @ReactMethod
    public void printImg(String base64String,float contrast, float brightness) {
        try {
            // Decode Base64 string to Bitmap
            Bitmap bitmap = decodeBase64ToBitmap(base64String);
            POSPrinter printer = new POSPrinter(curConnect);
            printer.
                    initializePrinter()
                    .printBitmap(changeBitmapContrastBrightness(bitmap,contrast,brightness),POSConst.ALIGNMENT_CENTER, 500)
                    .feedLine();
//                    .cutHalfAndFeed(1);
            Log.e("App Notify", "connect printer Done" );
        } catch (Exception e) {
            Log.e("App Notify", "connect printer fail!");
        }
    }
  
}
