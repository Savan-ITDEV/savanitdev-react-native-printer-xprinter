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
import androidx.annotation.NonNull;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.module.annotations.ReactModule;
import net.posprinter.posprinterface.IMyBinder;
import net.posprinter.posprinterface.ProcessData;
import net.posprinter.posprinterface.TaskCallback;
import net.posprinter.service.PosprinterService;
import net.posprinter.utils.BitmapProcess;
import net.posprinter.utils.BitmapToByteData;
import net.posprinter.utils.DataForSendToPrinterPos80;
import net.posprinter.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;


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
        // Toast toast = Toast.makeText(context, "init Done!", Toast.LENGTH_SHORT);
        // toast.show();
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
    private void disconnetNetPort(){
        if (ISCONNECT){
            myBinder.DisconnetNetPort(new TaskCallback() {
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
    private void connectNet(String ip,Promise promise){
       
        if (ip!=null){
            if (ISCONNECT) {
                myBinder.DisconnectCurrentPort(new TaskCallback() {
                    @Override
                    public void OnSucceed() {
                      
                    }

                    @Override
                    public void OnFailed() {
                    
                    }
                });
            } else {
                myBinder.ConnectNetPort(ip, 9100, new TaskCallback() {
                    @Override
                    public void OnSucceed() {
                        ISCONNECT = true;
                        promise.resolve(Boolean.toString(ISCONNECT));
                    }

                    @Override
                    public void OnFailed() {
                        ISCONNECT = false;
                         promise.reject(Boolean.toString(ISCONNECT));
                      
                    }
                });
            }

        }else {
           
        }
    }


    @ReactMethod
    private void connectNetImg(String ip,String base64String,int w1,int w2,Promise promise){

     if (ip!=null){
            if (ISCONNECT) {
              disConnectNet();
            } else {
                myBinder.ConnectNetPort(ip, 9100, new TaskCallback() {
                    @Override
                    public void OnSucceed() {
                        ISCONNECT = true;
                        printBitmap(base64String,w1,w2,promise);
                    }

                    @Override
                    public void OnFailed() {
                        ISCONNECT = false;
                        promise.reject("","connectNetImg error");
                      disConnectNet();
                    }
                });
            }

        }else {
            
        }

        
    }
   @ReactMethod
    private void printBitmap(String base64String,int w1,int w2,Promise promise){
        final Bitmap bitmap1 =  BitmapProcess.compressBmpByYourWidth
                (decodeBase64ToBitmap(base64String),w1);

        if (ISCONNECT){
           myBinder.WriteSendData(new TaskCallback() {
                @Override
                public void OnSucceed() {
                     promise.resolve("1");
                     disConnectNet();
                   
                   
                   
                }

                @Override
                public void OnFailed() {
                    promise.reject("","printer error");
                   
                     disConnectNet();
                   
                }
            }, new ProcessData() {
                @Override
                public List<byte[]> processDataBeforeSend() {
                    List<byte[]> list = new ArrayList<>();
                    list.add(DataForSendToPrinterPos80.initializePrinter());
                    // list.add(StringUtils.strTobytes("1234567890qwertyuiopakjbdscm nkjdv mcdskjb"));
                     list.add(DataForSendToPrinterPos80.printRasterBmp(0,bitmap1, BitmapToByteData.BmpType.Dithering, BitmapToByteData.AlignType.Right,w2));
                    list.add(DataForSendToPrinterPos80.printAndFeedLine());
                    list.add(DataForSendToPrinterPos80.selectCutPagerModerAndCutPager(0x42,0x66));
                    return list;
                }
            });
        }else {
              promise.reject("","printer error");
                     disConnectNet();
                    
        }
    }
    @ReactMethod
    private void pingPinter(String ip,Promise promise){

        if (ip!=null){
            myBinder.ConnectNetPort(ip, 9100, new TaskCallback() {
                @Override
                public void OnSucceed() {
                      disConnectNet();
                    promise.resolve(ip);
                    Log.e("App Notify connectNet", "connect OnSucceed" );
                }

                @Override
                public void OnFailed() {
                    disConnectNet();
                    promise.reject("","OnFailed connect der");
                    Log.e("App Notify connectNet", "connect OnFailed" );

                }
            });


        }else {
            promise.reject("","OnFailed connect der 2 ");
            Log.e("App Notify connectNet", "connect OnFailed" );
        }
    }
 
    @ReactMethod
    private void printImg(String base64String,int w1,int w2,Promise promise){
        final Bitmap bitmap1 =  BitmapProcess.compressBmpByYourWidth
                (decodeBase64ToBitmap(base64String),w1);

        if (ISCONNECT){
            myBinder.WriteSendData(new TaskCallback() {
                @Override
                public void OnSucceed() {
                   promise.resolve("1");
                }

                @Override
                public void OnFailed() {
                    promise.reject("0","OnFailed printImg");
                    // disConnectNet(promise);
                }
            }, new ProcessData() {
                @Override
                public List<byte[]> processDataBeforeSend() {
                    List<byte[]> list = new ArrayList<>();
                    list.add(DataForSendToPrinterPos80.initializePrinter());
                    list.add(DataForSendToPrinterPos80.printRasterBmp(0,bitmap1, BitmapToByteData.BmpType.Dithering, BitmapToByteData.AlignType.Right,w2));
                    list.add(DataForSendToPrinterPos80.printAndFeedLine());
                     list.add(DataForSendToPrinterPos80.selectCutPagerModerAndCutPager(0x42,0x66));
                    return list;
                }
            });
        }else {
             promise.reject("0","OnFailed printImg");
        }
    }

}
