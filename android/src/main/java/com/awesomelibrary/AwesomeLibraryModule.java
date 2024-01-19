package com.awesomelibrary;

import static android.content.Context.BIND_AUTO_CREATE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import androidx.annotation.NonNull;
import com.facebook.react.bridge.Callback;
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
import java.util.Iterator;
import java.util.List;
import java.util.Set;


@ReactModule(name = AwesomeLibraryModule.NAME)
public class AwesomeLibraryModule extends ReactContextBaseJavaModule {
  List<byte[]> setPrinter = new ArrayList<>();
  public static final String NAME = "AwesomeLibrary";
  public static IMyBinder myBinder;
  private BluetoothAdapter bluetoothAdapter;
  private DeviceReceiver BtReciever;
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
            // Toast toast = Toast.makeText(context, "connect", Toast.LENGTH_SHORT);
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
    private void connectBT(String macAddress,Promise promise){
        if (macAddress.equals(null)||macAddress.equals("")){
            promise.reject("Error connect BTE");
        }else {
            myBinder.ConnectBtPort(macAddress, new TaskCallback() {
                @Override
                public void OnSucceed() {
                    ISCONNECT=true;
                    promise.resolve("connect BTE success");
                }

                @Override
                public void OnFailed() {
                    ISCONNECT=false;
                    promise.reject("Error connect BTE");
                }
            } );
        }
    }
     
    @ReactMethod 
    private void printRawData(String encode,Promise promise){
        byte[] bytes = Base64.decode(encode, Base64.DEFAULT);
         myBinder.Write(bytes, new TaskCallback() {
            @Override
            public void OnSucceed() {

               promise.resolve("success print raw");
            }

            @Override
            public void OnFailed() {

              promise.reject("error print raw");
            }
        });
    }
    
    @SuppressLint("MissingPermission")
    @ReactMethod
    private void findAvailableDevice(Promise promise){
        try {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (!bluetoothAdapter.isEnabled()) {

                Set<BluetoothDevice> device = bluetoothAdapter.getBondedDevices();
                Log.d("TAG", "findAvalibleDevice: "+device.size());
            }else {

                if (!bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.startDiscovery();
                }

                IntentFilter filterStart=new IntentFilter(BluetoothDevice.ACTION_FOUND);
                IntentFilter filterEnd=new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                context.registerReceiver(BtReciever, filterStart);
                context.registerReceiver(BtReciever, filterEnd);
                Set<BluetoothDevice> device=bluetoothAdapter.getBondedDevices();

                WritableMap result = Arguments.createMap();
                for(Iterator<BluetoothDevice> it = device.iterator(); it.hasNext();){
                    BluetoothDevice btd=it.next();
                    Log.d("TAG", btd.getName()+'\n'+btd.getAddress());
                    result.putString(btd.getName(), btd.getAddress());
                }
                promise.resolve(result);
            }
        }
        catch (Exception exe) {
            Log.d("TAG", "Exception--: " + exe);
            promise.reject(exe);
        }
    }

    @ReactMethod
    public void onCreate() {
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
    private void connectNetImg(String ip,String base64String,int w1,int w2,Promise promise){

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
                     
                          printBitmap(base64String,w1,w2,promise);
                    }

                    @Override
                    public void OnFailed() {
                        ISCONNECT = false;
                       
                      disConnectNet();
                    }
                });
            }

        }else {
            
        }

        
    }

    @ReactMethod
    private void printBitmapBLE(String base64String,int w1,int w2,int isBLE ,Promise promise){
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
                   promise.reject("","OnFailed print img");
                   
                  disConnectNet();
                   
                }
            }, new ProcessData() {
                @Override
                public List<byte[]> processDataBeforeSend() {
                    List<byte[]> list = new ArrayList<>();
                    list.add(DataForSendToPrinterPos80.initializePrinter());
                    List<Bitmap> blist= new ArrayList<>();
                    blist = BitmapProcess.cutBitmap(50,bitmap1);
                    for (int i= 0 ;i<blist.size();i++){
                        list.add(DataForSendToPrinterPos80.printRasterBmp(0,blist.get(i), BitmapToByteData.BmpType.Threshold, BitmapToByteData.AlignType.Center,w2));
                    }
                    list.add(DataForSendToPrinterPos80.printAndFeedLine());

                    if(isBLE == 0){
                       list.add(DataForSendToPrinterPos80.selectCutPagerModerAndCutPager(0x42,0x66));
                    }
                  
                    return list;
                }
            });
        }else {
           promise.reject("","OnFailed print img");
                      disConnectNet();
                    
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
                   promise.reject("","OnFailed print img");
                   
                  disConnectNet();
                   
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
           promise.reject("","OnFailed print img");
                      disConnectNet();
                    
        }
    }
    @ReactMethod
    private void pingPinter(String ip,Promise promise){

        if (ip!=null){
            myBinder.ConnectNetPort(ip, 9100, new TaskCallback() {
                @Override
                public void OnSucceed() {
                    //   disConnectNet();
                    promise.resolve(ip);
                    Log.e("App Notify connectNet", "connect OnSucceed" );
                }

                @Override
                public void OnFailed() {
                    //   disConnectNet();
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
            // disConnectNet(promise);
        }
    }

     @ReactMethod
    private void connectNet(String ip,Promise promise){
       
        if (ip!=null){
            if (ISCONNECT) {
                myBinder.DisconnectCurrentPort(new TaskCallback() {
                    @Override
                    public void OnSucceed() {
                      ISCONNECT = false;
                      connectNet(ip,promise);
                    //   promise.resolve(Boolean.toString(ISCONNECT));
                    }

                    @Override
                    public void OnFailed() {
                     ISCONNECT = true;
                     promise.reject(Boolean.toString(ISCONNECT));
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
    private void printText(Promise promise){
        if (ISCONNECT){
            myBinder.WriteSendData(new TaskCallback() {
                @Override
                public void OnSucceed() {
                  
                    //  disConnectNet();
                    promise.resolve("Done");
                }

                @Override
                public void OnFailed() {
                   
                   promise.reject("error","printText fail!");
                //    disConnectNet();

                }
            }, new ProcessData() {
                @Override
                public List<byte[]> processDataBeforeSend() {
                    return setPrinter;
                }
            });
        }else {
              promise.reject("error","print ISCONNECT  false");
            // disConnectNet();
        }
    }

   @ReactMethod
    public void clearPaper(Promise promise){
       setPrinter.clear();
       if(setPrinter.size() ==0){
           promise.resolve("0");
       }
    }
    @ReactMethod
    public void initializeText(){
        setPrinter.add(DataForSendToPrinterPos80.initializePrinter());
    }
    @ReactMethod
    public void cut(){
        setPrinter.add(DataForSendToPrinterPos80.selectCutPagerModerAndCutPager(0x42,0x66));
    }
    @ReactMethod
    public void printAndFeedLine(){
        setPrinter.add(DataForSendToPrinterPos80.printAndFeedLine());
    }

    @ReactMethod
    public void CancelChineseCharModel(){
       setPrinter.add(DataForSendToPrinterPos80.CancelChineseCharModel());
    }

    @ReactMethod
    public void selectAlignment(int n){
        // type : 0 = left , 1 = : center , right : 2
       setPrinter.add(DataForSendToPrinterPos80.selectAlignment(n));
    }
    @ReactMethod
    public void text(String text,String charset){
       setPrinter.add(StringUtils.strTobytes(text,charset));
    }
    @ReactMethod
    public void selectCharacterSize(int n){
       setPrinter.add(DataForSendToPrinterPos80.selectCharacterSize(n));
    }

    @ReactMethod
    public void selectOrCancelBoldModel(int n){
       setPrinter.add(DataForSendToPrinterPos80.selectOrCancelBoldModel(n));
    }

    @ReactMethod
    public void selectCharacterCodePage(int n){
       setPrinter.add(DataForSendToPrinterPos80.selectCharacterCodePage(n));
    }

    @ReactMethod
    public void selectInternationalCharacterSets(int n){
       setPrinter.add(DataForSendToPrinterPos80.selectInternationalCharacterSets(n));
    }

    @ReactMethod
    public void setAbsolutePrintPosition(int n,int m){
       setPrinter.add(DataForSendToPrinterPos80.setAbsolutePrintPosition(n,m) );
    }


    // @ReactMethod
    // private byte[] GetPrinterName(){
    //     return DataForSendToPrinterPos80.GetPrinterName();
    // }
    // @ReactMethod
    // private byte[] GetManufacturer(){
    //     return DataForSendToPrinterPos80.GetManufacturer();
    // }
    // @ReactMethod
    // private byte[] GetFirmwareVersion(){
    //     return DataForSendToPrinterPos80.GetFirmwareVersion();
    // }
    // @ReactMethod
    // private byte[] GetSerialNumber(){
    //     return DataForSendToPrinterPos80.GetSerialNumber();
    // }
    // @ReactMethod
    // private byte[] selectChineseCharModel(){
    //     return DataForSendToPrinterPos80.selectChineseCharModel();
    // }
    // @ReactMethod
    // private byte[] selectPageModel(){
    //     return DataForSendToPrinterPos80.selectPageModel();
    // }
    // @ReactMethod
    // private byte[] avoidLostOrder(){
    //     return DataForSendToPrinterPos80.avoidLostOrder();
    // }
    // @ReactMethod
    // private byte[] selectStandardModel(){
    //     return DataForSendToPrinterPos80.selectStandardModel();
    // }
    // @ReactMethod
    // private byte[] setDefaultLineSpacing(){
    //     return DataForSendToPrinterPos80.setDefultLineSpacing();
    // }
   
   
  
    // @ReactMethod
    // private byte[] selectOrCancelChineseCharUnderLineModel(int n){
    //     return DataForSendToPrinterPos80.selectOrCancelChineseCharUnderLineModel(n);
    // }
    // @ReactMethod
    // private byte[] selectHRIFont(int n){
    //     return DataForSendToPrinterPos80.selectHRIFont(n);
    // }
    // @ReactMethod
    // private byte[] selectPrintDirectionUnderPageModel(int n){
    //     return DataForSendToPrinterPos80.selectPrintDirectionUnderPageModel(n);
    // }
    // @ReactMethod
    // private byte[] setBarcodeWidth(int n){
    //     return DataForSendToPrinterPos80.setBarcodeWidth(n);
    // }
    // @ReactMethod
    // private byte[] setBarcodeHeight(int n){
    //     return DataForSendToPrinterPos80.setBarcodeHeight(n);
    // }
    // @ReactMethod
    // private byte[] getPrinterStatus(int port){
    //     return DataForSendToPrinterPos80.getPrinterStatus(port);
    // }

    // @ReactMethod
    // private byte[] openOrCloseAutoReturnPrintState(int n){
    //     return DataForSendToPrinterPos80.openOrCloseAutoReturnPrintState(n);
    // }
    // @ReactMethod
    // private byte[] selectPrintTransducerOutPutPageOutSignal(int n){
    //     return DataForSendToPrinterPos80.selectPrintTransducerOutPutPageOutSignal(n);
    // }
    // @ReactMethod
    // private byte[] printBmpInFLASH(int n, int m){
    //     return DataForSendToPrinterPos80.printBmpInFLASH(n,m);
    // }
    // @ReactMethod
    // private byte[] definedUserDefinedChineseChar(int n, byte[] b){
    //     return DataForSendToPrinterPos80.definedUserDefinedChineseChar(n,b);
    // }
    // @ReactMethod
    // private byte[] setAbsolutePositionUnderPageModel(int n, int m){
    //     return DataForSendToPrinterPos80.setAbsolutePositionUnderPageModel(n,m);
    // }
    // @ReactMethod
    // private byte[] setChineseCharLeftAndRightSpace(int n, int m){
    //     return DataForSendToPrinterPos80.setChineseCharLeftAndRightSpace(n,m);
    // }
    // @ReactMethod
    // private byte[] setLeftSpace(int n, int m){
    //     return DataForSendToPrinterPos80.setLeftSpace(n,m);
    // }
    // @ReactMethod
    // private byte[] setPrintAreaWidth(int n, int m){
    //     return DataForSendToPrinterPos80.setPrintAreaWidth(n,m);
    // }
    // @ReactMethod
    // private byte[] setVerticalRelativePositionUnderPageModel(int n, int m){
    //     return DataForSendToPrinterPos80.setVerticalRelativePositionUnderPageModel(n,m);
    // }
    // @ReactMethod
    // private byte[] executeMacroCommand(int r, int t, int m){
    //     return DataForSendToPrinterPos80.executeMacrodeCommand(r,t,m);
    // }
    // @ReactMethod
    // private byte[] setHorizontalAndVerticalMoveUnit(int x, int y){
    //     return DataForSendToPrinterPos80.setHorizontalAndVerticalMoveUnit(x,y);
    // }
    // @ReactMethod
    // private byte[] selectOrCancelConvertPrintModel(int n){
    //     return DataForSendToPrinterPos80.selectOrCancelConvertPrintModel(n);
    // }
    // @ReactMethod
    // private byte[] allowOrForbidPressButton(int n){
    //     return DataForSendToPrinterPos80.allowOrForbidPressButton(n);
    // }
 
    // @ReactMethod
    // private byte[] printAndFeedForward(int n){
    //     return DataForSendToPrinterPos80.printAndFeedForward(n);
    // }
    // @ReactMethod
    // private byte[] selectPrintTransducerStopPrint(int n){
    //     return DataForSendToPrinterPos80.selectPrintTransducerStopPrint(n);
    // }
    // @ReactMethod
    // private byte[] setRelativeHorizontalPrintPosition(int nL, int nH){
    //     return DataForSendToPrinterPos80.setRelativeHorizontalPrintPosition(nL,nH);
    // }
    // @ReactMethod
    // private byte[] createCashboxControlPulse(int m, int t1, int t2){
    //     return DataForSendToPrinterPos80.creatCashboxContorlPulse(m,t1,t2);
    // }
    // @ReactMethod
    // private byte[] selectFont(int n){
    //     return DataForSendToPrinterPos80.selectFont(n);
    // }
    // @ReactMethod
    // private byte[] setChineseCharacterModel(int n){
    //     return DataForSendToPrinterPos80.setChineseCharacterModel(n);
    // }
    // @ReactMethod
    // private byte[] setLineSpacing(int n){
    //     return DataForSendToPrinterPos80.setLineSpaceing(n);
    // }
    // @ReactMethod
    // private byte[] selectOrCancelDoublePrintModel(int n){
    //     return DataForSendToPrinterPos80.selectOrCancelDoubelPrintModel(n);
    // }
    
    // @ReactMethod
    // private byte[] cancelUserDefinedCharacters(int n){
    //     return DataForSendToPrinterPos80.cancelUserDefinedCharacters(n);
    // }
    // @ReactMethod
    // private byte[] setHorizontalmovementPosition(byte[] b){
    //     return DataForSendToPrinterPos80.setHorizontalmovementPosition(b);
    // }

    // @ReactMethod
    // private byte[] selectPrinter(int n){
    //     return DataForSendToPrinterPos80.selectPrinter(n);
    // }
    // @ReactMethod
    // private byte[] printAndFeed(int n){
    //     return DataForSendToPrinterPos80.printAndFeed(n);
    // }
    // @ReactMethod
    // private byte[] returnState(int n){
    //     return DataForSendToPrinterPos80.returnState(n);
    // }
   
  
   

    // @ReactMethod
    // private byte[] printQRcode(int n, int errLevel, String code){
    //     // 
    //     return DataForSendToPrinterPos80.printQRcode(n,errLevel,code);
    // }
    // @ReactMethod
    // private byte[] printBarcode(int m, int n, String content){
    //     // 
    //     return DataForSendToPrinterPos80.printBarcode(m,n,content);
    // }
    // @ReactMethod
    // private byte[] printRasterBmp(int m, String bitmap,String bmpType, String alignType,int size,int w1){
    //      final Bitmap bitmap1 =  BitmapProcess.compressBmpByYourWidth
    //             (decodeBase64ToBitmap(bitmap),w1);
    //         BitmapToByteData.AlignType align;
    //         BitmapToByteData.BmpType bmpTypes;
    //        if(alignType == "Right"){
    //         align = BitmapToByteData.AlignType.Right;
    //        }else if(alignType == "Left"){
    //            align = BitmapToByteData.AlignType.Left;
    //        }else{
    //           align = BitmapToByteData.AlignType.Center;
    //        }
    //        if(bmpType == "Dithering"){
    //         bmpTypes = BitmapToByteData.BmpType.Dithering;
    //        }else {
    //         bmpTypes = BitmapToByteData.BmpType.Threshold;
    //        }

    //     return DataForSendToPrinterPos80.printRasterBmp(0,bitmap1,bmpTypes,align,size);
    // }

}
