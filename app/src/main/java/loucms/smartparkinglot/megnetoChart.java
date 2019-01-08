package loucms.smartparkinglot;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Created by loucms on 11/26/17.
 */

public class megnetoChart {
    private static final String TAG = "megnetoChart";
    private Context mContext;
    private LayoutInflater inflater;
    private int width=650;
    private int height=300;
    private LinearLayout.LayoutParams lp;
    private TheChartGLRenderer mtheChartGLRenderer;
    float mainLine_color[] = { 1.0f, 0.0f, 0.0f, 1.0f };
    public GLSurfaceView myGLSurfaceView;

    public megnetoChart(Context context, ViewGroup viewGroup){
        mContext = context;
        inflater = LayoutInflater.from(mContext);
        lp = new LinearLayout.LayoutParams(width,height);
        //lp.topMargin = 1500;
        //lp.setMarginStart(200);
        mtheChartGLRenderer = new TheChartGLRenderer(width,height,mainLine_color);
        myGLSurfaceView =(GLSurfaceView)inflater.inflate(R.layout.magnetochart,viewGroup,false);
        if(myGLSurfaceView==null){
            Log.d(TAG,"myGLSurfaceView is null");
        }else{
            Log.d(TAG,"myGLSurfaceView isn't null");
        }
        //myGLSurfaceView.setLayoutParams(lp);
        myGLSurfaceView.setEGLContextClientVersion(2);
        myGLSurfaceView.setZOrderOnTop(true);
        myGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        myGLSurfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);
        myGLSurfaceView.setRenderer(mtheChartGLRenderer);
        myGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        myGLSurfaceView.setVisibility(View.INVISIBLE);
        myGLSurfaceView.setX(-2000);
        viewGroup.addView(myGLSurfaceView);
    }
    public void updateFragment(float buf[][],int vertexCounter){
        Log.d(TAG,"updateFragment phase");
        //Log.d(TAG,"x = "+buf[0]+" : y = "+buf[1]+" : z  = "+buf[2]);
        Log.d(TAG,"myGLSurfaceView = "+myGLSurfaceView);
        Log.d(TAG,"total node is = "+vertexCounter);
        mtheChartGLRenderer.megnetoChart.upData(buf,vertexCounter);
        if(buf!=null)
            myGLSurfaceView.requestRender();
    }
    public void setVisibility(int visibility){
        myGLSurfaceView.setVisibility(visibility);
    }
    public int getVisibility(){
        return myGLSurfaceView.getVisibility();
    }
}
