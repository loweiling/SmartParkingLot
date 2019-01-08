package loucms.smartparkinglot;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by loucms on 11/11/17.
 */

public class MegnetoChart {
    private static final String TAG="MegnetoChart";
    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;
    private final int mProgram;
    //private final int mProgram_FOR_PLAIN;
    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = vPosition;" +
                    "}";
    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";
    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 2;
    float color_major[] = { 0.4f, 0.4f, 0.4f, 1.0f };
    float colorX_major[] = {  0.4f, 0.4f, 0.4f, 1.0f };
    float colorY_major[] = {  0.4f, 0.4f, 0.4f, 1.0f };
    float colorZ_major[] = { 0.4f, 0.4f, 0.4f, 1.0f };
    float color_minor[] = { 0.2f, 0.2f, 0.2f, 1.0f };
    float color_mainline[];
    float color_Xline[] = { 1.0f, 0f, 0f, 1.0f };
    float color_Yline[] = { 0.0f, 1.0f, 0f, 1.0f };
    float color_Zline[] = { 0.0f, 0f, 1.0f, 1.0f };
    private int mPositionHandle;
    private int mColorHandle;
    private int vertexCount;
    private final int vertexStride = COORDS_PER_VERTEX * 4;
    private int width;
    private int height;
    public float data_From_megneto[];
    public float Xdata_From_megneto[];
    public float Ydata_From_megneto[];
    public float Zdata_From_megneto[];
    private short lineDrawOrder[];
    private int vertexCount_line;
    private float[] longitudeMajor = new float[4];
    private float[] LatitudeMajor = new float[4];
    public MegnetoChart(int w,int h) {
        width = w;
        height = h;
        int vertexShader = TheChartGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,vertexShaderCode);
        int fragmentShader = TheChartGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,fragmentShaderCode);
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram,vertexShader);
        GLES20.glAttachShader(mProgram,fragmentShader);
        GLES20.glLinkProgram(mProgram);
        // initialize vertex byte buffer for shape coordinates
    }
    public void upData(float buf[][],int nodNumber){
        vertexCount_line = nodNumber;
        Xdata_From_megneto= new float[nodNumber*2];
        Ydata_From_megneto= new float[nodNumber*2];
        Zdata_From_megneto= new float[nodNumber*2];
        for(int i = 0;i<nodNumber;i++){
            Xdata_From_megneto[i*2] = buf[0][i*2];
            Xdata_From_megneto[i*2+1] = buf[0][i*2+1];
            Ydata_From_megneto[i*2] = buf[1][i*2];
            Ydata_From_megneto[i*2+1] = buf[1][i*2+1];
            Zdata_From_megneto[i*2] = buf[2][i*2];
            Zdata_From_megneto[i*2+1] = buf[2][i*2+1];
        }
        Log.d(TAG,"upData phase");
        Log.d(TAG,""+nodNumber);
    }
    public void drawline(float interval,float Xoffset,float Yoffset){
        float columnWidth = (2-2*Xoffset)/interval;
        float rowWidth = (1-2*Yoffset)/interval;
        lineDrawOrder = new short[vertexCount_line*2];
        for(int i = 0;i<vertexCount_line;i++){
            lineDrawOrder[i*2] = (short)i;
            lineDrawOrder[i*2+1] = (short)i;
            data_From_megneto[i*2] = data_From_megneto[i*2]*columnWidth-(1-Xoffset);
            data_From_megneto[i*2+1] = data_From_megneto[i*2+1]*rowWidth;
        }
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                vertexCount_line *2 * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(data_From_megneto);
        vertexBuffer.position(0);

        ByteBuffer dlb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 2 bytes per short)
                lineDrawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(lineDrawOrder);
        drawListBuffer.position(0);
        GLES20.glUseProgram(mProgram);
        mPositionHandle = GLES20.glGetAttribLocation(mProgram,"vPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle,COORDS_PER_VERTEX,GLES20.GL_FLOAT
                    ,false,vertexStride,vertexBuffer);
        mColorHandle = GLES20.glGetUniformLocation(mProgram,"vColor");
        GLES20.glUniform4fv(mColorHandle,1,color_mainline,0);
            //GLES20.glDrawElements(GLES20.GL_LINES,drawOrder.length,GLES20.GL_UNSIGNED_SHORT,drawListBuffer);
        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP,0,vertexCount_line);
        GLES20.glDisableVertexAttribArray(mPositionHandle);


    }
    public void drawXline(float xinterval,float yinterval,float Xoffset,float Yoffset){
        float columnWidth = (2-2*Xoffset)/xinterval;
        float rowWidth = (2f/3f-0.05f)/yinterval;
        lineDrawOrder = new short[vertexCount_line*2];
        for(int i = 0;i<vertexCount_line;i++){
            lineDrawOrder[i*2] = (short)i;
            lineDrawOrder[i*2+1] = (short)i;
            Xdata_From_megneto[i*2] = Xdata_From_megneto[i*2]*columnWidth-(1-Xoffset);
            Xdata_From_megneto[i*2+1] = Xdata_From_megneto[i*2+1]*rowWidth+(-0.975f+5f/3f);
        }
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                vertexCount_line *2 * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(Xdata_From_megneto);
        vertexBuffer.position(0);
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                lineDrawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(lineDrawOrder);
        drawListBuffer.position(0);
        GLES20.glUseProgram(mProgram);
        mPositionHandle = GLES20.glGetAttribLocation(mProgram,"vPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle,COORDS_PER_VERTEX,GLES20.GL_FLOAT
                ,false,vertexStride,vertexBuffer);
        mColorHandle = GLES20.glGetUniformLocation(mProgram,"vColor");
        GLES20.glUniform4fv(mColorHandle,1,color_Xline,0);
        //GLES20.glDrawElements(GLES20.GL_LINES,drawOrder.length,GLES20.GL_UNSIGNED_SHORT,drawListBuffer);
        GLES20.glLineWidth(3.0f);
        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP,0,vertexCount_line);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
    public void drawYline(float xinterval,float yinterval,float Xoffset,float Yoffset){
        float columnWidth = (2-2*Xoffset)/xinterval;
        float rowWidth = (2f/3f-0.05f)/yinterval;
        lineDrawOrder = new short[vertexCount_line*2];
        for(int i = 0;i<vertexCount_line;i++){
            lineDrawOrder[i*2] = (short)i;
            lineDrawOrder[i*2+1] = (short)i;
            Ydata_From_megneto[i*2] = Ydata_From_megneto[i*2]*columnWidth-(1-Xoffset);
            Ydata_From_megneto[i*2+1] = Ydata_From_megneto[i*2+1]*rowWidth+(-1f+2f/3f+0.05f+1f/3f-0.05f/2f);
        }
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                vertexCount_line *2 * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(Ydata_From_megneto);
        vertexBuffer.position(0);
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                lineDrawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(lineDrawOrder);
        drawListBuffer.position(0);
        GLES20.glUseProgram(mProgram);
        mPositionHandle = GLES20.glGetAttribLocation(mProgram,"vPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle,COORDS_PER_VERTEX,GLES20.GL_FLOAT
                ,false,vertexStride,vertexBuffer);
        mColorHandle = GLES20.glGetUniformLocation(mProgram,"vColor");
        GLES20.glUniform4fv(mColorHandle,1,color_Yline,0);
        //GLES20.glDrawElements(GLES20.GL_LINES,drawOrder.length,GLES20.GL_UNSIGNED_SHORT,drawListBuffer);
        GLES20.glLineWidth(3.0f);
        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP,0,vertexCount_line);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
    public void drawZline(float xinterval,float yinterval,float Xoffset,float Yoffset){
        float columnWidth = (2-2*Xoffset)/xinterval;
        float rowWidth = (2f/3f-0.05f)/yinterval;
        lineDrawOrder = new short[vertexCount_line*2];
        for(int i = 0;i<vertexCount_line;i++){
            lineDrawOrder[i*2] = (short)i;
            lineDrawOrder[i*2+1] = (short)i;
            Zdata_From_megneto[i*2] = Zdata_From_megneto[i*2]*columnWidth-(1-Xoffset);
            Zdata_From_megneto[i*2+1] = Zdata_From_megneto[i*2+1]*rowWidth+(-1f+0.05f+1f/3f-0.05f/2f);
        }
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                vertexCount_line *2 * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(Zdata_From_megneto);
        vertexBuffer.position(0);
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                lineDrawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(lineDrawOrder);
        drawListBuffer.position(0);
        GLES20.glUseProgram(mProgram);
        mPositionHandle = GLES20.glGetAttribLocation(mProgram,"vPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle,COORDS_PER_VERTEX,GLES20.GL_FLOAT
                ,false,vertexStride,vertexBuffer);
        mColorHandle = GLES20.glGetUniformLocation(mProgram,"vColor");
        GLES20.glUniform4fv(mColorHandle,1,color_Zline,0);
        //GLES20.glDrawElements(GLES20.GL_LINES,drawOrder.length,GLES20.GL_UNSIGNED_SHORT,drawListBuffer);
        GLES20.glLineWidth(3.0f);
        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP,0,vertexCount_line);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
    public void drawXLongitude_major(float interval,float Xoffset,float Yoffset){

        float columnWidth = (2-2*Xoffset)/interval;
        short Longitude_main_drawOrder[] = {0,1};

        for(float i = 0;i<=60;i+=10){
            if(i==0){
                longitudeMajor[0] = -1.0f;
                longitudeMajor[1] = -0.95f+4.0f/3.0f;
                longitudeMajor[2] = -1.0f;
                longitudeMajor[3] = 1.0f;
            }else if(i == 60){
                longitudeMajor[0] = 1.0f;
                longitudeMajor[1] = -0.95f+4.0f/3.0f;
                longitudeMajor[2] = 1.0f;
                longitudeMajor[3] = 1.0f;
            }else{
                longitudeMajor[0] = -1.0f+i*columnWidth+Xoffset;
                longitudeMajor[1] = -0.95f+4.0f/3.0f;
                longitudeMajor[2] = -1.0f +i*columnWidth+Xoffset;
                longitudeMajor[3] = 1.0f;
            }
            ByteBuffer bb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 4 bytes per float)
                    longitudeMajor.length * 4);
            bb.order(ByteOrder.nativeOrder());
            vertexBuffer = bb.asFloatBuffer();
            vertexBuffer.put(longitudeMajor);
            vertexBuffer.position(0);
            vertexCount = longitudeMajor.length/COORDS_PER_VERTEX;

            ByteBuffer dlb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 2 bytes per short)
                    Longitude_main_drawOrder.length * 2);
            dlb.order(ByteOrder.nativeOrder());
            drawListBuffer = dlb.asShortBuffer();
            drawListBuffer.put(Longitude_main_drawOrder);
            drawListBuffer.position(0);

            GLES20.glUseProgram(mProgram);
            mPositionHandle = GLES20.glGetAttribLocation(mProgram,"vPosition");
            GLES20.glEnableVertexAttribArray(mPositionHandle);
            GLES20.glVertexAttribPointer(mPositionHandle,COORDS_PER_VERTEX,GLES20.GL_FLOAT
                    ,false,vertexStride,vertexBuffer);
            mColorHandle = GLES20.glGetUniformLocation(mProgram,"vColor");
            GLES20.glUniform4fv(mColorHandle,1,colorX_major,0);
            //GLES20.glDrawElements(GLES20.GL_LINES,drawOrder.length,GLES20.GL_UNSIGNED_SHORT,drawListBuffer);
            GLES20.glLineWidth(1.0f);
            GLES20.glDrawArrays(GLES20.GL_LINES,0,vertexCount);
            GLES20.glDisableVertexAttribArray(mPositionHandle);
        }

    }
    public void drawYLongitude_major(float interval,float Xoffset,float Yoffset){

        float columnWidth = (2-2*Xoffset)/interval;

        short Longitude_main_drawOrder[] = {0,1};

        for(float i = 0;i<=60;i+=10){
            if(i==0){
                longitudeMajor[0] = -1.0f;
                longitudeMajor[1] = -0.95f+2.0f/3.0f;
                longitudeMajor[2] = -1.0f;
                longitudeMajor[3] = -1.0f+4.0f/3.0f;
            }else if(i == 60){
                longitudeMajor[0] = 1.0f;
                longitudeMajor[1] = -0.95f+2.0f/3.0f;
                longitudeMajor[2] = 1.0f;
                longitudeMajor[3] = -1.0f+4.0f/3.0f;
            }else{
                longitudeMajor[0] = -1.0f+i*columnWidth+Xoffset;
                longitudeMajor[1] = -0.95f+2.0f/3.0f;
                longitudeMajor[2] = -1.0f +i*columnWidth+Xoffset;
                longitudeMajor[3] = -1.0f+4.0f/3.0f;
            }
            //float longitudeMajor[]={-1.0f+i*columnWidth+Xoffset,/*-1+1/3+0.05f*/-0.95f+2.0f/3.0f,-1.0f
                    //+i*columnWidth+Xoffset,-1+4.0f/3.0f};
            ByteBuffer bb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 4 bytes per float)
                    longitudeMajor.length * 4);
            bb.order(ByteOrder.nativeOrder());
            vertexBuffer = bb.asFloatBuffer();
            vertexBuffer.put(longitudeMajor);
            vertexBuffer.position(0);
            vertexCount = longitudeMajor.length/COORDS_PER_VERTEX;
            ByteBuffer dlb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 2 bytes per short)
                    Longitude_main_drawOrder.length * 2);
            dlb.order(ByteOrder.nativeOrder());
            drawListBuffer = dlb.asShortBuffer();
            drawListBuffer.put(Longitude_main_drawOrder);
            drawListBuffer.position(0);

            GLES20.glUseProgram(mProgram);
            mPositionHandle = GLES20.glGetAttribLocation(mProgram,"vPosition");
            GLES20.glEnableVertexAttribArray(mPositionHandle);
            GLES20.glVertexAttribPointer(mPositionHandle,COORDS_PER_VERTEX,GLES20.GL_FLOAT
                    ,false,vertexStride,vertexBuffer);
            mColorHandle = GLES20.glGetUniformLocation(mProgram,"vColor");
            GLES20.glUniform4fv(mColorHandle,1,colorY_major,0);
            //GLES20.glDrawElements(GLES20.GL_LINES,drawOrder.length,GLES20.GL_UNSIGNED_SHORT,drawListBuffer);
            GLES20.glLineWidth(1.0f);
            GLES20.glDrawArrays(GLES20.GL_LINES,0,vertexCount);
            GLES20.glDisableVertexAttribArray(mPositionHandle);
        }
    }
    public void drawZLongitude_major(float interval,float Xoffset,float Yoffset){

        float columnWidth = (2-2*Xoffset)/interval;

        short Longitude_main_drawOrder[] = {0,1};

        for(float i = 0;i<=60;i+=10){
            if(i==0){
                longitudeMajor[0] = -1.0f;
                longitudeMajor[1] = -0.95f;
                longitudeMajor[2] = -1.0f;
                longitudeMajor[3] = -1.0f+2.0f/3.0f;
            }else if(i == 60){
                longitudeMajor[0] = 1.0f;
                longitudeMajor[1] = -0.95f;
                longitudeMajor[2] = 1.0f;
                longitudeMajor[3] = -1.0f+2.0f/3.0f;
            }else{
                longitudeMajor[0] = -1.0f+i*columnWidth+Xoffset;
                longitudeMajor[1] = -0.95f;
                longitudeMajor[2] = -1.0f+i*columnWidth+Xoffset;
                longitudeMajor[3] = -1.0f+2.0f/3.0f;
            }

            float longitudeMajor[]={-1.0f+i*columnWidth+Xoffset,-0.95f,-1.0f
                    +i*columnWidth+Xoffset,-1+2.0f/3.0f};
            ByteBuffer bb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 4 bytes per float)
                    longitudeMajor.length * 4);
            bb.order(ByteOrder.nativeOrder());
            vertexBuffer = bb.asFloatBuffer();
            vertexBuffer.put(longitudeMajor);
            vertexBuffer.position(0);
            vertexCount = longitudeMajor.length/COORDS_PER_VERTEX;

            ByteBuffer dlb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 2 bytes per short)
                    Longitude_main_drawOrder.length * 2);
            dlb.order(ByteOrder.nativeOrder());
            drawListBuffer = dlb.asShortBuffer();
            drawListBuffer.put(Longitude_main_drawOrder);
            drawListBuffer.position(0);

            GLES20.glUseProgram(mProgram);
            mPositionHandle = GLES20.glGetAttribLocation(mProgram,"vPosition");
            GLES20.glEnableVertexAttribArray(mPositionHandle);
            GLES20.glVertexAttribPointer(mPositionHandle,COORDS_PER_VERTEX,GLES20.GL_FLOAT
                    ,false,vertexStride,vertexBuffer);
            mColorHandle = GLES20.glGetUniformLocation(mProgram,"vColor");
            GLES20.glUniform4fv(mColorHandle,1,colorZ_major,0);
            //GLES20.glDrawElements(GLES20.GL_LINES,drawOrder.length,GLES20.GL_UNSIGNED_SHORT,drawListBuffer);
            GLES20.glLineWidth(1.0f);
            GLES20.glDrawArrays(GLES20.GL_LINES,0,vertexCount);
            GLES20.glDisableVertexAttribArray(mPositionHandle);
        }

    }
    public void drawLongitude_major(float interval,float Xoffset,float Yoffset){

        float columnWidth = (2-2*Xoffset)/interval;

        short Longitude_main_drawOrder[] = {0,1};

        for(float i = 0;i<=60;i+=10){
            float longitudeMajor[]={-1.0f+i*columnWidth+Xoffset,-0.5f+Yoffset,-1.0f
                    +i*columnWidth+Xoffset,0.5f-Yoffset};
            ByteBuffer bb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 4 bytes per float)
                    longitudeMajor.length * 4);
            bb.order(ByteOrder.nativeOrder());
            vertexBuffer = bb.asFloatBuffer();
            vertexBuffer.put(longitudeMajor);
            vertexBuffer.position(0);
            vertexCount = longitudeMajor.length/COORDS_PER_VERTEX;

            ByteBuffer dlb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 2 bytes per short)
                    Longitude_main_drawOrder.length * 2);
            dlb.order(ByteOrder.nativeOrder());
            drawListBuffer = dlb.asShortBuffer();
            drawListBuffer.put(Longitude_main_drawOrder);
            drawListBuffer.position(0);

            GLES20.glUseProgram(mProgram);
            mPositionHandle = GLES20.glGetAttribLocation(mProgram,"vPosition");
            GLES20.glEnableVertexAttribArray(mPositionHandle);
            GLES20.glVertexAttribPointer(mPositionHandle,COORDS_PER_VERTEX,GLES20.GL_FLOAT
                    ,false,vertexStride,vertexBuffer);
            mColorHandle = GLES20.glGetUniformLocation(mProgram,"vColor");
            GLES20.glUniform4fv(mColorHandle,1,color_major,0);
            //GLES20.glDrawElements(GLES20.GL_LINES,drawOrder.length,GLES20.GL_UNSIGNED_SHORT,drawListBuffer);
            GLES20.glDrawArrays(GLES20.GL_LINES,0,vertexCount);
            GLES20.glDisableVertexAttribArray(mPositionHandle);
        }

    }
    public void drawLongitude_minor(float interval,float Xoffset,float Yoffset){
        float columnWidth = (2-2*Xoffset)/interval;

        short Longitude_minor_drawOrder[] = {0,1};

        for(float i = 0;i<=60;i++){
            if(i%10==0)continue;
            float longitudeMajor[]={-1.0f+i*columnWidth+Xoffset,-0.5f+Yoffset,-1.0f
                    +i*columnWidth+Xoffset,0.5f-Yoffset};
            ByteBuffer bb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 4 bytes per float)
                    longitudeMajor.length * 4);
            bb.order(ByteOrder.nativeOrder());
            vertexBuffer = bb.asFloatBuffer();
            vertexBuffer.put(longitudeMajor);
            vertexBuffer.position(0);
            vertexCount = longitudeMajor.length/COORDS_PER_VERTEX;

            ByteBuffer dlb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 2 bytes per short)
                    Longitude_minor_drawOrder.length * 2);
            dlb.order(ByteOrder.nativeOrder());
            drawListBuffer = dlb.asShortBuffer();
            drawListBuffer.put(Longitude_minor_drawOrder);
            drawListBuffer.position(0);

            GLES20.glUseProgram(mProgram);
            mPositionHandle = GLES20.glGetAttribLocation(mProgram,"vPosition");
            GLES20.glEnableVertexAttribArray(mPositionHandle);
            GLES20.glVertexAttribPointer(mPositionHandle,COORDS_PER_VERTEX,GLES20.GL_FLOAT
                    ,false,vertexStride,vertexBuffer);
            mColorHandle = GLES20.glGetUniformLocation(mProgram,"vColor");
            GLES20.glUniform4fv(mColorHandle,1,color_minor,0 );
            //GLES20.glDrawElements(GLES20.GL_LINES,drawOrder.length,GLES20.GL_UNSIGNED_SHORT,drawListBuffer);
            GLES20.glDrawArrays(GLES20.GL_LINES,0,vertexCount);
            GLES20.glDisableVertexAttribArray(mPositionHandle);
        }

    }
    public void drawXLongitude_minor(float interval,float Xoffset,float Yoffset){

        float columnWidth = (2-2*Xoffset)/interval;

        short Longitude_main_drawOrder[] = {0,1};

        for(float i = 0;i<=60;i++){
            if(i%10==0)continue;
            float longitudeMajor[]={-1.0f+i*columnWidth+Xoffset,/*-1+1/3*2+0.05f*/-0.95f+4.0f/3.0f,-1.0f
                    +i*columnWidth+Xoffset,1};
            ByteBuffer bb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 4 bytes per float)
                    longitudeMajor.length * 4);
            bb.order(ByteOrder.nativeOrder());
            vertexBuffer = bb.asFloatBuffer();
            vertexBuffer.put(longitudeMajor);
            vertexBuffer.position(0);
            vertexCount = longitudeMajor.length/COORDS_PER_VERTEX;

            ByteBuffer dlb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 2 bytes per short)
                    Longitude_main_drawOrder.length * 2);
            dlb.order(ByteOrder.nativeOrder());
            drawListBuffer = dlb.asShortBuffer();
            drawListBuffer.put(Longitude_main_drawOrder);
            drawListBuffer.position(0);

            GLES20.glUseProgram(mProgram);
            mPositionHandle = GLES20.glGetAttribLocation(mProgram,"vPosition");
            GLES20.glEnableVertexAttribArray(mPositionHandle);
            GLES20.glVertexAttribPointer(mPositionHandle,COORDS_PER_VERTEX,GLES20.GL_FLOAT
                    ,false,vertexStride,vertexBuffer);
            mColorHandle = GLES20.glGetUniformLocation(mProgram,"vColor");
            GLES20.glUniform4fv(mColorHandle,1,color_major,0);
            //GLES20.glDrawElements(GLES20.GL_LINES,drawOrder.length,GLES20.GL_UNSIGNED_SHORT,drawListBuffer);
            GLES20.glLineWidth(1.0f);
            GLES20.glDrawArrays(GLES20.GL_LINES,0,vertexCount);
            GLES20.glDisableVertexAttribArray(mPositionHandle);
        }

    }
    public void drawYLongitude_minor(float interval,float Xoffset,float Yoffset){

        float columnWidth = (2-2*Xoffset)/interval;

        short Longitude_main_drawOrder[] = {0,1};

        for(float i = 0;i<=60;i++){
            if(i%10==0)continue;
            float longitudeMajor[]={-1.0f+i*columnWidth+Xoffset,/*-1+1/3+0.05f*/-0.95f+2.0f/3.0f,-1.0f
                    +i*columnWidth+Xoffset,-1+4.0f/3.0f};
            ByteBuffer bb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 4 bytes per float)
                    longitudeMajor.length * 4);
            bb.order(ByteOrder.nativeOrder());
            vertexBuffer = bb.asFloatBuffer();
            vertexBuffer.put(longitudeMajor);
            vertexBuffer.position(0);
            vertexCount = longitudeMajor.length/COORDS_PER_VERTEX;

            ByteBuffer dlb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 2 bytes per short)
                    Longitude_main_drawOrder.length * 2);
            dlb.order(ByteOrder.nativeOrder());
            drawListBuffer = dlb.asShortBuffer();
            drawListBuffer.put(Longitude_main_drawOrder);
            drawListBuffer.position(0);

            GLES20.glUseProgram(mProgram);
            mPositionHandle = GLES20.glGetAttribLocation(mProgram,"vPosition");
            GLES20.glEnableVertexAttribArray(mPositionHandle);
            GLES20.glVertexAttribPointer(mPositionHandle,COORDS_PER_VERTEX,GLES20.GL_FLOAT
                    ,false,vertexStride,vertexBuffer);
            mColorHandle = GLES20.glGetUniformLocation(mProgram,"vColor");
            GLES20.glUniform4fv(mColorHandle,1,color_major,0);
            //GLES20.glDrawElements(GLES20.GL_LINES,drawOrder.length,GLES20.GL_UNSIGNED_SHORT,drawListBuffer);
            GLES20.glLineWidth(1.0f);
            GLES20.glDrawArrays(GLES20.GL_LINES,0,vertexCount);
            GLES20.glDisableVertexAttribArray(mPositionHandle);
        }

    }
    public void drawZLongitude_minor(float interval,float Xoffset,float Yoffset){

        float columnWidth = (2-2*Xoffset)/interval;

        short Longitude_main_drawOrder[] = {0,1};

        for(float i = 0;i<=60;i++){
            if(i%10==0)continue;
            float longitudeMajor[]={-1.0f+i*columnWidth+Xoffset,-0.95f,-1.0f
                    +i*columnWidth+Xoffset,-1+2.0f/3.0f};
            ByteBuffer bb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 4 bytes per float)
                    longitudeMajor.length * 4);
            bb.order(ByteOrder.nativeOrder());
            vertexBuffer = bb.asFloatBuffer();
            vertexBuffer.put(longitudeMajor);
            vertexBuffer.position(0);
            vertexCount = longitudeMajor.length/COORDS_PER_VERTEX;

            ByteBuffer dlb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 2 bytes per short)
                    Longitude_main_drawOrder.length * 2);
            dlb.order(ByteOrder.nativeOrder());
            drawListBuffer = dlb.asShortBuffer();
            drawListBuffer.put(Longitude_main_drawOrder);
            drawListBuffer.position(0);

            GLES20.glUseProgram(mProgram);
            mPositionHandle = GLES20.glGetAttribLocation(mProgram,"vPosition");
            GLES20.glEnableVertexAttribArray(mPositionHandle);
            GLES20.glVertexAttribPointer(mPositionHandle,COORDS_PER_VERTEX,GLES20.GL_FLOAT
                    ,false,vertexStride,vertexBuffer);
            mColorHandle = GLES20.glGetUniformLocation(mProgram,"vColor");
            GLES20.glUniform4fv(mColorHandle,1,color_major,0);
            //GLES20.glDrawElements(GLES20.GL_LINES,drawOrder.length,GLES20.GL_UNSIGNED_SHORT,drawListBuffer);
            GLES20.glLineWidth(1.0f);
            GLES20.glDrawArrays(GLES20.GL_LINES,0,vertexCount);
            GLES20.glDisableVertexAttribArray(mPositionHandle);
        }

    }
    public void drawLatitude_major(float interval,float Xoffset,float Yoffset){
        float rowWidth = (1-2*Yoffset)/interval;

        short Latitude_major_drawOrder[] = {0,1};

        for(float i = 0;i<=60;i+=10){
            float LatitudeMajor[]={-1.0f+Xoffset,-0.5f+Yoffset+i*rowWidth,1.0f
                    -Xoffset,-0.5f+Yoffset+i*rowWidth};
            ByteBuffer bb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 4 bytes per float)
                    LatitudeMajor.length * 4);
            bb.order(ByteOrder.nativeOrder());
            vertexBuffer = bb.asFloatBuffer();
            vertexBuffer.put(LatitudeMajor);
            vertexBuffer.position(0);
            vertexCount = LatitudeMajor.length/COORDS_PER_VERTEX;

            ByteBuffer dlb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 2 bytes per short)
                    Latitude_major_drawOrder.length * 2);
            dlb.order(ByteOrder.nativeOrder());
            drawListBuffer = dlb.asShortBuffer();
            drawListBuffer.put(Latitude_major_drawOrder);
            drawListBuffer.position(0);

            GLES20.glUseProgram(mProgram);
            mPositionHandle = GLES20.glGetAttribLocation(mProgram,"vPosition");
            GLES20.glEnableVertexAttribArray(mPositionHandle);
            GLES20.glVertexAttribPointer(mPositionHandle,COORDS_PER_VERTEX,GLES20.GL_FLOAT
                    ,false,vertexStride,vertexBuffer);
            mColorHandle = GLES20.glGetUniformLocation(mProgram,"vColor");
            GLES20.glUniform4fv(mColorHandle,1,color_major,0);
            //GLES20.glDrawElements(GLES20.GL_LINES,drawOrder.length,GLES20.GL_UNSIGNED_SHORT,drawListBuffer);
            GLES20.glDrawArrays(GLES20.GL_LINES,0,vertexCount);
            GLES20.glDisableVertexAttribArray(mPositionHandle);
        }

    }
    public void drawXLatitude_major(float interval,float Xoffset,float Yoffset){
        float rowWidth = (2.0f/3.0f-0.05f)/interval;
        short Latitude_major_drawOrder[] = {0,1};
        for(float i = 0;i<=200;i+=100){
            if(i==0){
                LatitudeMajor[0] = -1.0f;
                LatitudeMajor[1] = -0.95f+4.0f/3.0f;
                LatitudeMajor[2] = 1.0f;
                LatitudeMajor[3] = -0.95f+4.0f/3.0f;
            }else if(i == 200){
                LatitudeMajor[0] = -1.0f;
                LatitudeMajor[1] = 1.0f;
                LatitudeMajor[2] = 1.0f;
                LatitudeMajor[3] = 1.0f;
            }else{
                LatitudeMajor[0] = -1.0f+Xoffset;
                LatitudeMajor[1] = 1.0f-i*rowWidth;
                LatitudeMajor[2] = 1.0f-Xoffset;
                LatitudeMajor[3] = 1.0f-i*rowWidth;
            }
            //float LatitudeMajor[]={-1.0f+Xoffset,1.0f-i*rowWidth,1.0f
                    //-Xoffset,1.0f-i*rowWidth};//because "float" somehow larger than 1
            ByteBuffer bb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 4 bytes per float)
                    LatitudeMajor.length * 4);
            bb.order(ByteOrder.nativeOrder());
            vertexBuffer = bb.asFloatBuffer();
            vertexBuffer.put(LatitudeMajor);
            vertexBuffer.position(0);
            vertexCount = LatitudeMajor.length/COORDS_PER_VERTEX;

            ByteBuffer dlb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 2 bytes per short)
                    Latitude_major_drawOrder.length * 2);
            dlb.order(ByteOrder.nativeOrder());
            drawListBuffer = dlb.asShortBuffer();
            drawListBuffer.put(Latitude_major_drawOrder);
            drawListBuffer.position(0);

            GLES20.glUseProgram(mProgram);
            mPositionHandle = GLES20.glGetAttribLocation(mProgram,"vPosition");
            GLES20.glEnableVertexAttribArray(mPositionHandle);
            GLES20.glVertexAttribPointer(mPositionHandle,COORDS_PER_VERTEX,GLES20.GL_FLOAT
                    ,false,vertexStride,vertexBuffer);
            mColorHandle = GLES20.glGetUniformLocation(mProgram,"vColor");
            GLES20.glUniform4fv(mColorHandle,1,color_major,0);
            //GLES20.glDrawElements(GLES20.GL_LINES,drawOrder.length,GLES20.GL_UNSIGNED_SHORT,drawListBuffer);
            GLES20.glLineWidth(1.0f);
            GLES20.glDrawArrays(GLES20.GL_LINES,0,vertexCount);
            GLES20.glDisableVertexAttribArray(mPositionHandle);
        }
    }
    public void drawYLatitude_major(float interval,float Xoffset,float Yoffset){
        float rowWidth = (2.0f/3.0f-0.05f)/interval;

        short Latitude_major_drawOrder[] = {0,1};

        for(float i = 0;i<=200;i+=100){
            if(i==0){
                LatitudeMajor[0] = -1.0f;
                LatitudeMajor[1] = -0.95f+2.0f/3.0f;
                LatitudeMajor[2] = 1.0f;
                LatitudeMajor[3] = -0.95f+2.0f/3.0f;
            }else if(i == 200){
                LatitudeMajor[0] = -1.0f;
                LatitudeMajor[1] = -1.0f+4.0f/3.0f;
                LatitudeMajor[2] = 1.0f;
                LatitudeMajor[3] = -1.0f+4.0f/3.0f;
            }else{
                LatitudeMajor[0] = -1.0f+Xoffset;
                LatitudeMajor[1] = -0.95f+2.0f/3.0f+i*rowWidth;
                LatitudeMajor[2] = 1.0f-Xoffset;
                LatitudeMajor[3] = -0.95f+2.0f/3.0f+i*rowWidth;
            }
            //float LatitudeMajor[]={-1.0f+Xoffset,-0.95f+2.0f/3.0f+i*rowWidth,1.0f
                    //-Xoffset,-0.95f+2.0f/3.0f+i*rowWidth};
            ByteBuffer bb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 4 bytes per float)
                    LatitudeMajor.length * 4);
            bb.order(ByteOrder.nativeOrder());
            vertexBuffer = bb.asFloatBuffer();
            vertexBuffer.put(LatitudeMajor);
            vertexBuffer.position(0);
            vertexCount = LatitudeMajor.length/COORDS_PER_VERTEX;

            ByteBuffer dlb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 2 bytes per short)
                    Latitude_major_drawOrder.length * 2);
            dlb.order(ByteOrder.nativeOrder());
            drawListBuffer = dlb.asShortBuffer();
            drawListBuffer.put(Latitude_major_drawOrder);
            drawListBuffer.position(0);

            GLES20.glUseProgram(mProgram);
            mPositionHandle = GLES20.glGetAttribLocation(mProgram,"vPosition");
            GLES20.glEnableVertexAttribArray(mPositionHandle);
            GLES20.glVertexAttribPointer(mPositionHandle,COORDS_PER_VERTEX,GLES20.GL_FLOAT
                    ,false,vertexStride,vertexBuffer);
            mColorHandle = GLES20.glGetUniformLocation(mProgram,"vColor");
            GLES20.glUniform4fv(mColorHandle,1,color_major,0);
            //GLES20.glDrawElements(GLES20.GL_LINES,drawOrder.length,GLES20.GL_UNSIGNED_SHORT,drawListBuffer);
            GLES20.glLineWidth(1.0f);
            GLES20.glDrawArrays(GLES20.GL_LINES,0,vertexCount);
            GLES20.glDisableVertexAttribArray(mPositionHandle);
        }

    }
    public void drawZLatitude_major(float interval,float Xoffset,float Yoffset){
        float rowWidth = (2.0f/3.0f-0.05f)/interval;

        short Latitude_major_drawOrder[] = {0,1};

        for(float i = 0;i<=200;i+=100){
            if(i==0){
                LatitudeMajor[0] = -1.0f;
                LatitudeMajor[1] = -0.95f;
                LatitudeMajor[2] = 1.0f;
                LatitudeMajor[3] = -0.95f;
            }else if(i == 200){
                LatitudeMajor[0] = -1.0f;
                LatitudeMajor[1] = -1.0f+2.0f/3.0f;
                LatitudeMajor[2] = 1.0f;
                LatitudeMajor[3] = -1.0f+2.0f/3.0f;
            }else{
                LatitudeMajor[0] = -1.0f+Xoffset;
                LatitudeMajor[1] = -0.95f+i*rowWidth;
                LatitudeMajor[2] = 1.0f-Xoffset;
                LatitudeMajor[3] = -0.95f+i*rowWidth;
            }
            //float LatitudeMajor[]={-1.0f+Xoffset,-0.95f+i*rowWidth,1.0f
                    //-Xoffset,-0.95f+i*rowWidth};
            ByteBuffer bb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 4 bytes per float)
                    LatitudeMajor.length * 4);
            bb.order(ByteOrder.nativeOrder());
            vertexBuffer = bb.asFloatBuffer();
            vertexBuffer.put(LatitudeMajor);
            vertexBuffer.position(0);
            vertexCount = LatitudeMajor.length/COORDS_PER_VERTEX;

            ByteBuffer dlb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 2 bytes per short)
                    Latitude_major_drawOrder.length * 2);
            dlb.order(ByteOrder.nativeOrder());
            drawListBuffer = dlb.asShortBuffer();
            drawListBuffer.put(Latitude_major_drawOrder);
            drawListBuffer.position(0);

            GLES20.glUseProgram(mProgram);
            mPositionHandle = GLES20.glGetAttribLocation(mProgram,"vPosition");
            GLES20.glEnableVertexAttribArray(mPositionHandle);
            GLES20.glVertexAttribPointer(mPositionHandle,COORDS_PER_VERTEX,GLES20.GL_FLOAT
                    ,false,vertexStride,vertexBuffer);
            mColorHandle = GLES20.glGetUniformLocation(mProgram,"vColor");
            GLES20.glUniform4fv(mColorHandle,1,color_major,0);
            //GLES20.glDrawElements(GLES20.GL_LINES,drawOrder.length,GLES20.GL_UNSIGNED_SHORT,drawListBuffer);
            GLES20.glLineWidth(1.0f);
            GLES20.glDrawArrays(GLES20.GL_LINES,0,vertexCount);
            GLES20.glDisableVertexAttribArray(mPositionHandle);
        }

    }
    public void drawLatitude_minor(float interval,float Xoffset,float Yoffset){
        GLES20.glUseProgram(mProgram);
        float rowWidth = (1-2*Yoffset)/interval;

        short Latitude_minor_drawOrder[] = {0,1};

        for(float i = 0;i<=60;i++){
            if(i%10==0) continue;
            float LatitudeMajor[]={-1.0f+Xoffset,-0.5f+Yoffset+i*rowWidth,1.0f
                    -Xoffset,-0.5f+Yoffset+i*rowWidth};
            ByteBuffer bb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 4 bytes per float)
                    LatitudeMajor.length * 4);
            bb.order(ByteOrder.nativeOrder());
            vertexBuffer = bb.asFloatBuffer();
            vertexBuffer.put(LatitudeMajor);
            vertexBuffer.position(0);
            vertexCount = LatitudeMajor.length/COORDS_PER_VERTEX;

            ByteBuffer dlb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 2 bytes per short)
                    Latitude_minor_drawOrder.length * 2);
            dlb.order(ByteOrder.nativeOrder());
            drawListBuffer = dlb.asShortBuffer();
            drawListBuffer.put(Latitude_minor_drawOrder);
            drawListBuffer.position(0);

            GLES20.glUseProgram(mProgram);
            mPositionHandle = GLES20.glGetAttribLocation(mProgram,"vPosition");
            GLES20.glEnableVertexAttribArray(mPositionHandle);
            GLES20.glVertexAttribPointer(mPositionHandle,COORDS_PER_VERTEX,GLES20.GL_FLOAT
                    ,false,vertexStride,vertexBuffer);
            mColorHandle = GLES20.glGetUniformLocation(mProgram,"vColor");
            GLES20.glUniform4fv(mColorHandle,1,color_minor,0 );
            //GLES20.glDrawElements(GLES20.GL_LINES,drawOrder.length,GLES20.GL_UNSIGNED_SHORT,drawListBuffer);
            GLES20.glDrawArrays(GLES20.GL_LINES,0,vertexCount);
            GLES20.glDisableVertexAttribArray(mPositionHandle);
        }

    }
    public void drawXLatitude_minor(float interval,float Xoffset,float Yoffset){
        GLES20.glUseProgram(mProgram);
        float rowWidth = (2.0f/3.0f-0.05f)/interval;

        short Latitude_minor_drawOrder[] = {0,1};

        for(float i = 0;i<=200;i++){
            if(i%100==0) continue;
            float LatitudeMajor[]={-1.0f+Xoffset,-0.95f+4.0f/3.0f+i*rowWidth,1.0f
                    -Xoffset,-0.95f+4.0f/3.0f+i*rowWidth};
            ByteBuffer bb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 4 bytes per float)
                    LatitudeMajor.length * 4);
            bb.order(ByteOrder.nativeOrder());
            vertexBuffer = bb.asFloatBuffer();
            vertexBuffer.put(LatitudeMajor);
            vertexBuffer.position(0);
            vertexCount = LatitudeMajor.length/COORDS_PER_VERTEX;

            ByteBuffer dlb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 2 bytes per short)
                    Latitude_minor_drawOrder.length * 2);
            dlb.order(ByteOrder.nativeOrder());
            drawListBuffer = dlb.asShortBuffer();
            drawListBuffer.put(Latitude_minor_drawOrder);
            drawListBuffer.position(0);

            GLES20.glUseProgram(mProgram);
            mPositionHandle = GLES20.glGetAttribLocation(mProgram,"vPosition");
            GLES20.glEnableVertexAttribArray(mPositionHandle);
            GLES20.glVertexAttribPointer(mPositionHandle,COORDS_PER_VERTEX,GLES20.GL_FLOAT
                    ,false,vertexStride,vertexBuffer);
            mColorHandle = GLES20.glGetUniformLocation(mProgram,"vColor");
            GLES20.glUniform4fv(mColorHandle,1,color_minor,0 );
            //GLES20.glDrawElements(GLES20.GL_LINES,drawOrder.length,GLES20.GL_UNSIGNED_SHORT,drawListBuffer);
            GLES20.glLineWidth(1.0f);
            GLES20.glDrawArrays(GLES20.GL_LINES,0,vertexCount);
            GLES20.glDisableVertexAttribArray(mPositionHandle);
        }

    }
    public void drawYLatitude_minor(float interval,float Xoffset,float Yoffset){
        GLES20.glUseProgram(mProgram);
        float rowWidth = (2.0f/3.0f-0.05f)/interval;

        short Latitude_minor_drawOrder[] = {0,1};

        for(float i = 0;i<=200;i++){
            if(i%100==0) continue;
            float LatitudeMajor[]={-1.0f+Xoffset,-0.95f+2.0f/3.0f+i*rowWidth,1.0f
                    -Xoffset,-0.95f+2.0f/3.0f+i*rowWidth};
            ByteBuffer bb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 4 bytes per float)
                    LatitudeMajor.length * 4);
            bb.order(ByteOrder.nativeOrder());
            vertexBuffer = bb.asFloatBuffer();
            vertexBuffer.put(LatitudeMajor);
            vertexBuffer.position(0);
            vertexCount = LatitudeMajor.length/COORDS_PER_VERTEX;

            ByteBuffer dlb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 2 bytes per short)
                    Latitude_minor_drawOrder.length * 2);
            dlb.order(ByteOrder.nativeOrder());
            drawListBuffer = dlb.asShortBuffer();
            drawListBuffer.put(Latitude_minor_drawOrder);
            drawListBuffer.position(0);

            GLES20.glUseProgram(mProgram);
            mPositionHandle = GLES20.glGetAttribLocation(mProgram,"vPosition");
            GLES20.glEnableVertexAttribArray(mPositionHandle);
            GLES20.glVertexAttribPointer(mPositionHandle,COORDS_PER_VERTEX,GLES20.GL_FLOAT
                    ,false,vertexStride,vertexBuffer);
            mColorHandle = GLES20.glGetUniformLocation(mProgram,"vColor");
            GLES20.glUniform4fv(mColorHandle,1,color_minor,0 );
            //GLES20.glDrawElements(GLES20.GL_LINES,drawOrder.length,GLES20.GL_UNSIGNED_SHORT,drawListBuffer);
            GLES20.glLineWidth(1.0f);
            GLES20.glDrawArrays(GLES20.GL_LINES,0,vertexCount);
            GLES20.glDisableVertexAttribArray(mPositionHandle);
        }

    }
    public void drawZLatitude_minor(float interval,float Xoffset,float Yoffset){
        GLES20.glUseProgram(mProgram);
        float rowWidth = (2.0f/3.0f-0.05f)/interval;

        short Latitude_minor_drawOrder[] = {0,1};

        for(float i = 0;i<=200;i++){
            if(i%100==0) continue;
            float LatitudeMajor[]={-1.0f+Xoffset,-0.95f+i*rowWidth,1.0f
                    -Xoffset,-0.95f+i*rowWidth};
            ByteBuffer bb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 4 bytes per float)
                    LatitudeMajor.length * 4);
            bb.order(ByteOrder.nativeOrder());
            vertexBuffer = bb.asFloatBuffer();
            vertexBuffer.put(LatitudeMajor);
            vertexBuffer.position(0);
            vertexCount = LatitudeMajor.length/COORDS_PER_VERTEX;

            ByteBuffer dlb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 2 bytes per short)
                    Latitude_minor_drawOrder.length * 2);
            dlb.order(ByteOrder.nativeOrder());
            drawListBuffer = dlb.asShortBuffer();
            drawListBuffer.put(Latitude_minor_drawOrder);
            drawListBuffer.position(0);

            GLES20.glUseProgram(mProgram);
            mPositionHandle = GLES20.glGetAttribLocation(mProgram,"vPosition");
            GLES20.glEnableVertexAttribArray(mPositionHandle);
            GLES20.glVertexAttribPointer(mPositionHandle,COORDS_PER_VERTEX,GLES20.GL_FLOAT
                    ,false,vertexStride,vertexBuffer);
            mColorHandle = GLES20.glGetUniformLocation(mProgram,"vColor");
            GLES20.glUniform4fv(mColorHandle,1,color_minor,0 );
            //GLES20.glDrawElements(GLES20.GL_LINES,drawOrder.length,GLES20.GL_UNSIGNED_SHORT,drawListBuffer);
            GLES20.glLineWidth(1.0f);
            GLES20.glDrawArrays(GLES20.GL_LINES,0,vertexCount);
            GLES20.glDisableVertexAttribArray(mPositionHandle);
        }

    }

    public void setMainLineColor(float color[]){
        color_mainline = color;
    }

}
