package loucms.smartparkinglot;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.util.Arrays;

/**
 * Created by loucms on 11/27/17.
 */

public class camera extends cameraSettingDetail{
    private final static String TAG ="camera";
    private Context mContext;
    private ViewGroup mViewGroup;
    public SurfaceView cameraSurfaceView;
    public TextureView cameraTextureView;
    public SurfaceHolder surfaceHolder;
    public Surface surface;
    public Surface textureviewSurface;
    private LayoutInflater inflater;
    private CameraManager cameraManager;
    private Handler backgroundHandler,mMainThreadHandler;
    private HandlerThread backgroundHandlerThread;
    private String cameraId = "0";//default is back camera
    public int cameraDirection = CameraCharacteristics.LENS_FACING_BACK;
    private CameraDevice cameraDevice;
    public CaptureRequest.Builder cameraRequestBuilder;
    private CameraCaptureSession cameraCaptureSession;
    private StreamConfigurationMap mMap;
    private int g= 0;
    private int rotation;
    private Display mDisplay;
    public final static int REQUEST_TRANSFORM_TEXTURE_VIEW = 33;


    public camera(Context context, ViewGroup viewGroup,int LensFacing,Handler handler ){
        mContext = context;
        inflater = LayoutInflater.from(mContext);
        mDisplay =  ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        rotation = mDisplay.getRotation();
        cameraSurfaceView = (SurfaceView)inflater.inflate(R.layout.camera_surfaceview,viewGroup,false);
        cameraTextureView = (TextureView)inflater.inflate(R.layout.camera_textureview,viewGroup,false);
        cameraManager = (CameraManager)mContext.getSystemService(Context.CAMERA_SERVICE);
        try {
            mMap = cameraManager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        /*if(rotation== Surface.ROTATION_90 ||rotation==Surface.ROTATION_270){
            cameraSurfaceView.setRotation(90);
        }*/
        surfaceHolder = cameraSurfaceView.getHolder();
        surfaceHolder.addCallback(surfaceHolderCallback);
        startBackgroundThread();
        cameraDirection=LensFacing;
        mViewGroup = viewGroup;
        mViewGroup.addView(cameraSurfaceView);
        mViewGroup.addView(cameraTextureView);
        mMainThreadHandler = handler;
    }
    public SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.d(TAG,"surfaceCreated phase");
            if(cameraManager==null){
                Log.d(TAG,"This device doesn't provide camera");
            }else{
                if(mMap !=null){
                    //Size[] a = map.getOutputSizes(SurfaceView.class);
                    //Log.d(TAG,"output size = "+ a.length);
                    Log.d(TAG,"is supported for this surfaceview at surfaceCreated phase "
                            +mMap.isOutputSupportedFor(SurfaceView.class));
                    Log.d(TAG,"is supported for this surface at surfaceCreated phase "
                            +mMap.isOutputSupportedFor(holder.getSurface()));
                     /*for(Size a:map.getOutputSizes(SurfaceView.class)){
                            Log.d(TAG,"width : "+a.getWidth()+" height : "+a.getHeight());
                     }*/
                }
            }
            Log.d(TAG,"surfaceCreated phase holder = "+ holder+" surface = "+holder.getSurface());
            if(cameraManager!=null){

                holder.setFormat(PixelFormat.RGB_565);
                Log.d(TAG,"is supported for this surface at surfaceCreated phase after set Format "
                        +mMap.isOutputSupportedFor(holder.getSurface())+" surface = "+holder.getSurface());
                //holder.setFixedSize(1280,960);
                if(rotation== Surface.ROTATION_0 ||rotation==Surface.ROTATION_180){
                    Log.d(TAG,"camera rotate 90 or 270 and surface is portrait");
                    Log.d(TAG,"width = 1280 height = 720");
                    holder.setFixedSize(1280,720);
                    Log.d(TAG,"is supported for this surface at surfaceCreated phase after set FixedSize "
                            +mMap.isOutputSupportedFor(holder.getSurface())+" surface = "+holder.getSurface());
                }else{
                    Log.d(TAG,"camera rotate 0 or 180 and surface is landscape");
                    holder.setFixedSize(1280,720);
                    Log.d(TAG,"is supported for this surface at surfaceCreated phase after set FixedSize "
                            +mMap.isOutputSupportedFor(holder.getSurface())+" surface = "+holder.getSurface());
                }
                for(int format: mMap.getOutputFormats()){
                    Log.d(TAG,"Output format is(at surfaceCreated) : "+ format+"\n");
                    for(Size size: mMap.getOutputSizes(format)){
                        Log.d(TAG,"output size : width = "+ size.getWidth()+" height = "+ size.getHeight());
                    }
                }
                for(Size size: mMap.getOutputSizes(SurfaceTexture.class)){
                    Log.d(TAG,"output size (from surfacetexture class at surfaceCreated): width = "+ size.getWidth()+" height = "+ size.getHeight());
                }
            }
        }
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            g++;
            Log.d(TAG,"g = "+g);
            Log.d(TAG,"surfaceChanged phase");
            Log.d(TAG,"is supported for this surface at surfaceChanged phase "+mMap.isOutputSupportedFor(holder.getSurface()));
            Log.d(TAG,"surfaceChanged phase holder = "+ holder+" surface"+holder.getSurface()+" format = "+format+" width = "+width
                    +" height = "+height);
            if(g ==2){
                Log.d(TAG,"g = 2 and prepare to setup camera");
                surface = holder.getSurface();
                setupCamera(cameraDirection);
            }
        }
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.d(TAG,"surfaceDestroyed phase");
            /*if(cameraCaptureSession!=null){
                cameraCaptureSession.close();
                cameraCaptureSession = null;
            }
            if(cameraDevice!=null){
                cameraDevice.close();
                cameraDevice = null;
            }
            if(cameraManager!=null){
                cameraManager=null;
            }*/
            //stopBackgroundThread();
        }
    };
    public void setupCamera(int direction){
        Log.d(TAG,"setupCamera phase");
        if(cameraManager!=null){
            if(direction == CameraCharacteristics.LENS_FACING_BACK){
                cameraId = "0";
                try {
                    if(mContext.checkSelfPermission(Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED)
                        cameraManager.openCamera("0",cameraDeviceState_callback,backgroundHandler);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }else{
                cameraId = "1";
                try {
                    if(mContext.checkSelfPermission(Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED)
                        cameraManager.openCamera(cameraId,cameraDeviceState_callback,backgroundHandler);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

        }
    }
    public CameraDevice.StateCallback cameraDeviceState_callback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.d(TAG,"onOpened phase");
            cameraDevice = camera;
            SurfaceTexture texture = cameraTextureView.getSurfaceTexture();
            texture.setDefaultBufferSize(1280,720);
            textureviewSurface =new Surface(texture);
            try {
                cameraRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                if(rotation==Surface.ROTATION_90||rotation==Surface.ROTATION_270){
                    Message msg  = mMainThreadHandler.obtainMessage();
                    msg.what = REQUEST_TRANSFORM_TEXTURE_VIEW;
                    msg.sendToTarget();
                    cameraRequestBuilder.addTarget(textureviewSurface);
                    //cameraRequestBuilder.addTarget(surface);
                    cameraDevice.createCaptureSession(Arrays.asList(textureviewSurface),CameraSessionStateCallback,backgroundHandler);
                }else{
                    cameraRequestBuilder.addTarget(surface);
                    //cameraRequestBuilder.addTarget(textureviewSurface);
                    cameraDevice.createCaptureSession(Arrays.asList(surface),CameraSessionStateCallback,backgroundHandler);
                }

                //cameraDevice.createCaptureSession(Arrays.asList(surface),CameraSessionStateCallback,backgroundHandler);
            } catch (CameraAccessException e1) {
                e1.printStackTrace();
            }
        }
        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.d(TAG,"onDisconnected phase");
            if(camera!=null)
                camera.close();
            if(cameraManager!=null)
                cameraManager = null;
            cameraDevice =null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.d(TAG,"onError phase");
            if(error == CameraDevice.StateCallback.ERROR_CAMERA_IN_USE){
                Log.d(TAG,"onError phase : ERROR_CAMERA_IN_USE");
                //indicating that the camera device is in use already.
            }else if(error == CameraDevice.StateCallback.ERROR_CAMERA_DEVICE){
                Log.d(TAG,"onError phase : ERROR_CAMERA_DEVICE");
                //indicating that the camera device has encountered a fatal error.
            }else if(error == CameraDevice.StateCallback.ERROR_CAMERA_DISABLED){
                Log.d(TAG,"onError phase : ERROR_CAMERA_DISABLED");
                //indicating that the camera device could not be opened due to a device policy.
            }else if(error == CameraDevice.StateCallback.ERROR_CAMERA_SERVICE){
                Log.d(TAG,"onError phase : ERROR_CAMERA_SERVICE");
                //indicating that the camera service has encountered a fatal error.
            }else if(error == CameraDevice.StateCallback.ERROR_MAX_CAMERAS_IN_USE){
                Log.d(TAG,"onError phase : ERROR_MAX_CAMERAS_IN_USE");
                //indicating that the camera device could not be opened
                // because there are too many other open camera devices.
            }
        }
    };
    public CameraCaptureSession.StateCallback CameraSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            Log.d(TAG,"onConfigured phase");
            cameraCaptureSession = session;
            if(cameraDevice==null){
                return;
            }
            setCaptureRequestDetail(cameraRequestBuilder);
            try {
                cameraCaptureSession.setRepeatingRequest(cameraRequestBuilder.build()
                                    /*Build a request using the current target Surfaces and settings.*/
                        ,null,backgroundHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            MainActivity.isCameraPreviewOpened = true;
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.d(TAG,"onConfigureFailed failed");
        }
    };
    public void startBackgroundThread(){
        backgroundHandlerThread = new HandlerThread("cameraPreviewThread");
        backgroundHandlerThread.start();
        backgroundHandler = new Handler(backgroundHandlerThread.getLooper());
    }
    public void stopBackgroundThread(){
        backgroundHandlerThread.quitSafely();
        try{
            backgroundHandlerThread.join();
            backgroundHandlerThread= null;
            backgroundHandler = null;
        }catch (InterruptedException e){
        }
    }
    public void setCaptureRequestDetail(CaptureRequest.Builder builder){
        //builder.set(CaptureRequest.CONTROL_MODE,CaptureRequest.CONTROL_MODE_AUTO);
        //builder.set(CaptureRequest.BLACK_LEVEL_LOCK,true);
        /*builder.set(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE,
                CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE_HIGH_QUALITY);*/
        Log.d(TAG,"setCaptureRequestDetail phase");
        builder.set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE,
                CaptureRequest.CONTROL_AE_ANTIBANDING_MODE_AUTO);
        builder.set(CaptureRequest.CONTROL_AE_LOCK,
                false);
        builder.set(CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_ON);
        builder.set(CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_AUTO);
        //builder.set(CaptureRequest.CONTROL_AWB_LOCK, true);
        builder.set(CaptureRequest.CONTROL_AWB_MODE,
                CaptureRequest.CONTROL_AWB_MODE_AUTO);
        /*builder.set(CaptureRequest.CONTROL_EFFECT_MODE,
                CaptureRequest.CONTROL_EFFECT_MODE_SOLARIZE);*/
        builder.set(CaptureRequest.CONTROL_MODE,
                CaptureRequest.CONTROL_MODE_AUTO);
        builder.set(CaptureRequest.CONTROL_SCENE_MODE,
                CaptureRequest.CONTROL_SCENE_MODE_DISABLED);
        /*builder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,
                CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON);*/
        builder.set(CaptureRequest.NOISE_REDUCTION_MODE,
                CaptureRequest.NOISE_REDUCTION_MODE_HIGH_QUALITY);
        builder.set(CaptureRequest.JPEG_ORIENTATION,90);
        if(rotation==Surface.ROTATION_0){
            //builder.set(CaptureRequest.JPEG_ORIENTATION,90);
        }else if(rotation==Surface.ROTATION_90){
            //builder.set(CaptureRequest.JPEG_ORIENTATION,0);
        }

        /*builder.set(CaptureRequest.SENSOR_TEST_PATTERN_MODE,
                CaptureRequest.SENSOR_TEST_PATTERN_MODE_OFF);*/
    }
    public void cameraClose(){
        Log.d(TAG,"cameraClose phase");
        if(cameraCaptureSession!=null){
            cameraCaptureSession.close();
            cameraCaptureSession = null;
        }

        if(cameraDevice!=null){
            cameraDevice.close();
            cameraDevice=null;
        }
        if(cameraRequestBuilder!=null){
            cameraRequestBuilder=null;
        }

    }
    public void resetCaptureRequestDetail(CaptureRequest.Builder builder){
        Log.d(TAG,"resetCaptureRequestDetail phase");
        if(cameraCaptureSession!=null){
            try {
                cameraCaptureSession.setRepeatingRequest(builder.build()
                                    /*Build a request using the current target Surfaces and settings.*/
                        ,null,backgroundHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

    }
}

