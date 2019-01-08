package loucms.smartparkinglot;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by loucms on 11/29/17.
 */

public class screenProjector {
    private final static String TAG = "screenProjector";
    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;
    private Context mContext;
    private Intent mIntentForMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private Handler mProjectionHandler;
    private DisplayMetrics metrics;
    private MediaRecorder mMediaRecorder;
    private Display mDisplay;
    private Surface mSurface;


    public screenProjector(Context context,Surface surface){
        mContext = context;
        mProjectionHandler = new Handler(mContext.getMainLooper());
        mMediaProjectionManager = (MediaProjectionManager)mContext.
                getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if(mMediaProjectionManager!=null){
            mIntentForMediaProjection = mMediaProjectionManager.createScreenCaptureIntent();
        }else{
            Toast.makeText(mContext,"MediaProjectionManager create fail",Toast.LENGTH_SHORT).show();
        }
        metrics = new DisplayMetrics();
        mDisplay = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        mDisplay.getMetrics(metrics);
        mMediaRecorder = new MediaRecorder();
        mSurface = surface;


    }
    public MediaProjectionManager getMediaProjectionManager(){
        if(mMediaProjectionManager!=null){
            return mMediaProjectionManager;
        }
        else return null;
    }
    public Intent getIntent_Projection(){
        if(mIntentForMediaProjection!=null){
            return mIntentForMediaProjection;
        }else{
            return null;
        }

    }
    public void setMediaProjection(MediaProjection mediaProjection){
        mMediaProjection = mediaProjection;
        if(mMediaProjection!=null){
            mMediaProjection.registerCallback(projectionCallback,mProjectionHandler);

        }

    }
    public void setupRecordDetail(){
        if(mMediaRecorder!=null) {
            Log.d(TAG,"setupRecordDetail phase");
            mMediaRecorder.setCaptureRate(25);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setOutputFile(MainActivity.applicationFolder.toString()+"/"+new SimpleDateFormat("MM.dd HHmmss").format(new Date())+".mp4");
            mMediaRecorder.setVideoEncodingBitRate(/*320*180*2*8*30*/10000000);
            mMediaRecorder.setVideoFrameRate(25);
            //mMediaRecorder.setVideoSize(1280,720);
            //mMediaRecorder.setOrientationHint(360);
            if(mDisplay.getRotation()== Surface.ROTATION_90||mDisplay.getRotation()== Surface.ROTATION_270){
                mMediaRecorder.setVideoSize(1280,720);

            }else{
                mMediaRecorder.setVideoSize(1280,720);//width and height must
                //compatible with virtualdisplay, set like width = 720 and height = 1280 just make
                //the video play on mobile  is portrait
                //you can set as width = 1280 and height = 720, then video play on mobile is landscape
                //both case on pc are the same
                //but if you want to see the landscape on pc look better(fill all the pc screen)
                //you should set width = 1280 and height = 720
            }

            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            try {
                mMediaRecorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(mDisplay.getRotation()== Surface.ROTATION_90||mDisplay.getRotation()== Surface.ROTATION_270){
                mVirtualDisplay = mMediaProjection.createVirtualDisplay("virtualDisplay",1280,720,
                        metrics.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION,
                        mMediaRecorder.getSurface(),mVirtualDisplayCallback,mProjectionHandler);
            }else{
                mVirtualDisplay = mMediaProjection.createVirtualDisplay("virtualDisplay",1280,720,
                        metrics.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION,
                        mMediaRecorder.getSurface(),mVirtualDisplayCallback,mProjectionHandler);
            }

            //must be called after prepare
        }
    }
    public void startRecord(){
            mMediaRecorder.start();
    }
    public void stopRecord(){
        if(mMediaRecorder!=null){
            mMediaRecorder.stop();
        }
        if(mMediaProjection!=null){
            mMediaProjection.stop();
            mMediaProjection.unregisterCallback(projectionCallback);
            mMediaProjection=null;
        }
        if(mVirtualDisplay!=null){
            mVirtualDisplay.release();
        }

    }
    public MediaProjection.Callback projectionCallback = new MediaProjection.Callback() {
        @Override
        public void onStop() {
            if(mMediaProjection!=null){
                mMediaProjection.stop();
                mMediaProjection.unregisterCallback(projectionCallback);
                mMediaProjection=null;
            }
            if(mVirtualDisplay!=null){
                mVirtualDisplay.release();
            }
            super.onStop();

        }
    };
    public VirtualDisplay.Callback mVirtualDisplayCallback = new VirtualDisplay.Callback() {
        @Override
        public void onPaused() {
            super.onPaused();
        }

        @Override
        public void onResumed() {
            super.onResumed();
        }

        @Override
        public void onStopped() {
            super.onStopped();
            if(mMediaProjection!=null){
                mMediaProjection.stop();
                mMediaProjection.unregisterCallback(projectionCallback);
                mMediaProjection=null;
            }
            if(mVirtualDisplay!=null){
                mVirtualDisplay.release();
            }
        }
    };


}
