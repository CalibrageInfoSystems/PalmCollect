package com.cis.palm360collection.ui;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.cis.palm360collection.R;
import com.cis.palm360collection.cloudhelper.ApplicationThread;
import com.cis.palm360collection.common.CommonConstants;
import com.cis.palm360collection.common.CommonUtils;
import com.cis.palm360collection.database.DataAccessHandler;
import com.cis.palm360collection.database.Palm3FoilDatabase;
import com.cis.palm360collection.database.Queries;
import com.cis.palm360collection.datasync.helpers.DataSyncHelper;
import com.cis.palm360collection.helper.PrefUtil;
import com.cis.palm360collection.uihelper.ProgressBar;
import com.cis.palm360collection.utils.UiUtils;

import java.io.File;
import java.util.ArrayList;

import SecuGen.FDxSDKPro.JSGFPLib;


//Splash Screen
public class SplashScreen extends AppCompatActivity {

    public static final String LOG_TAG = SplashScreen.class.getName();


    private static final int REQUEST_CODE_PERMISSIONS = 100;


    public static Palm3FoilDatabase palm3FoilDatabase;

    private DataAccessHandler dataAccessHandler;
    private SharedPreferences sharedPreferences;


    private String[] PERMISSIONS_REQUIRED = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.FOREGROUND_SERVICE
    };
    private ActivityResultLauncher<Intent> mGetPermission;

    //Creating DB and Master Sync
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        sharedPreferences = getSharedPreferences("appprefs", MODE_PRIVATE);

        if (!CommonUtils.isNetworkAvailable(this)) {
            UiUtils.showCustomToastMessage("Please check your network connection", SplashScreen.this, 1);
        }

        mGetPermission = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            Toast.makeText(SplashScreen.this, "Permission granted", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        takePermission();
    }

    private void takePermission() {
        if (isPermissionGranted()) {
            initializeApp();
        } else {
            requestPermission();
        }
    }

    private boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return Environment.isExternalStorageManager()
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }

    }


    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                mGetPermission.launch(intent);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                mGetPermission.launch(intent);
            }
        }

        ArrayList<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissions.add(Manifest.permission.FOREGROUND_SERVICE);
        permissions.add(Manifest.permission.CAMERA);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissions.add(Manifest.permission.FOREGROUND_SERVICE_LOCATION);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN);
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE);
        }


        ActivityCompat.requestPermissions(
                this,
                permissions.toArray(new String[0]),
                REQUEST_CODE_PERMISSIONS
        );
    }


    private void initializeApp() {
        ensureDatabaseDirectory();
        try {
            palm3FoilDatabase = Palm3FoilDatabase.getPalm3FoilDatabase(this);
            palm3FoilDatabase.createDataBase();
            dbUpgradeCall();
        } catch (Exception e) {
            android.util.Log.e(LOG_TAG, "Database init failed: " + e.getMessage());
        }

        dataAccessHandler = new DataAccessHandler(this);

        if (!CommonUtils.isNetworkAvailable(this)) {
            UiUtils.showCustomToastMessage("Please check your network connection", this, 1);
        } else {
            startMasterSync();
        }
    }
    private void ensureDatabaseDirectory() {
        File dbDir = new File(Environment.getExternalStorageDirectory(), "palm60_Files/3F_Database");
        if (!dbDir.exists()) {
            boolean isCreated = dbDir.mkdirs();
            if (!isCreated) {
                android.util.Log.e(LOG_TAG, "Failed to create database directory");
            }
        }
    }

    //Request Permissions Result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (isPermissionGranted()) {
                initializeApp();
            } else {
                Toast.makeText(this, "Required permissions not granted", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
    //Perform Master Sync
    public void startMasterSync() {

        if (CommonUtils.isNetworkAvailable(this) && !sharedPreferences.getBoolean(CommonConstants.IS_MASTER_SYNC_SUCCESS, false)) {
            DataSyncHelper.performMasterSync(this, PrefUtil.getBool(this, CommonConstants.IS_MASTER_SYNC_SUCCESS), new ApplicationThread.OnComplete() {
                @Override
                public void execute(boolean success, Object result, String msg) {
                    ProgressBar.hideProgressBar();
                    if (success) {
                        sharedPreferences.edit().putBoolean(CommonConstants.IS_MASTER_SYNC_SUCCESS, true).apply();
                        goToLogin();
                    } else {
                        Log.v(LOG_TAG, "Master sync failed: " + msg);
                        ApplicationThread.uiPost(LOG_TAG, "master sync message", () -> {
                            UiUtils.showCustomToastMessage("Data syncing failed", SplashScreen.this, 1);
                            goToLogin();
                        });
                    }
                }
            });
        } else {
            goToLogin();
        }
    }

    private void goToLogin() {
        startActivity(new Intent(this, LoginScreen.class));
        finish();
    }




    //Db Upgrade Method
    public void dbUpgradeCall() {
        DataAccessHandler dataAccessHandler = new DataAccessHandler(SplashScreen.this, false);
        String count = dataAccessHandler.getCountValue(Queries.getInstance().UpgradeCount());
        if (TextUtils.isEmpty(count) || Integer.parseInt(count) == 0) {
            PrefUtil.putBool(SplashScreen.this, CommonConstants.IS_FRESH_INSTALL, true);
        }
    }

//    public static final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            //Log.d(TAG,"Enter mUsbReceiver.onReceive()");
//            if (ACTION_USB_PERMISSION.equals(action)) {
//                synchronized (this) {
//                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
//                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
//                        if(device != null){
//                            //DEBUG Log.d(TAG, "Vendor ID : " + device.getVendorId() + "\n");
//                            //DEBUG Log.d(TAG, "Product ID: " + device.getProductId() + "\n");
//                            /*debugMessage("USB BroadcastReceiver VID : " + device.getVendorId() + "\n");
//                            debugMessage("USB BroadcastReceiver PID: " + device.getProductId() + "\n");*/
//                        }
//                        else
//                            android.util.Log.e("TAG", "mUsbReceiver.onReceive() Device is null");
//                    }
//                    else
//                        android.util.Log.e("TAG", "mUsbReceiver.onReceive() permission denied for device " + device);
//                }
//            }
//        }
//    };

//    public void onResume() {
//        super.onResume();
//
//        registerReceiver(SplashScreen.mUsbReceiver, filter);
//        long error = sgfplib.Init(SGFDxDeviceName.SG_DEV_AUTO);
//        if (error != SGFDxErrorCode.SGFDX_ERROR_NONE) {
//            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
//            if (error == SGFDxErrorCode.SGFDX_ERROR_DEVICE_NOT_FOUND)
//                dlgAlert.setMessage("The attached fingerprint device is not supported on Android");
//            else
//                dlgAlert.setMessage("Fingerprint device initialization failed!");
//            dlgAlert.setTitle("SecuGen Fingerprint SDK");
//            dlgAlert.setPositiveButton("OK",
//                    new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int whichButton) {
//                            finish();
//                            return;
//                        }
//                    }
//            );
//            dlgAlert.setCancelable(false);
//            dlgAlert.create().show();
//        } else {
//            UsbDevice usbDevice = sgfplib.GetUsbDevice();
//            if (usbDevice == null) {
//                AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
//                dlgAlert.setMessage("SecuGen fingerprint sensor not found!");
//                dlgAlert.setTitle("SecuGen Fingerprint SDK");
//                dlgAlert.setPositiveButton("OK",
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int whichButton) {
//                                finish();
//                                return;
//                            }
//                        }
//                );
//                dlgAlert.setCancelable(false);
//                dlgAlert.create().show();
//            }
//        }
//    }
}
