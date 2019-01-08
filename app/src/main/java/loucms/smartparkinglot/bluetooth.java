package loucms.smartparkinglot;

import android.animation.ObjectAnimator;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by loucms on 11/24/17.
 */

public class bluetooth {
    private static final UUID MY_UUID_SECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String TAG="bluetooth";
    public final int STATE_NONE = 0;       // we're doing nothing
    public final int STATE_LISTEN = 1;     // now listening for incoming connections
    public final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public final int STATE_CONNECTED = 3;  // now connected to a remote device
    private BluetoothAdapter adapter;
    private BluetoothDevice device;
    private boolean secure = false;
    private int state;
    private int newState;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    public megnetoChart megnetoChart;
    public circleImageView deviceCircleImageView;
    private int totalPacketLength = 0;
    private byte[] Packet = new byte[24];
    private int packetIndex = 0;
    private boolean askToSend = false;
    private float xdata[] = new float[120];
    private float ydata[] = new float[120];
    private float zdata[] = new float[120];
    private float databuf[][] = new float[3][120];
    private float xFirst;
    private float yFirst;
    private float zFirst;
    private int timeCounterMM = 0;
    private int timeCounterSS = 0;
    private LinearLayout mLayout;
    private ViewGroup mViewGroup;
    private Context mContext;
    public LayoutInflater inflater;
    private ObjectAnimator objectAnimator;
    private boolean openfile=false;
    private File outputFile;
    private FileOutputStream mFileOutputStream;
    private int previousTime;
    private byte[] previousByteTime = new byte[4];
    private boolean firstround=true;
    private boolean setInitialValue = false;
    private byte[] temp;
    private byte whitespace = 32;
    public OutputStreamWriter bluetoothDataOutputWriter;
    public OutputStream bluetoothDataOutput;
    private URL myUrl;
    public byte[] output_to_web;
    public boolean readyOutputToWeb = false;
    public HttpURLConnection myURLConnection;
    public BufferedReader myReader;
    public bluetooth(BluetoothDevice Device, Context context, LinearLayout layout, ViewGroup viewGroup){
        adapter = BluetoothAdapter.getDefaultAdapter();
        device = Device;
        state = STATE_NONE;
        mContext = context;
        mLayout = layout;
        mViewGroup = viewGroup;
        /*try{
            myUrl = new URL("https://smartparkingspace.nctu.me/web6s/connect");

            if(myUrl!=null){
                Log.d(TAG,"myUrl is not null");
            }
        }catch (MalformedURLException e){
            Log.d(TAG,"new URL failed!");
        }catch (IOException e){
            Log.d(TAG,"openConnection() failed!");
        }*/

        megnetoChart = new megnetoChart(mContext,viewGroup);
        inflater = LayoutInflater.from(mContext);
        //deviceCircleImageView =new circleImageView(mContext);
        deviceCircleImageView =(circleImageView)inflater.inflate(R.layout.bluetooth_circleview,layout,false);
        String name = "ic_"+device.getName();
        int id = mContext.getResources().getIdentifier(name, "drawable", mContext.getPackageName());
        deviceCircleImageView.setImageResource(id);
        layout.addView(deviceCircleImageView);
        //deviceCircleImageView.inflate(mContext,R.layout.bluetooth_circleview,viewGroup);
        deviceCircleImageView.setOnClickListener(myListener);
        objectAnimator= ObjectAnimator.ofInt(deviceCircleImageView,"BorderColorResource",R.color.bluetoothConnectBorderColor
        ,R.color.bluetoothDataTransmitBorderColor,R.color.bluetoothConnectBorderColor);
        objectAnimator.setDuration(600);
        temp = new byte[27];
        connect();

    }

    public ImageView.OnClickListener myListener = new ImageView.OnClickListener(){

        @Override
        public void onClick(View v) {
            if(!askToSend) {
                sendMessage("s");
                askToSend = true;
                deviceCircleImageView.setBorderOverlay(true);
                deviceCircleImageView.setBorderColorResource(R.color.bluetoothDataTransmitBorderColor);
                megnetoChart.setVisibility(View.VISIBLE);
            }
            else {
                sendMessage("e");
                askToSend = false;
                deviceCircleImageView.setBorderOverlay(false);
                firstround = true;
            }
        }
    };
    public synchronized int getState() {
        return state;
    }
    private synchronized void updateUserInterfaceTitle() {
        state = getState();
        Log.d(TAG, "updateUserInterfaceTitle() " + newState + " -> " + state);
        newState = state;

        // Give the new state to the Handler so the UI Activity can update
        handler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, newState, -1).sendToTarget();
    }
    public void connect(){
        // Cancel any thread attempting to make a connection
        if (state == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        mConnectThread = new ConnectThread(device, secure);
        mConnectThread.start();
        // Update UI title
        updateUserInterfaceTitle();
    }


    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device, boolean secure) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                if (secure) {
                    tmp = device.createRfcommSocketToServiceRecord(
                            MY_UUID_SECURE);
                } else {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(
                            MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            }
            mmSocket = tmp;
            state = STATE_CONNECTING;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);

            // Always cancel discovery because it will slow down a connection
            adapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    Log.d(TAG,"Prepare to close.");
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " + mSocketType +
                            " socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }
            Log.d(TAG,""+adapter.getName()+" and "+ mmDevice.getName()+" success connect");
            // Reset the ConnectThread because we're done
            synchronized (this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice, mSocketType);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
            }
        }
    }
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device, final String socketType) {
        Log.d(TAG, "connected, Socket Type:" + socketType);

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = handler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        handler.sendMessage(msg);
        // Update UI title
        updateUserInterfaceTitle();
    }
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private int index=0;
        private byte[] buf = new byte[24];

        public ConnectedThread(BluetoothSocket socket, String socketType) {
            Log.d(TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp Stream not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            state = STATE_CONNECTED;
            //try {
                //*myURLConnection = (HttpURLConnection)myUrl.openConnection();//step 1, create a connection
                //*myURLConnection.setAllowUserInteraction(true);
                //myURLConnection.setDoOutput(true);//step 2,The setup parameters and general request properties are manipulated.
                //*myURLConnection.setDoInput(true);//step 2,The setup parameters and general request properties are manipulated.
                //*myURLConnection.setRequestProperty("accept", "*/*");
                //*myURLConnection.setRequestProperty("Content-Type", "application/json");
                //*myURLConnection.setRequestProperty("Connection", "keep-alive");
                //*myURLConnection.setRequestMethod("GET");
                //*myURLConnection.connect();
                //bluetoothDataOutputWriter = new OutputStreamWriter(myURLConnection.getOutputStream());
                //bluetoothDataOutput = myURLConnection.getOutputStream();
                //*myReader = new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()));
            //} catch (IOException e) {
                //e.printStackTrace();
            //}



        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[24];
            byte[] temp = new byte[24];
            int tempIndex = 0;
            int bytes;

            // Keep listening to the InputStream while connected
            while (state == STATE_CONNECTED) {
                try {
                    // Read from the InputStream

                    /*String tempString ;//just for testing
                    tempString = "{\"sensor_id\":"+8+",\"x_data\":"+15.3+",\"y_data\":"+24.8
                            +",\"z_data\":"+17.9+",\"sensor_status\":"+false+",\"timestamp\":"+MainActivity.systemSS+"}";
                    byte[] out = tempString.getBytes(StandardCharsets.UTF_8);*/

                    //*myURLConnection = (HttpURLConnection)myUrl.openConnection();//step 1, create a connection
                    //*myURLConnection.setAllowUserInteraction(true);
                    //*myURLConnection.setDoOutput(true);//step 2,The setup parameters and general request properties are manipulated.
                    //myURLConnection.setDoInput(true);//step 2,The setup parameters and general request properties are manipulated.
                    //myURLConnection.setFixedLengthStreamingMode(tempString.length());
                    //*myURLConnection.setChunkedStreamingMode(0);               //step 2,The setup parameters and general request properties are manipulated.
                    //*myURLConnection.setRequestProperty("accept", "application/json");
                    //*myURLConnection.setRequestProperty("Content-Type", "application/json");
                    //*myURLConnection.setRequestProperty("Connection", "keep-alive");
                    //*myURLConnection.setRequestMethod("POST");
                    //*myURLConnection.connect();                                  //step 3,The actual connection to the remote object is made, using the connect method


                    //bluetoothDataOutputWriter = new OutputStreamWriter(myURLConnection.getOutputStream());//this is second choice
                    //bluetoothDataOutputWriter.write(tempString);


                    //bluetoothDataOutput = myURLConnection.getOutputStream();//this is first choice
                    //bluetoothDataOutput.write(out);
                    //myReader = new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()));
                    //if(myReader!=null) Log.d(TAG,"myReader"+myReader.readLine());
                    //else Log.d(TAG,"myReader is null ");

                    //Log.d(TAG,"timestamp : "+MainActivity.systemSS);
                    //Log.d(TAG,"tempString : "+tempString);
                    //Log.d(TAG,"tempString to byet : "+String.valueOf(out));
                    //Log.d(TAG,"myUrl : "+myUrl.toString());
                    //Log.d(TAG,"myURLConnection : "+myURLConnection.toString());
                    //Log.d(TAG,"getResponseCode : "+myURLConnection.getResponseCode());
                    //Log.d(TAG,"getContentLengthe : "+myURLConnection.getContentLength());
                    //Log.d(TAG,"getContentType : "+myURLConnection.getContentType());
                    //Log.d(TAG,"getHeaderField : "+myURLConnection.getHeaderField("Host"));
                    //myReader = new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()));
                    //if(myReader!=null) Log.d(TAG,"myReader"+myReader.readLine());
                    //else Log.d(TAG,"myReader is null ");
                    //Log.d(TAG,"getResponseMessage : "+myURLConnection.getResponseMessage());
                    /*if(readyOutputToWeb){//if you want to post, you must implement post and get
                        myURLConnection = (HttpURLConnection)myUrl.openConnection();//step 1, create a connection
                        myURLConnection.setAllowUserInteraction(true);
                        myURLConnection.setDoOutput(true);//step 2,The setup parameters and general request properties are manipulated.
                        //myURLConnection.setDoInput(true);//step 2,The setup parameters and general request properties are manipulated.
                        //myURLConnection.setFixedLengthStreamingMode(tempString.length());
                        myURLConnection.setChunkedStreamingMode(0);               //step 2,The setup parameters and general request properties are manipulated.
                        myURLConnection.setRequestProperty("accept", "application/json");
                        myURLConnection.setRequestProperty("Content-Type", "application/json");
                        myURLConnection.setRequestProperty("Connection", "keep-alive");
                        myURLConnection.setRequestMethod("POST");
                        myURLConnection.connect();                                  //step 3,The actual connection to the remote object is made, using the connect method

                        bluetoothDataOutput = myURLConnection.getOutputStream();
                        bluetoothDataOutput.write(output_to_web);
                        myReader = new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()));
                        if(myReader!=null) Log.d(TAG,"myReader"+myReader.readLine());
                        else Log.d(TAG,"myReader is null ");
                        readyOutputToWeb = false;
                        myURLConnection.disconnect();//this statement is very important
                    }*/


                    bytes = mmInStream.read(buffer);
                    if(buffer[0]==35){
                        for(int i = 0;i<bytes;i++){
                            temp[tempIndex++] = buffer[i];
                        }
                    }else if(buffer[bytes-1]==36){
                        for(int i = 0;i<bytes;i++){
                            temp[tempIndex++] = buffer[i];
                        }
                        temp[tempIndex]='\0';
                    }
                    for(int i = 0;i<24;i++){
                        Log.d(TAG,"temp["+i+"] = "+temp[i]);
                    }
                    Log.d(TAG,"tempIndex = "+tempIndex);

                    for(int i = 0;i<bytes;i++){
                        Log.d(TAG,"buffer["+i+"] = "+buffer[i]);
                    }  //for debug

                    Log.d(TAG,"the total byte read from Bluetooth is "+ bytes);
                    if(temp[0]==35&&temp[22]==36){
                        for(int i = 0;i<24;i++){
                            Log.d(TAG,"temp["+i+"] = "+temp[i]);
                        }
                        handler.obtainMessage(Constants.MESSAGE_READ, 23, -1, temp)
                                .sendToTarget();
                        tempIndex = 0;
                        temp = new byte[24];
                    }

                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                } //finally {
                    //myURLConnection.disconnect();
                //}
            }
        }
        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
                handler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
    private void connectionFailed() {
        // Send a failure message back to the Activity
        Message msg = handler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Unable to connect device");
        msg.setData(bundle);
        handler.sendMessage(msg);

        state = STATE_NONE;
        // Update UI title
        updateUserInterfaceTitle();

        // Start the service over to restart listening mode
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        // Send a failure message back to the Activity
        Message msg = handler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Device connection was lost");
        msg.setData(bundle);
        handler.sendMessage(msg);
        state = STATE_NONE;
        updateUserInterfaceTitle();

    }
    public void sendMessage(String message) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (state != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        if(message.length()>0){
            byte[]out = message.getBytes();
            Log.d(TAG," out = "+out[0]);
            r.write(out);
        }
    }
    public final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            char[] readBuffer = new char[24];
            final String Handel_TAG="Bluetooth Handler";
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case STATE_CONNECTED:
                            Log.d(Handel_TAG,""+device.getName()+" and "+adapter.getName()
                                    +" are connected.");
                            deviceCircleImageView.setBorderOverlay(true);
                            deviceCircleImageView.setBorderColorResource(R.color.bluetoothConnectBorderColor);
                            break;
                        case STATE_CONNECTING:

                            break;
                        case STATE_LISTEN:
                        case STATE_NONE:
                            deviceCircleImageView.setBorderOverlay(false);
                            //deviceCircleImageView.setVisibility(View.GONE);
                            mLayout.removeView(deviceCircleImageView);
                            mViewGroup.removeView(megnetoChart.myGLSurfaceView);
                            if(mFileOutputStream!=null){
                                try {
                                    mFileOutputStream.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                mFileOutputStream = null;
                            }
                            update_connectBTs();

                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    Log.d(Handel_TAG,"The message : "+writeMessage+"is sent.");
                    break;
                case Constants.MESSAGE_READ:
                    // construct a string from the valid bytes in the buffer
                    byte[] buf = (byte[])msg.obj;
                    dealWithPacket(msg.arg1,buf);
                    /*if(buf[0]==35||buf[msg.arg1-1]==36){
                        Log.d(Handel_TAG,"buf[0] = "+(char)buf[0]+" : buf[msg.arg1-1] = "+(char)buf[msg.arg1-1]);
                        dealWithPacket(msg.arg1,buf);
                    }*/

                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    Log.d(Handel_TAG,""+msg.getData().getString(Constants.DEVICE_NAME)+" is connected.");
                    //show the device is connected
                    //connectedDevice.setText("connect to "+mConnectedDeviceName);
                    break;
                case Constants.MESSAGE_TOAST:
                    Log.d(Handel_TAG,msg.getData().getString(Constants.TOAST));
                    break;
            }
        }
    };
    public synchronized void dealWithPacket(int bytes,byte[] buf){

        totalPacketLength +=bytes;
        Log.d(TAG,"totalPacketLength is "+ totalPacketLength);
        if(totalPacketLength<23){
            for(int i = 0;i<bytes;i++){
                Packet[packetIndex++] = buf[i];
                Log.d(TAG,"The whole packet received is "+Packet[i]);
            }
        }else if(totalPacketLength==23){
            for(int i = 0;i<bytes;i++){
                Packet[packetIndex] = buf[i];
                if(packetIndex<=23){
                    Packet[packetIndex] = buf[i];
                }
                packetIndex++;
            }
            totalPacketLength = 0;
            packetIndex = 0;
            for(int i = 0;i<24;i++)
            Log.d(TAG,"The whole packet received is "+Packet[i]);
            float a = 0;
            float b = 0;
            float c = 0;
            float ipart = 0;
            float fpart = 0;
            if(Packet[1]==45){
                ipart = (Packet[2]-48)*100+(Packet[3]-48)*10+(Packet[4]-48);
                fpart = (Packet[5]-48)*100+(Packet[6]-48)*10+(Packet[7]-48);
                fpart =  fpart/1000;
                a = (ipart+fpart)*-1;
            }else{
                ipart = (Packet[2]-48)*100+(Packet[3]-48)*10+(Packet[4]-48);
                fpart = (Packet[5]-48)*100+(Packet[6]-48)*10+(Packet[7]-48);
                fpart =  fpart/1000;
                a = (ipart+fpart);
            }
            if(Packet[8]==45){
                ipart = (Packet[9]-48)*100+(Packet[10]-48)*10+(Packet[11]-48);
                fpart = (Packet[12]-48)*100+(Packet[13]-48)*10+(Packet[14]-48);
                fpart =  fpart/1000;
                b = (ipart+fpart)*-1;
            }else{
                ipart = (Packet[9]-48)*100+(Packet[10]-48)*10+(Packet[11]-48);
                fpart = (Packet[12]-48)*100+(Packet[13]-48)*10+(Packet[14]-48);
                fpart =  fpart/1000;
                b = (ipart+fpart);
            }
            if(Packet[15]==45){
                ipart = (Packet[16]-48)*100+(Packet[17]-48)*10+(Packet[18]-48);
                fpart = (Packet[19]-48)*100+(Packet[20]-48)*10+(Packet[21]-48);
                fpart =  fpart/1000;

                c = (ipart+fpart)*-1;
            }else{
                ipart = (Packet[16]-48)*100+(Packet[17]-48)*10+(Packet[18]-48);
                fpart = (Packet[19]-48)*100+(Packet[20]-48)*10+(Packet[21]-48);
                fpart =  fpart/1000;

                c = (ipart+fpart);
            }
            //showX.setText("X : "+String.valueOf(a));
            //showY.setText("Y : "+String.valueOf(b));
            //showZ.setText("Z : "+String.valueOf(c));
            //timeCounter.setText(""+timeCounterMM+" : "+timeCounterSS);
            if(timeCounterSS==0&&setInitialValue==false){
                xFirst = a;
                yFirst = b;
                zFirst = c;
                setInitialValue = true;
            }
            if(timeCounterMM==0){
                databuf[0][timeCounterSS*2] = timeCounterSS;
                //databuf[0][timeCounterSS*2+1] = a-xFirst;
                databuf[0][timeCounterSS*2+1] = a;
                databuf[1][timeCounterSS*2] = timeCounterSS;
                //databuf[1][timeCounterSS*2+1] = b-yFirst;
                databuf[1][timeCounterSS*2+1] = b;
                databuf[2][timeCounterSS*2] = timeCounterSS;
                //databuf[2][timeCounterSS*2+1] = /*(c-xFirst)/1*/c-zFirst;
                databuf[2][timeCounterSS*2+1] = c;
                /*xdata[timeCounterSS*2] = timeCounterSS;
                xdata[timeCounterSS*2+1] = (a-xFirst)/1;
                ydata[timeCounterSS*2] = timeCounterSS;
                ydata[timeCounterSS*2+1] = (b-yFirst)/1;
                zdata[timeCounterSS*2] = timeCounterSS;
                zdata[timeCounterSS*2+1] = (c-zFirst)/1;
                megnetoChart.updateFragment(xdata,timeCounterSS+1);*/
                megnetoChart.updateFragment(databuf,timeCounterSS+1);
                objectAnimator.start();


                String tempString ;
                tempString = "{\"sensor_id\":"+1+",\"x_data\":"+a+",\"y_data\":"+b
                        +",\"z_data\":"+c+",\"sensor_status\":"+false+",\"timestamp\":"+MainActivity.systemSS+"}";
                output_to_web = tempString.getBytes(StandardCharsets.UTF_8);
                readyOutputToWeb = true;
            }else{
                for(int i = 1;i<=117;i+=2){
                    databuf[0][i] = databuf[0][i+2];
                    databuf[1][i] = databuf[1][i+2];
                    databuf[2][i] = databuf[2][i+2];
                    /*xdata[i] = xdata[i+2];
                    ydata[i] = ydata[i+2];
                    zdata[i] = zdata[i+2];*/
                }
                /*xdata[119] = (a-xFirst)/xFirst;
                ydata[119] = (b-yFirst)/yFirst;
                zdata[119] = (c-zFirst)/zFirst;
                megnetoChart.updateFragment(xdata,60);*/
                //databuf[0][119] = a-xFirst;
                databuf[0][119] = a;
                //databuf[1][119] = b-yFirst;
                databuf[1][119] = b;
                //databuf[2][119] = c-zFirst;
                databuf[2][119] = c;
                megnetoChart.updateFragment(databuf,60);
                objectAnimator.start();
                String tempString ;
                tempString = "{\"sensor_id\":"+1+",\"x_data\":"+a+",\"y_data\":"+b
                        +",\"z_data\":"+c+",\"sensor_status\":"+false+",\"timestamp\":"+MainActivity.systemSS+"}";
                output_to_web = tempString.getBytes(StandardCharsets.UTF_8);
                readyOutputToWeb = true;
            }
            timeCounterSS++;
            if(timeCounterSS==60){
                timeCounterMM++;
                timeCounterSS = 0;
            }
            if(MainActivity.isBluetoothDevicesRecording==true&&openfile==false){
                try {
                    outputFile = File.createTempFile(new SimpleDateFormat("MM.dd HH:mm:ss").format(new Date())
                            +device.getName(),".txt",MainActivity.applicationFolder);
                    Log.d(TAG,""+outputFile.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    mFileOutputStream = new FileOutputStream(outputFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                openfile=true;
            }
            if(MainActivity.isBluetoothDevicesRecording){
                if(firstround){
                    previousTime = MainActivity.systemSS;
                    previousByteTime = MainActivity.time;
                    firstround = false;
                    try {
                        mFileOutputStream.write(previousByteTime);
                        mFileOutputStream.write(whitespace);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    temp[0] = Packet[1];
                    temp[1] = Packet[2];
                    temp[2] = Packet[3];
                    temp[3] = Packet[4];
                    temp[4] = 46;
                    temp[5] = Packet[5];
                    temp[6] = Packet[6];
                    temp[7] = Packet[7];
                    temp[8] = 32;
                    temp[9] = Packet[8];
                    temp[10] = Packet[9];
                    temp[11] = Packet[10];
                    temp[12] = Packet[11];
                    temp[13] = 46;
                    temp[14] = Packet[12];
                    temp[15] = Packet[13];
                    temp[16] = Packet[14];
                    temp[17] = 32;
                    temp[18] = Packet[15];
                    temp[19] = Packet[16];
                    temp[20] = Packet[17];
                    temp[21] = Packet[18];
                    temp[22] = 46;
                    temp[23] = Packet[19];
                    temp[24] = Packet[20];
                    temp[25] = Packet[21];
                    temp[26] = 10;
                    try {
                        mFileOutputStream.write(temp);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{
                    try {
                        mFileOutputStream.write(MainActivity.time);
                        mFileOutputStream.write(whitespace);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    temp[0] = Packet[1];
                    temp[1] = Packet[2];
                    temp[2] = Packet[3];
                    temp[3] = Packet[4];
                    temp[4] = 46;
                    temp[5] = Packet[5];
                    temp[6] = Packet[6];
                    temp[7] = Packet[7];
                    temp[8] = 32;
                    temp[9] = Packet[8];
                    temp[10] = Packet[9];
                    temp[11] = Packet[10];
                    temp[12] = Packet[11];
                    temp[13] = 46;
                    temp[14] = Packet[12];
                    temp[15] = Packet[13];
                    temp[16] = Packet[14];
                    temp[17] = 32;
                    temp[18] = Packet[15];
                    temp[19] = Packet[16];
                    temp[20] = Packet[17];
                    temp[21] = Packet[18];
                    temp[22] = 46;
                    temp[23] = Packet[19];
                    temp[24] = Packet[20];
                    temp[25] = Packet[21];
                    temp[26] = 10;
                    try {
                        mFileOutputStream.write(temp);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


                /*if(MainActivity.systemSS-previousTime>1){
                    for(int i = previousTime+1;i<=MainActivity.systemSS;i++){
                        try {
                            previousByteTime[3] = (byte)(previousByteTime[3]+1);
                            if(previousByteTime[3]=='0') previousByteTime[2] = (byte)(previousByteTime[2]+1);
                            if(previousByteTime[2]=='0') previousByteTime[1] = (byte)(previousByteTime[1]+1);
                            if(previousByteTime[1]=='0') previousByteTime[0] = (byte)(previousByteTime[0]+1);
                            mFileOutputStream.write(previousByteTime);
                            mFileOutputStream.write(whitespace);
                            mFileOutputStream.write(temp);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        previousTime = MainActivity.systemSS;
                        previousByteTime = MainActivity.time;
                    }
                }
                else if(MainActivity.systemSS-previousTime==1&&MainActivity.systemSS-previousTime!=0){
                    try {
                        mFileOutputStream.write(MainActivity.time);
                        mFileOutputStream.write(whitespace);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    byte[] temp = new byte[27];
                    temp[0] = Packet[1];
                    temp[1] = Packet[2];
                    temp[2] = Packet[3];
                    temp[3] = Packet[4];
                    temp[4] = 46;
                    temp[5] = Packet[5];
                    temp[6] = Packet[6];
                    temp[7] = Packet[7];
                    temp[8] = 32;
                    temp[9] = Packet[8];
                    temp[10] = Packet[9];
                    temp[11] = Packet[10];
                    temp[12] = Packet[11];
                    temp[13] = 46;
                    temp[14] = Packet[12];
                    temp[15] = Packet[13];
                    temp[16] = Packet[14];
                    temp[17] = 32;
                    temp[18] = Packet[15];
                    temp[19] = Packet[16];
                    temp[20] = Packet[17];
                    temp[21] = Packet[18];
                    temp[22] = 46;
                    temp[23] = Packet[19];
                    temp[24] = Packet[20];
                    temp[25] = Packet[21];
                    temp[26] = 10;
                    try {
                        mFileOutputStream.write(temp);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(previousTime==MainActivity.systemSS-1) {
                        previousTime= MainActivity.systemSS;
                        previousByteTime = MainActivity.time;
                    }
                }*/
            }
            if(askToSend)
                sendMessage("s");
            else
                sendMessage("e");

        }

    }
    private synchronized void update_connectBTs(){
        if(state==STATE_NONE){
            if(MainActivity.connectBTs>0){
                MainActivity.connectBTs--;
            }

        }
        Log.d(TAG,"MainActivity.connectBTs = "+MainActivity.connectBTs);
    }
    public void stopBluetooth(){
        if(askToSend){
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
            if (mConnectedThread != null) {
                mConnectedThread.cancel();
                mConnectedThread = null;
            }
        }else{
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
            if (mConnectedThread != null) {
                mConnectedThread.cancel();
                mConnectedThread = null;
            }
        }
        if(mFileOutputStream!=null){
            try {
                mFileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mFileOutputStream = null;
        }
        state = STATE_NONE;
        updateUserInterfaceTitle();
    }


}
