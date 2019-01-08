package loucms.smartparkinglot;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by loucms on 11/11/17.
 */

public class TheChartGLRenderer implements GLSurfaceView.Renderer{
    private static final String TAG = "TheChartGLRenderer";
    public MegnetoChart megnetoChart;
    private int width;
    private int height;
    private float mainlineColor[];
    /*public TheChartGLRenderer(MegnetoChart drawingLine){
        megnetoChart = drawingLine;
    }*/

    public TheChartGLRenderer(int w,int h,float color[]){
        width = w;
        height =h;
        mainlineColor = color;
    }
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f,0.0f,0.0f,0.0f);
        Log.d(TAG,"onSurfaceCreated phase");
        megnetoChart = new MegnetoChart(width,height);
        megnetoChart.setMainLineColor(mainlineColor);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        GLES20.glViewport(0,0,width,height);
        Log.d(TAG,"onSurfaceChanged phase");

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        drawDetail();
        Log.d(TAG,"onDrawFrame phase");
    }
    public void drawDetail(){
        if(MainActivity.drawGridLine){
            //megnetoChart.drawLongitude_major(60,0f,0f);
            //megnetoChart.drawXLongitude_minor(60,0,0);
            //megnetoChart.drawYLongitude_minor(60,0,0);
            //megnetoChart.drawZLongitude_minor(60,0,0);
            //megnetoChart.drawLongitude_minor(60,0f,0f);
            //megnetoChart.drawLatitude_major(60,0f,0f);
            megnetoChart.drawXLongitude_major(60,0,0);
            megnetoChart.drawYLongitude_major(60,0,0);
            megnetoChart.drawZLongitude_major(60,0,0);
            megnetoChart.drawXLatitude_major(200,0f,0f);
            megnetoChart.drawYLatitude_major(200,0f,0f);
            megnetoChart.drawZLatitude_major(200,0f,0f);
        }
        if(megnetoChart.Xdata_From_megneto!=null){
            megnetoChart.drawXline(60,200,0,0);
        }
        if(megnetoChart.Ydata_From_megneto!=null){
            megnetoChart.drawYline(60,200,0,0);
        }
        if(megnetoChart.Zdata_From_megneto!=null){
            megnetoChart.drawZline(60,200,0,0);
        }
    }

    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
}
