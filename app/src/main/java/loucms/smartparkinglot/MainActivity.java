package loucms.smartparkinglot;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String TAG="MainActivity";
    private static final int REQUEST_BLUETOOTH_PERMISSION = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 2;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION = 3;
    private static final int REQUEST_CAPTURE_VIDEO_OUTPUT=4;
    private static final int REQUEST_BLUETOOTH_ENABLE=5;
    private BluetoothAdapter mBluetoothAdapter;
    private ImageButton openBluetooth,searchBluetoothDevices,drawGridLines,camera,
            cameraSwitch,cameraSetter,screenProjectionRecorder,showThreeCharts;
    public static int systemSS=0;
    public static byte[] time = new byte[4];

    private Chronometer chronometer;
    private TextView showTime;
    public static File applicationFolder;
    public static boolean mExternalStorageAvailable = false;
    public static boolean mExternalStorageWriteable = false;

    private Button ccam,caam,ael,aemode,afmode,awblock,awbmode,blacklevellock
            ,effectMode,controlMode,sceneMode,videoStableMode,noiseReductionMode,sensorTestPatternMode;
    private SeekBar seekBar;
    private RelativeLayout cameraSettingDetailLayout;


    private ListView mPairedListView,mNewDevicesListview;
    private bluetoothDevicesCustomAdapter mPairedDeviceAdapter,mNewDeviceAdapter;
    private ArrayList<BluetoothDevice> mPairedDeviceList,mNewDeviceList;
    private boolean turnOnBluetoothDevicesList = false;
    private ViewGroup viewGroup,viewGroup_forCamera,chartContainer;
    public  static int connectBTs = 0;//to record how many bluetooth devices are connected
    private bluetooth[] connectedBluetoothDevices = new bluetooth[7];//at most 7 bluetooth devices can connect
    public int focusIndex = 0;//specify which device is be focused now
    public int multiFoucusIndex = 0;
    public static boolean drawGridLine=false;
    public static boolean isCameraPreviewOpened = false;
    private camera mCamera;
    private screenProjector screenProjector;
    private boolean isScreenProjectionRecording=false;
    public static boolean isBluetoothDevicesRecording = false;
    public Handler mainThreadHandler;
    private boolean isOpenThreeCharts=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate phase");
        setContentView(R.layout.activity_main);

        chronometer = (Chronometer)findViewById(R.id.currenttime);
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.setOnChronometerTickListener(timelistener);
        chronometer.start();
        showTime=(TextView)findViewById(R.id.showtimenow);
        checkWriteExternalStoragePermission();
        applicationFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),"smartParkingSpace");
        //applicationFolder = new File("/storage/emulated/0/6E21-BD45");
        Log.d(TAG,""+applicationFolder.toString());
        if(!applicationFolder.exists()){
            Log.d(TAG,applicationFolder.getAbsolutePath()+" doesn't exist");
            applicationFolder.mkdir();
        }else{
            Log.d(TAG,applicationFolder.getAbsolutePath()+" exist");
        }
        /*if(applicationFolder.canWrite()){
            Log.d(TAG,applicationFolder.getAbsolutePath()+" is writable");
        }else{
            Log.d(TAG,applicationFolder.getAbsolutePath()+" isn't writable");
        }
        applicationFolder.setWritable(true);
        if(applicationFolder.canWrite()){
            Log.d(TAG,applicationFolder.getAbsolutePath()+" is writable via setting writable");
        }else{
            Log.d(TAG,applicationFolder.getAbsolutePath()+" isn't writable via setting writable");
        }*/
        registerBroadcast();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.disable();
        //mNewDeviceList = new ArrayList<BluetoothDevice>();
        mPairedListView = (ListView)findViewById(R.id.pairedList);
        openBluetooth = (ImageButton)findViewById(R.id.openBluetoothfunction);
        openBluetooth.setOnClickListener(listener);
        searchBluetoothDevices = (ImageButton)findViewById(R.id.searchBluetooth);
        searchBluetoothDevices.setOnClickListener(listener);
        drawGridLines=(ImageButton)findViewById(R.id.gridLine);
        drawGridLines.setOnClickListener(listener);
        showThreeCharts = (ImageButton)findViewById(R.id.showthreecharts);
        showThreeCharts.setOnClickListener(listener);
        viewGroup = findViewById(R.id.container);
        viewGroup_forCamera = findViewById(R.id.containerForCamera);
        chartContainer = findViewById(R.id.chartcontainer);
        camera = (ImageButton)findViewById(R.id.camera);
        camera.setOnClickListener(listener);
        cameraSwitch = (ImageButton)findViewById(R.id.cameraswitcher);
        cameraSwitch.setOnClickListener(listener);
        cameraSetter = (ImageButton)findViewById(R.id.cameraSettings);
        cameraSetter.setOnClickListener(listener);
        cameraSettingDetailLayout = (RelativeLayout)findViewById(R.id.cameraSettingDetail);
        ccam=(Button)findViewById(R.id.CCAM);
        ccam.setOnClickListener(CameraCharacteristicslistener);
        caam=(Button)findViewById(R.id.CAAM);
        caam.setOnClickListener(CameraCharacteristicslistener);
        ael=(Button)findViewById(R.id.AEL);
        ael.setOnClickListener(CameraCharacteristicslistener);
        aemode=(Button)findViewById(R.id.AEMODE);
        aemode.setOnClickListener(CameraCharacteristicslistener);
        afmode=(Button)findViewById(R.id.AFMODE);
        afmode.setOnClickListener(CameraCharacteristicslistener);
        awblock=(Button)findViewById(R.id.AWBLOCK);
        awblock.setOnClickListener(CameraCharacteristicslistener);
        awbmode=(Button)findViewById(R.id.AWBMODE);
        awbmode.setOnClickListener(CameraCharacteristicslistener);
        blacklevellock=(Button)findViewById(R.id.BlacklevelLock);
        blacklevellock.setOnClickListener(CameraCharacteristicslistener);
        effectMode=(Button)findViewById(R.id.EFFECTMode);
        effectMode.setOnClickListener(CameraCharacteristicslistener);
        controlMode=(Button)findViewById(R.id.CONTROLMode);
        controlMode.setOnClickListener(CameraCharacteristicslistener);
        sceneMode=(Button)findViewById(R.id.SCENEMode);
        sceneMode.setOnClickListener(CameraCharacteristicslistener);
        videoStableMode=(Button)findViewById(R.id.VideoStableMode);
        videoStableMode.setOnClickListener(CameraCharacteristicslistener);
        noiseReductionMode=(Button)findViewById(R.id.noiseReductionMode);
        noiseReductionMode.setOnClickListener(CameraCharacteristicslistener);
        sensorTestPatternMode=(Button)findViewById(R.id.sensorTestPatternMode);
        sensorTestPatternMode.setOnClickListener(CameraCharacteristicslistener);
        screenProjectionRecorder = (ImageButton)findViewById(R.id.screen_projection_recorder);
        screenProjectionRecorder.setOnClickListener(listener);

        mainThreadHandler = new Handler(getMainLooper()){
            @Override
            public void handleMessage(Message msg){
                if(msg.what ==mCamera.REQUEST_TRANSFORM_TEXTURE_VIEW){
                    if(mCamera!=null){
                        if(mCamera.cameraTextureView!=null){
                            Log.d(TAG,"adjust textureview");
                            RectF viewRect = new RectF(0,0,viewGroup_forCamera.getWidth(),viewGroup_forCamera.getHeight());
                            Log.d(TAG,"view-width = "+viewGroup_forCamera.getWidth()+" view-height = "+viewGroup_forCamera.getHeight()
                            +" centerX = "+viewRect.centerX()+" centerY = "+ viewRect.centerY()+" mCamera.cameraTextureView.getWidth() = "
                            +mCamera.cameraTextureView.getWidth()+" mCamera.cameraTextureView.getHeight() = "+mCamera.cameraTextureView.getHeight());
                            float centerX = viewRect.centerX();
                            float centerY = viewRect.centerY();
                            Matrix matrix = new Matrix();
                            matrix.postScale(0.6f,2.0f,centerX,centerY);
                            matrix.postRotate(-90,centerX,centerY);
                            mCamera.cameraTextureView.setTransform(matrix);
                        }
                    }

                }
            }
        };

    }
    public Chronometer.OnChronometerTickListener timelistener = new Chronometer.OnChronometerTickListener() {
        @Override
        public void onChronometerTick(Chronometer chronometer) {
            systemSS++;
            showTime.setText(""+systemSS);
            time[0] = (byte)((systemSS/1000)+48);
            time[1] = (byte)((systemSS/100)%10+48);
            time[2] = (byte)((systemSS/10)%10+48);
            time[3] = (byte)((systemSS%10)+48);
        }
    };
    /*private class urlthread extends Thread{
        private URL myurl;

        public urlthread(URL url){
            myurl = url;
            try {
                myURLConnection = myurl.openConnection();
                myURLConnection.connect();
                myURLConnection.setDoOutput(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(myURL.openStream()));
                Log.d(TAG,in.readLine());
                myURLConnection = (HttpURLConnection)myURL.openConnection();

                myURLConnection.connect();
                myURLConnection.setDoOutput(true);
                myURLConnection.setChunkedStreamingMode(0);
                myURLConnection.setRequestMethod("POST");
                bluetoothDataOutputWriter = new OutputStreamWriter(myURLConnection.getOutputStream());
                myReader = new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()));
                if(bluetoothDataOutputWriter!=null){
                    Log.d(TAG,"bluetoothDataOutputWriter is not null");
                    Log.d(TAG,"myURLConnection response message : "+myURLConnection.getResponseMessage());
                    Log.d(TAG,"myURLConnection response code : "+myURLConnection.getResponseCode());
                    bluetoothDataOutputWriter.write(1);
                    BufferedReader inn = new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()));
                    Log.d(TAG,inn.readLine());
                }
                else{
                    Log.d(TAG,"bluetoothDataOutputWriter is null");
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "openConnection() failed!");
            }
        }

    }*/
    public Button.OnClickListener CameraCharacteristicslistener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isCameraPreviewOpened) {
                switch (v.getId()) {
                    case R.id.CCAM:
                        mCamera.setCOLOR_CORRECTION_ABERRATION_MODE(mCamera.cameraRequestBuilder, String.valueOf(ccam.getText()));
                        ccam.setText(mCamera.getCOLOR_CORRECTION_ABERRATION_MODE());
                        mCamera.resetCaptureRequestDetail(mCamera.cameraRequestBuilder);
                        break;
                    case R.id.CAAM:
                        mCamera.setCONTROL_AE_ANTIBANDING_MODE(mCamera.cameraRequestBuilder, String.valueOf(caam.getText()));
                        caam.setText(mCamera.getCONTROL_AE_ANTIBANDING_MODE());
                        mCamera.resetCaptureRequestDetail(mCamera.cameraRequestBuilder);
                        break;
                    case R.id.AEL:
                        mCamera.setCONTROL_AE_LOCK(mCamera.cameraRequestBuilder, String.valueOf(ael.getText()));
                        ael.setText(mCamera.getCONTROL_AE_LOCK());
                        mCamera.resetCaptureRequestDetail(mCamera.cameraRequestBuilder);
                        break;
                    case R.id.AEMODE:
                        mCamera.setCONTROL_AE_MODE(mCamera.cameraRequestBuilder, String.valueOf(aemode.getText()));
                        aemode.setText(mCamera.getCONTROL_AE_MODE());
                        mCamera.resetCaptureRequestDetail(mCamera.cameraRequestBuilder);
                        break;
                    case R.id.AFMODE:
                        mCamera.setCONTROL_AF_MODE(mCamera.cameraRequestBuilder, String.valueOf(afmode.getText()));
                        afmode.setText(mCamera.getCONTROL_AF_MODE());
                        mCamera.resetCaptureRequestDetail(mCamera.cameraRequestBuilder);
                        break;
                    case R.id.AWBLOCK:
                        mCamera.setCONTROL_AWB_LOCK(mCamera.cameraRequestBuilder, String.valueOf(awblock.getText()));
                        awblock.setText(mCamera.getCONTROL_AWB_LOCK());
                        mCamera.resetCaptureRequestDetail(mCamera.cameraRequestBuilder);
                        break;
                    case R.id.AWBMODE:
                        mCamera.setCONTROL_AWB_MODE(mCamera.cameraRequestBuilder, String.valueOf(awbmode.getText()));
                        awbmode.setText(mCamera.getCONTROL_AWB_MODE());
                        mCamera.resetCaptureRequestDetail(mCamera.cameraRequestBuilder);
                        break;
                    case R.id.BlacklevelLock:
                        mCamera.setBLACK_LEVEL_LOCK(mCamera.cameraRequestBuilder, String.valueOf(blacklevellock.getText()));
                        blacklevellock.setText(mCamera.getBLACK_LEVEL_LOCK());
                        mCamera.resetCaptureRequestDetail(mCamera.cameraRequestBuilder);
                        break;
                    case R.id.EFFECTMode:
                        mCamera.setCONTROL_EFFECT_MODE(mCamera.cameraRequestBuilder, String.valueOf(effectMode.getText()));
                        effectMode.setText(mCamera.getCONTROL_EFFECT_MODE());
                        mCamera.resetCaptureRequestDetail(mCamera.cameraRequestBuilder);
                        break;
                    case R.id.CONTROLMode:
                        mCamera.setCONTROL_MODE(mCamera.cameraRequestBuilder, String.valueOf(controlMode.getText()));
                        controlMode.setText(mCamera.getCONTROL_MODE());
                        mCamera.resetCaptureRequestDetail(mCamera.cameraRequestBuilder);
                        break;
                    case R.id.SCENEMode:
                        mCamera.setCONTROL_SCENE_MODE(mCamera.cameraRequestBuilder, String.valueOf(sceneMode.getText()));
                        sceneMode.setText(mCamera.getCONTROL_SCENE_MODE());
                        mCamera.resetCaptureRequestDetail(mCamera.cameraRequestBuilder);
                        break;
                    case R.id.VideoStableMode:
                        mCamera.setCONTROL_VIDEO_STABILIZATION_MODE(mCamera.cameraRequestBuilder, String.valueOf(videoStableMode.getText()));
                        videoStableMode.setText(mCamera.getCONTROL_VIDEO_STABILIZATION_MODE());
                        mCamera.resetCaptureRequestDetail(mCamera.cameraRequestBuilder);
                        break;
                    case R.id.noiseReductionMode:
                        mCamera.setNOISE_REDUCTION_MODE(mCamera.cameraRequestBuilder, String.valueOf(noiseReductionMode.getText()));
                        noiseReductionMode.setText(mCamera.getNOISE_REDUCTION_MODE());
                        mCamera.resetCaptureRequestDetail(mCamera.cameraRequestBuilder);
                        break;
                    case R.id.sensorTestPatternMode:
                        mCamera.setSENSOR_TEST_PATTERN_MODE(mCamera.cameraRequestBuilder, String.valueOf(sensorTestPatternMode.getText()));
                        sensorTestPatternMode.setText(mCamera.getSENSOR_TEST_PATTERN_MODE());
                        mCamera.resetCaptureRequestDetail(mCamera.cameraRequestBuilder);
                        break;
                }
            }
        }
    };
    public void registerBroadcast(){
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothBroadcastReceiver,filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(bluetoothBroadcastReceiver,filter);
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetoothBroadcastReceiver,filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bluetoothBroadcastReceiver,filter);
    }
    @Override
    public void onStart(){
        super.onStart();
        Log.d(TAG,"onStart phase");
    }
    @Override
    public void onResume(){
        super.onResume();
        Log.d(TAG,"onResume phase");

    }
    public void showPairedBluetoothDevices(){
        mPairedDeviceList = new ArrayList<BluetoothDevice>();
        Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
        for(BluetoothDevice device:bondedDevices){
            mPairedDeviceList.add(device);
        }
        mPairedDeviceAdapter = new bluetoothDevicesCustomAdapter(MainActivity.this
                ,mPairedDeviceList);
        mPairedListView.setAdapter(mPairedDeviceAdapter);
        mPairedListView.setOnItemClickListener(listViewListener);
        mPairedListView.setVisibility(View.VISIBLE);
    }
    public ImageButton.OnClickListener listener = new ImageButton.OnClickListener(){
        @Override
        public void onClick(View v){
            switch(v.getId()){
                case R.id.openBluetoothfunction:
                    if(connectBTs == 0){
                        checkBluetoothPermission();
                        checkBTEnableDisable();
                    }else{
                        for(int i =0;i<7;i++){
                            if(connectedBluetoothDevices[i]!=null)
                                connectedBluetoothDevices[i].stopBluetooth();
                        }
                        if(mBluetoothAdapter.isEnabled()){
                            mBluetoothAdapter.disable();
                        }
                        openBluetooth.setImageResource(R.drawable.ic_bluetooth_24dp);
                    }
                    break;
                case R.id.searchBluetooth:
                    Toast.makeText(MainActivity.this,"press searchBluetooth",Toast.LENGTH_SHORT).show();
                    if(mBluetoothAdapter==null){
                        Log.d(TAG,"mBluetoothAdapter is null");
                        finish();
                    }
                    if(!mBluetoothAdapter.isEnabled()){
                        Toast.makeText(MainActivity.this,"Please turn on the Bluetooth.",Toast.LENGTH_SHORT).show();
                    }else if(!turnOnBluetoothDevicesList){
                        Log.d(TAG,"showPairedBluetoothDevices phase");
                        showPairedBluetoothDevices();
                        turnOnBluetoothDevicesList = true;
                    }else if(turnOnBluetoothDevicesList){
                        Log.d(TAG,"mPairedListView.setVisibility(View.GONE)");
                        mPairedListView.setVisibility(View.GONE);
                        turnOnBluetoothDevicesList = false;
                    }
                    break;
                case R.id.gridLine:
                    if(!drawGridLine){
                        drawGridLine = true;
                        drawGridLines.setImageResource(R.drawable.ic_grid_on_24dp);
                    }else{
                        drawGridLine = false;
                        drawGridLines.setImageResource(R.drawable.ic_grid_off_24dp);
                    }
                    break;
                case R.id.camera:
                    Log.d(TAG,"camera is pressed");
                    if(!isCameraPreviewOpened){
                        checkCameraPermission();
                        Log.d(TAG,"mCamera = "+mCamera);
                    }else{
                        isCameraPreviewOpened= false;
                        if(mCamera!=null){
                            mCamera.cameraClose();
                            mCamera.stopBackgroundThread();
                            if(mCamera.cameraSurfaceView!=null){
                                viewGroup_forCamera.removeView(mCamera.cameraSurfaceView);
                            }
                            if(mCamera.cameraTextureView!=null){
                                viewGroup_forCamera.removeView(mCamera.cameraTextureView);
                            }
                            mCamera=null;
                        }
                    }
                    break;
                case R.id.cameraswitcher:
                    if(isCameraPreviewOpened){
                        if(mCamera!=null){
                            if(mCamera.cameraDirection== CameraCharacteristics.LENS_FACING_BACK){
                                mCamera.cameraClose();
                                mCamera.stopBackgroundThread();
                                viewGroup_forCamera.removeView(mCamera.cameraSurfaceView);
                                mCamera=null;
                                mCamera = new camera(MainActivity.this,viewGroup_forCamera,CameraCharacteristics.LENS_FACING_FRONT,mainThreadHandler);
                                //mCamera.setupCamera(CameraCharacteristics.LENS_FACING_FRONT);
                            }else{
                                mCamera.cameraClose();
                                mCamera.stopBackgroundThread();
                                viewGroup_forCamera.removeView(mCamera.cameraSurfaceView);
                                mCamera =null;
                                mCamera = new camera(MainActivity.this,viewGroup_forCamera,CameraCharacteristics.LENS_FACING_BACK,mainThreadHandler);
                                //mCamera.setupCamera(CameraCharacteristics.LENS_FACING_BACK);
                            }
                        }
                    }
                    break;
                case R.id.cameraSettings:
                    if(isCameraPreviewOpened){
                        if(cameraSettingDetailLayout.getVisibility()==View.GONE){
                            cameraSettingDetailLayout.setVisibility(View.VISIBLE);
                        }else
                            cameraSettingDetailLayout.setVisibility(View.GONE);
                    }
                    break;
                case R.id.screen_projection_recorder:
                    if(isCameraPreviewOpened){
                        if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
                            if(isScreenProjectionRecording){
                                screenProjectionRecorder.setImageResource(R.drawable.ic_videocam_not_recording_24dp);
                                screenProjector.stopRecord();
                                isScreenProjectionRecording= false;
                                if(isBluetoothDevicesRecording)
                                    isBluetoothDevicesRecording=false;
                            }else{
                                screenProjector = new screenProjector(MainActivity.this,mCamera.surface);
                                isBluetoothDevicesRecording = true;
                                if(screenProjector.getMediaProjectionManager()!=null){
                                    if(screenProjector.getIntent_Projection()!=null){
                                        startActivityForResult(screenProjector.getIntent_Projection(),REQUEST_CAPTURE_VIDEO_OUTPUT);
                                    }
                                }
                            }
                        }
                    }else{
                        Toast.makeText(MainActivity.this,"Please turn on Camera",Toast.LENGTH_SHORT).show();
                    }

                    break;
                case R.id.showthreecharts:
                    if(connectBTs>=3){
                        if(!isOpenThreeCharts){
                            isOpenThreeCharts=true;
                            int xPosition=0;
                            connectedBluetoothDevices[multiFoucusIndex].megnetoChart.myGLSurfaceView.setX(xPosition);
                            connectedBluetoothDevices[multiFoucusIndex].deviceCircleImageView.setOutsideBorderColorResource(R.color.outSideBorderColor);
                            connectedBluetoothDevices[(multiFoucusIndex+1)%connectBTs].megnetoChart.myGLSurfaceView.setX(xPosition+chartContainer.getWidth()/3-10);
                            connectedBluetoothDevices[(multiFoucusIndex+1)%connectBTs].deviceCircleImageView.setOutsideBorderColorResource(R.color.outSideBorderColor);
                            connectedBluetoothDevices[(multiFoucusIndex+2)%connectBTs].megnetoChart.myGLSurfaceView.setX(xPosition+chartContainer.getWidth()*2/3-20);
                            connectedBluetoothDevices[(multiFoucusIndex+2)%connectBTs].deviceCircleImageView.setOutsideBorderColorResource(R.color.outSideBorderColor);

                        }else{
                            isOpenThreeCharts=false;
                            int xPosition = 0;
                            connectedBluetoothDevices[multiFoucusIndex].megnetoChart.myGLSurfaceView.setX(xPosition+chartContainer.getWidth()/3-10);
                            connectedBluetoothDevices[multiFoucusIndex].deviceCircleImageView.setOutsideBorderColorResource(R.color.outSideBorderColor);
                            connectedBluetoothDevices[(multiFoucusIndex+1)%connectBTs].megnetoChart.myGLSurfaceView.setX(-2000);
                            connectedBluetoothDevices[(multiFoucusIndex+1)%connectBTs].deviceCircleImageView.setOutsideBorderColorResource(R.color.transparent);
                            connectedBluetoothDevices[(multiFoucusIndex+2)%connectBTs].megnetoChart.myGLSurfaceView.setX(-2000);
                            connectedBluetoothDevices[(multiFoucusIndex+2)%connectBTs].deviceCircleImageView.setOutsideBorderColorResource(R.color.transparent);
                            multiFoucusIndex = focusIndex;
                        }
                    }
            }
        }
    };
    public void onPause(){
        super.onPause();

    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        if(mBluetoothAdapter!=null)
            mBluetoothAdapter.cancelDiscovery();
        unregisterReceiver(bluetoothBroadcastReceiver);
        viewGroup.removeAllViews();

        if(mCamera!=null){
            viewGroup_forCamera.removeView(mCamera.cameraSurfaceView);
            mCamera.cameraClose();
        }
        if(chronometer!=null){
            chronometer.stop();
        }
        finish();
    }
    public ListView.OnItemClickListener listViewListener = new ListView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            LinearLayout layout = (LinearLayout)findViewById(R.id.bluetoothDevices);
            if(connectBTs<7){
                connectedBluetoothDevices[connectBTs] = new bluetooth((BluetoothDevice)parent.getItemAtPosition(position),MainActivity.this,layout,chartContainer);
                if(connectBTs==0){
                    focusIndex = connectBTs;
                    multiFoucusIndex = focusIndex;
                    Log.d(TAG,"focusIndex = "+focusIndex);
                    if(getWindowManager().getDefaultDisplay().getRotation()== Surface.ROTATION_0||getWindowManager().getDefaultDisplay().getRotation()== Surface.ROTATION_180)
                        connectedBluetoothDevices[connectBTs].megnetoChart.myGLSurfaceView.setX(0);
                    else
                        connectedBluetoothDevices[connectBTs].megnetoChart.myGLSurfaceView.setX(chartContainer.getWidth()*1/3-10);

                    connectedBluetoothDevices[connectBTs].deviceCircleImageView.setOutsideBorderColor(R.color.outSideBorderColor);
                    connectBTs++;
                    Log.d(TAG,"connectBTs = "+ connectBTs);
                }else{
                    connectBTs++;
                    Log.d(TAG,"connectBTs = "+ connectBTs);
                }
            }
            mPairedListView.setVisibility(View.GONE);
        }
    };
    public void checkBluetoothPermission(){
        if(checkSelfPermission(Manifest.permission.BLUETOOTH)!= PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH,Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.BLUETOOTH_PRIVILEGED,Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_BLUETOOTH_PERMISSION);
        }
    }
    public void checkCameraPermission(){
        if(checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.CAMERA},REQUEST_CAMERA_PERMISSION);
        }else {
            mCamera = new camera(MainActivity.this,viewGroup_forCamera,CameraCharacteristics.LENS_FACING_BACK,mainThreadHandler);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[]permissions, @NonNull int[]grantResults){
        switch(requestCode){
            case REQUEST_BLUETOOTH_PERMISSION:
                if(grantResults.length!=4){
                    Log.d(TAG,"failed to get Bluetooth permission");
                    finish();
                }else{
                    for(int i = 0;i<grantResults.length;i++){
                        Log.d(TAG,""+permissions[i]+" : "+grantResults[i]);
                        if(grantResults[i]==-1){
                            //grant is 0,denied is -1
                            finish();
                        }
                    }
                }
                break;
            case REQUEST_CAMERA_PERMISSION:
                if(grantResults[0]!=PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "failed to get Camera permission");
                }else{
                    Log.d(TAG,"create new camera");
                    mCamera = new camera(MainActivity.this,viewGroup_forCamera,CameraCharacteristics.LENS_FACING_BACK,mainThreadHandler);
                }
                break;
            case REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION:
                if(grantResults[0]!=PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG,"failed to get WRITE_EXTERNAL_STORAGE_PERMISSION");
                }else{
                    Log.d(TAG,"Get WRITE_EXTERNAL_STORAGE_PERMISSION");
                }
        }
    }
    public void checkBTEnableDisable(){
        Log.d(TAG,"check Bluetooth is enabled or not");
        if(mBluetoothAdapter==null){
            Log.d(TAG,"mBluetoothAdapter is not provided,quit!!");
            finish();
        }else if(checkSelfPermission(Manifest.permission.BLUETOOTH)!=PackageManager.PERMISSION_GRANTED){
            Log.d(TAG,"Bluetooth permission isn't granted,quit!!");
            finish();
        }
        else{
            if(!mBluetoothAdapter.isEnabled()){
                Log.d(TAG,"bluetooth is not enabled");
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent,REQUEST_BLUETOOTH_ENABLE);
            }else{
                mBluetoothAdapter.disable();
                openBluetooth.setImageResource(R.drawable.ic_bluetooth_24dp);
                Log.d(TAG,"bluetooth is disabled");
            }
        }
    }
    public void checkWriteExternalStoragePermission(){
        Log.d(TAG,"check Screen projection is enabled or not");
        if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION);
        }
    }
    public void onActivityResult(int requestCode,int resultCode,Intent intent){
        Log.d(TAG,"onActivityResult");
        switch(requestCode){
            case REQUEST_BLUETOOTH_ENABLE:
                if(resultCode == Activity.RESULT_OK){
                    Log.d(TAG,"Bluetooth turns on!!");
                    openBluetooth.setImageResource(R.drawable.ic_bluetooth_enabled_24dp);
                }else if(resultCode==Activity.RESULT_CANCELED){
                    Toast.makeText(this,"Refuse to turn on Bluetooth!!",Toast.LENGTH_SHORT).show();
                    Log.d(TAG,"Refuse to turn on Bluetooth!!");
                }
                break;
            case REQUEST_CAPTURE_VIDEO_OUTPUT:
                if(resultCode==Activity.RESULT_OK){
                    screenProjector.setMediaProjection(screenProjector.
                            getMediaProjectionManager().getMediaProjection(resultCode,intent));
                    screenProjector.setupRecordDetail();
                    screenProjector.startRecord();
                    screenProjectionRecorder.setImageResource(R.drawable.ic_videocam_24dp);
                    isScreenProjectionRecording= true;
                }else{
                    Toast.makeText(this,"Refuse to turn on Screen Projection",Toast.LENGTH_SHORT).show();
                    screenProjector.setMediaProjection(null);
                    screenProjector.stopRecord();
                }

        }
    }
    public BroadcastReceiver bluetoothBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.ERROR);
                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG,"Bluetooth STATE_OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG,"Bluetooth STATE_ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG,"Bluetooth STATE_TURNING_OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG,"Bluetooth STATE_TURNING_ON");
                        break;
                }
            }else if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)){
                Log.d(TAG,"BluetoothAdapter Discovery STARTED!!");
            }else if(action.equals(BluetoothDevice.ACTION_FOUND)){
                Log.d(TAG,"BluetoothDevice ACTION_FOUND");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getBondState()!=BluetoothDevice.BOND_BONDED){
                    Log.d(TAG,"which device is found "+device.getName()+" : "+device.getAddress());
                    mNewDeviceList.add(device);
                }
            }
        }
    };
    @Override
    public boolean onTouchEvent(MotionEvent e){
        float x1=0;
        float x2=0;
        if (!isOpenThreeCharts) {
            if(connectBTs>1){
                switch(e.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        x1 = e.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                        x2 = e.getX();
                        if(x2-x1>100){
                            Log.d(TAG,"swipe right");
                            connectedBluetoothDevices[focusIndex].megnetoChart.myGLSurfaceView.setX(-2000);
                            connectedBluetoothDevices[focusIndex].deviceCircleImageView.setOutsideBorderColorResource(R.color.transparent);

                            if(getWindowManager().getDefaultDisplay().getRotation()== Surface.ROTATION_0||getWindowManager().getDefaultDisplay().getRotation()== Surface.ROTATION_180)
                                connectedBluetoothDevices[(focusIndex+connectBTs+1)%connectBTs].megnetoChart.myGLSurfaceView.setX(0);
                            else
                                connectedBluetoothDevices[(focusIndex+connectBTs+1)%connectBTs].megnetoChart.myGLSurfaceView.setX(chartContainer.getWidth()*1/3-10);

                            connectedBluetoothDevices[(focusIndex+connectBTs+1)%connectBTs].deviceCircleImageView.setOutsideBorderColorResource(R.color.outSideBorderColor);
                            focusIndex = (focusIndex+connectBTs+1)%connectBTs;
                            multiFoucusIndex = focusIndex;

                        }else if(x2-x1<100){
                            Log.d(TAG,"swipe left");
                            connectedBluetoothDevices[focusIndex].megnetoChart.myGLSurfaceView.setX(-2000);
                            connectedBluetoothDevices[focusIndex].deviceCircleImageView.setOutsideBorderColorResource(R.color.transparent);
                            if(getWindowManager().getDefaultDisplay().getRotation()== Surface.ROTATION_0||getWindowManager().getDefaultDisplay().getRotation()== Surface.ROTATION_180)
                                connectedBluetoothDevices[(focusIndex+connectBTs-1)%connectBTs].megnetoChart.myGLSurfaceView.setX(0);
                            else
                                connectedBluetoothDevices[(focusIndex+connectBTs-1)%connectBTs].megnetoChart.myGLSurfaceView.setX(chartContainer.getWidth()*1/3-10);

                            connectedBluetoothDevices[(focusIndex+connectBTs-1)%connectBTs].deviceCircleImageView.setOutsideBorderColorResource(R.color.outSideBorderColor);
                            focusIndex = (focusIndex+connectBTs-1)%connectBTs;
                            multiFoucusIndex = focusIndex;
                        }
                        break;
                }
                return true;
            }else return false;
        }else{
            if(connectBTs>3){
                switch(e.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        x1 = e.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                        x2 = e.getX();
                        if(x2-x1>100){
                            Log.d(TAG,"swipe right");
                            if((multiFoucusIndex+2)==connectBTs){
                                connectedBluetoothDevices[multiFoucusIndex].megnetoChart.myGLSurfaceView.setX(-2000);
                                connectedBluetoothDevices[multiFoucusIndex].deviceCircleImageView.setOutsideBorderColorResource(R.color.transparent);
                                multiFoucusIndex = 0;
                                connectedBluetoothDevices[(multiFoucusIndex+connectBTs+1)%connectBTs].megnetoChart.myGLSurfaceView.setX(0);
                                connectedBluetoothDevices[(multiFoucusIndex+connectBTs+1)%connectBTs].deviceCircleImageView.setOutsideBorderColorResource(R.color.outSideBorderColor);
                                connectedBluetoothDevices[(multiFoucusIndex+connectBTs+2)%connectBTs].megnetoChart.myGLSurfaceView.setX(chartContainer.getWidth()/3-10);
                                connectedBluetoothDevices[(multiFoucusIndex+connectBTs+2)%connectBTs].deviceCircleImageView.setOutsideBorderColorResource(R.color.outSideBorderColor);
                                connectedBluetoothDevices[(multiFoucusIndex+connectBTs+3)%connectBTs].megnetoChart.myGLSurfaceView.setX(chartContainer.getWidth()*2/3-10);
                                connectedBluetoothDevices[(multiFoucusIndex+connectBTs+3)%connectBTs].deviceCircleImageView.setOutsideBorderColorResource(R.color.outSideBorderColor);
                            }else{
                                connectedBluetoothDevices[multiFoucusIndex].megnetoChart.myGLSurfaceView.setX(-2000);
                                connectedBluetoothDevices[multiFoucusIndex].deviceCircleImageView.setOutsideBorderColorResource(R.color.transparent);
                                connectedBluetoothDevices[(multiFoucusIndex+connectBTs+1)%connectBTs].megnetoChart.myGLSurfaceView.setX(0);
                                //connectedBluetoothDevices[(multiFoucusIndex+connectBTs+1)%connectBTs].deviceCircleImageView.setOutsideBorderColorResource(R.color.outSideBorderColor);
                                connectedBluetoothDevices[(multiFoucusIndex+connectBTs+2)%connectBTs].megnetoChart.myGLSurfaceView.setX(chartContainer.getWidth()/3-10);
                                //connectedBluetoothDevices[(multiFoucusIndex+connectBTs+2)%connectBTs].deviceCircleImageView.setOutsideBorderColorResource(R.color.outSideBorderColor);
                                connectedBluetoothDevices[(multiFoucusIndex+connectBTs+3)%connectBTs].megnetoChart.myGLSurfaceView.setX(chartContainer.getWidth()*2/3-10);
                                connectedBluetoothDevices[(multiFoucusIndex+connectBTs+3)%connectBTs].deviceCircleImageView.setOutsideBorderColorResource(R.color.outSideBorderColor);
                                multiFoucusIndex = (multiFoucusIndex+connectBTs+1)%connectBTs;
                            }
                        }else if(x2-x1<100){
                            Log.d(TAG,"swipe left");
                            if(multiFoucusIndex==0){
                                multiFoucusIndex = connectBTs-3;
                                connectedBluetoothDevices[(multiFoucusIndex+connectBTs)%connectBTs].megnetoChart.myGLSurfaceView.setX(0);
                                connectedBluetoothDevices[(multiFoucusIndex+connectBTs)%connectBTs].deviceCircleImageView.setOutsideBorderColorResource(R.color.outSideBorderColor);
                                connectedBluetoothDevices[(multiFoucusIndex+connectBTs+1)%connectBTs].megnetoChart.myGLSurfaceView.setX(chartContainer.getWidth()/3-10);
                                connectedBluetoothDevices[(multiFoucusIndex+connectBTs+1)%connectBTs].deviceCircleImageView.setOutsideBorderColorResource(R.color.outSideBorderColor);
                                connectedBluetoothDevices[(multiFoucusIndex+connectBTs+2)%connectBTs].megnetoChart.myGLSurfaceView.setX(chartContainer.getWidth()*2/3-10);
                                connectedBluetoothDevices[(multiFoucusIndex+connectBTs+2)%connectBTs].deviceCircleImageView.setOutsideBorderColorResource(R.color.outSideBorderColor);

                            }else{
                                connectedBluetoothDevices[(multiFoucusIndex+connectBTs-1)%connectBTs].megnetoChart.myGLSurfaceView.setX(0);
                                connectedBluetoothDevices[(multiFoucusIndex+connectBTs-1)%connectBTs].deviceCircleImageView.setOutsideBorderColorResource(R.color.outSideBorderColor);
                                connectedBluetoothDevices[(multiFoucusIndex+connectBTs)%connectBTs].megnetoChart.myGLSurfaceView.setX(chartContainer.getWidth()/3-10);
                                //connectedBluetoothDevices[(multiFoucusIndex+connectBTs)%connectBTs].deviceCircleImageView.setOutsideBorderColorResource(R.color.outSideBorderColor);
                                connectedBluetoothDevices[(multiFoucusIndex+connectBTs+1)%connectBTs].megnetoChart.myGLSurfaceView.setX(chartContainer.getWidth()*2/3-10);
                                //connectedBluetoothDevices[(multiFoucusIndex+connectBTs+1)%connectBTs].deviceCircleImageView.setOutsideBorderColorResource(R.color.outSideBorderColor);
                                multiFoucusIndex = (multiFoucusIndex+connectBTs-1)%connectBTs;
                            }

                        }
                        break;
                }
                return true;
            }
            else return false;

        }

    }
    /*
    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if(connectBTs>1)
        if(e1.getX()-e2.getX()>150){
            Log.d(TAG,"onFling left");
            connectedBluetoothDevices[focusIndex].megnetoChart.myGLSurfaceView.setZ(-1);
            connectedBluetoothDevices[(focusIndex+connectBTs-1)%connectBTs].megnetoChart.myGLSurfaceView.setZ(1);
            focusIndex = (focusIndex+connectBTs-1)%connectBTs;
        }else if(e2.getX()-e1.getX()>150){
            Log.d(TAG,"onFling right");
            connectedBluetoothDevices[focusIndex].megnetoChart.myGLSurfaceView.setZ(-1);
            connectedBluetoothDevices[(focusIndex+connectBTs+1)%connectBTs].megnetoChart.myGLSurfaceView.setZ(1);
            focusIndex = (focusIndex+connectBTs+1)%connectBTs;
        }

        return true;
    }*/
    /*public void updateExternalStorageState(){
        String state  = Environment.getExternalStorageState();
        if(state.equals(Environment.MEDIA_MOUNTED)){
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        }
        else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
            Toast.makeText(this,"SD card is READ_ONLY",Toast.LENGTH_SHORT).show();
        } else {
            mExternalStorageAvailable = mExternalStorageWriteable = false;
            Toast.makeText(this,"SD card not exist",Toast.LENGTH_SHORT).show();
        }
    }
    public boolean hasExternalStorageFolder(){
        if(Environment.getExternalStorageState(applicationFolder).equals(Environment.MEDIA_UNKNOWN)){
            return false;
        }else return true;

    }*/
}
