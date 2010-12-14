package com.android;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

public class BluetoothService extends Service implements BluetoothReadingsInterface {
	
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothSocket mmSocket;
    private BluetoothComm conn;
    private Handler mHandler;
    //private final BluetoothDevice mmDevice;
    private final UUID ID_Name = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    
    public static final String NEW_GPS_LOCATION = "New_GPS_Location";
    
    
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;


	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO: Actions to perform when service is created.
		
		
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		Thread thread = new Thread(null, doBackgroundThreadProcessing, "Background");
		thread.start();
	}
	
	@Override
	public void onDestroy (){
		super.onDestroy();  
		{  
			DisableListener();
			
	    }
	}
	

	
	
	public void DisableListener(){
		//this.location.removeUpdates(locListen);
		conn.StopComm();
	}

	// Runnable that executes the background processing method.
	private Runnable doBackgroundThreadProcessing = new Runnable() {
	  public void run() {
		  BackgroundConnectThread();
	  }
	};
	
	private void BackgroundConnectThread(){
		BluetoothDevice device = GetPairedDevices();
		//If connection can be established connect
		if(device == null){
			//Toast.makeText( getApplicationContext(),"Could not find M220",Toast.LENGTH_SHORT).show();
		}
		else if(EstablishConnection(device)){
			Connect();
			InitializeM220Reader();
			ListenToM220Reader();
		}
		else{
			//Toast.makeText( getApplicationContext(),"Could Not Establish BT Connection",Toast.LENGTH_SHORT).show();
		}
	}
	
	public BluetoothDevice GetPairedDevices(){
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    	Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
    	// If there are paired devices
    	//Toast.makeText( getApplicationContext(),"Searching For M220_AD80142",Toast.LENGTH_SHORT).show();
    	if (pairedDevices.size() > 0) {
    	    // Loop through paired devices
    	    for (BluetoothDevice device : pairedDevices) {
				//if equals to reader name lets do something
    	    	if(device.getName().compareToIgnoreCase("M220_AA00002") == 0){
    	    		//Toast.makeText( getApplicationContext(),"Device Found",Toast.LENGTH_SHORT).show();
    	    		return device;
    	    	}
    	    }
    	}
    	return null;
    }
	
	public boolean EstablishConnection(BluetoothDevice device){
    	// Get a BluetoothSocket to connect with the given BluetoothDevice
    	BluetoothSocket tmp = null;
    	
    	try {
        	// MY_UUID is the app's UUID string, also used by the server code
        	tmp = device.createRfcommSocketToServiceRecord(ID_Name);
        	
        } 
        catch (IOException e) {return false;}
        mmSocket = tmp;
        return true;
    }
	    
	    
	
	private void Connect(){
		try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mmSocket.connect();
        } 
        catch (IOException connectException) {
        	// Unable to connect; close the socket and get out
	        try {
                mmSocket.close();
            } 
	        catch (IOException closeException) { }
	        return;
        }        
        //Manage socket connection

        // Toast.makeText( getApplicationContext(),"Connected to M220",Toast.LENGTH_SHORT).show();
	}
	
	public void InitializeM220Reader(){
		conn.write("M,0\r");
		conn.write("G,LOCATE,04,0,1,1\r");
		conn.write("O,2,40,0,0\r");
		conn.write("S,1\r");
		conn.write("U,82\r");
		conn.write("M,433\r");
	}
	
	public void ListenToM220Reader(){
		conn.start();
	}
    
    public BluetoothComm GetBluetoothConnection(){
    	return conn;
    }
    
    public boolean IsConnected(){
    	if(conn == null){
    		return false;
    	}
    	else{
    		return true;
    	}
    }

    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        try {
            mmSocket.close();
        } 
        catch (IOException e) { }
    }
    
	BluetoothSocket bSocket;

    public final static String NEW_RFID_READING = "New_RFID_Reading";
    
    //This is a callback through the implemented interface
    //Retrieves Victim Information
	@Override
	public void readingCallback(String reading) {

	}
	
	
}












