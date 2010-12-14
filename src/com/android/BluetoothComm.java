package com.android;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

public class BluetoothComm extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    
 // Message types sent from the BluetoothChatService Handler
    boolean bToothComm;
    
    
    private Driver callback;
    private TrafficHandler th;
    
    public BluetoothComm(Driver callback, BluetoothSocket socket, Handler mHandler) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        
        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut; 
        
        this.callback = callback;
        
    }

    @Override
	public void run() {
        byte[] buffer = new byte[100024];// buffer store for the stream
        int bytes; // bytes returned from read()
        bToothComm = true;
        String messagebuffer="";
        th=new TrafficHandler(callback);//traffic handler will queue all received data in a separate thread before passing it to the Activity
        th.start();
        
        // Keep listening to the InputStream until connection is termination
        while (bToothComm) {
            try {
                // Read from the InputStream
                bytes = mmInStream.read(buffer);
                
                //Convert Byte to String
                String msg = new String(buffer, 0, bytes);
                
                Log.d("rawreading",msg);
                String[] tempstorage=msg.split("\n");
                for(String i:tempstorage){
                	if(i!=null && i.contains("(")&& i.contains(")")){
    	                //publish message so it may be queued and transmitted to Activity when convenient
    	                th.receiveMsg(i);
                    }
                    //Wait for remainder of message by using buffer
                    else{
                    	if(!i.contains("(") && i.contains(")") && i!=null){
                    		messagebuffer+=i;
                    		th.receiveMsg(messagebuffer);
                    		messagebuffer="";
                    	}
                    	else{
                    		if(i!=null)
                    			messagebuffer+=i;
                    	}
                    }
                    	
                    
                }
            } 
            catch (IOException e) {
            	break;
            }
        }
    }
    
    public void StopComm(){
    	bToothComm = false;
    	try {
			mmSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    
    //publish information to callback interface
    public void announceRFIDReading(String rfidMsg){
    	callback.readingCallback(rfidMsg);
    }

    /* Call this from the main Activity to send data to the remote device */
    public void write(String str) {
        try {
        	byte[] bytes = str.getBytes();
            mmOutStream.write(bytes);
        } catch (IOException e) { }
    }

    /* Call this from the main Activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (Exception e) { }
    }
    public void terminateThread(){
    	
    	th.stop();
    }
}
