package com.awesomelibrary;
import static android.content.Context.BIND_AUTO_CREATE;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import com.facebook.react.bridge.Promise;
import androidx.annotation.NonNull;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactMethod;
import java.util.ArrayList;
import java.util.List;
import net.posprinter.posprinterface.IMyBinder;
import net.posprinter.posprinterface.ProcessData;
import net.posprinter.posprinterface.TaskCallback;
import net.posprinter.service.PosprinterService;
import net.posprinter.utils.BitmapProcess;
import net.posprinter.utils.BitmapToByteData;
import net.posprinter.utils.DataForSendToPrinterPos80;


@ReactModule(name = AwesomeLibraryModule.NAME)
public class AwesomeLibraryModule extends ReactContextBaseJavaModule {
  public static final String NAME = "AwesomeLibrary";
  public static IMyBinder myBinder;
  public static boolean ISCONNECT=false;
 Context context;
  public AwesomeLibraryModule(ReactApplicationContext reactContext) {
    super(reactContext);
    context = reactContext ;
  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }

  ServiceConnection mSerconnection= new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBinder= (IMyBinder) service;
            Log.e("myBinder","connect");
            Toast toast = Toast.makeText(context, "connect", Toast.LENGTH_SHORT);
            toast.show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e("myBinder","disconnect");
            Toast toast = Toast.makeText(context, "disconnect", Toast.LENGTH_SHORT);
            toast.show();
        }
    };



    @ReactMethod
    public void onCreate() {
        Toast toast = Toast.makeText(context, "init Done!", Toast.LENGTH_SHORT);
        toast.show();
//        Log.e("App Notify", "init Done!");
//        super.onStart();

      Intent intent =new Intent(context, PosprinterService.class);
      context.bindService(intent,mSerconnection,BIND_AUTO_CREATE);
    }

      public static Bitmap decodeBase64ToBitmap(String base64String) {
        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
  @ReactMethod
    private void disConnectNet(){
        if (ISCONNECT){
            myBinder.DisconnectCurrentPort(new TaskCallback() {
                @Override
                public void OnSucceed() {
                    ISCONNECT = false;

                }

                @Override
                public void OnFailed() {
                    ISCONNECT = true;

                }
            });
        }
    }

    @ReactMethod
    private void connectNet(String ip){

        if (ip!=null){
            if (ISCONNECT) {
                myBinder.DisconnectCurrentPort(new TaskCallback() {
                    @Override
                    public void OnSucceed() {
                    }

                    @Override
                    public void OnFailed() {
                        disConnectNet();
                    }
                });
            } else {
                myBinder.ConnectNetPort(ip, 9100, new TaskCallback() {
                    @Override
                    public void OnSucceed() {
                        ISCONNECT = true;

                        Log.e("App Notify connectNet", "connect OnSucceed" );
                    }

                    @Override
                    public void OnFailed() {
                        ISCONNECT = false;
                        Log.e("App Notify connectNet", "connect OnFailed" );
                        disConnectNet();
                    }
                });
            }

        }else {
            Log.e("App Notify connectNet", "connect OnFailed 2" );
        }
    }

    @ReactMethod
    private void pingPinter(String ip,Promise promise){

        if (ip!=null){
            myBinder.ConnectNetPort(ip, 9100, new TaskCallback() {
                @Override
                public void OnSucceed() {

                    promise.resolve(ip);
                    Log.e("App Notify connectNet", "connect OnSucceed" );
                }

                @Override
                public void OnFailed() {
                    promise.reject("","OnFailed connect");
                    Log.e("App Notify connectNet", "connect OnFailed" );

                }
            });


        }else {
            promise.reject("","OnFailed connect");
            Log.e("App Notify connectNet", "connect OnFailed" );
        }
    }
    @ReactMethod
    private void printBitmap(String base64String,int w1,int w2){
//        final Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.test);
        final Bitmap bitmap1 =  BitmapProcess.compressBmpByYourWidth
                (decodeBase64ToBitmap(base64String),w1);

        if (ISCONNECT){
           myBinder.WriteSendData(new TaskCallback() {
                @Override
                public void OnSucceed() {
                    disConnectNet();

                }

                @Override
                public void OnFailed() {
                    disConnectNet();
                }
            }, new ProcessData() {
                @Override
                public List<byte[]> processDataBeforeSend() {
                    List<byte[]> list = new ArrayList<>();
                    list.add(DataForSendToPrinterPos80.initializePrinter());

                        list.add(DataForSendToPrinterPos80.printRasterBmp(0,bitmap1, BitmapToByteData.BmpType.Dithering, BitmapToByteData.AlignType.Right,w2));

                    list.add(DataForSendToPrinterPos80.printAndFeedLine());
                    return list;
                }
            });
        }else {
            disConnectNet();
        }
    }

}
