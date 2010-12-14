package com.android;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Driver extends Activity  {
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothSocket mmSocket;
	private BluetoothComm conn=null;
	private Handler mHandler;
	private final UUID ID_Name = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	Pattern pattern;
	Context context;
	private MediaPlayer mMediaPlayer;  
	BluetoothSocket bSocket;

	private Button closeButton;
	private Button startButton;

	Thread btthread; // Thread that is responsible for managing the Bluetooth connection
					
	File root;
	File datafile;
	FileWriter fw;
	BufferedWriter out;
	
	Calendar cal;
	SimpleDateFormat sdf;
	String time;
	
	/*
	 * @author Rohan Balakrishnan
	 * @param savedInstanceState
	 * 
	 * @return void
	 * 
	 * \brief Ostensible Main method of the application. 
	 * Creates two buttons: Start button (initiates connection with bluetooth device) and Stop button (terminates connection)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		//Toast.makeText(this, "Starting program...", Toast.LENGTH_SHORT).show();
		String enableBT = BluetoothAdapter.ACTION_REQUEST_ENABLE;
		startActivityForResult(new Intent(enableBT), 0);
		// Getting the Bluetooth adapter
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		
		//Check whether Bluetooth adapter is available 
		String toasttext;
		if (adapter.isEnabled()) {
			String address = adapter.getAddress();
			String name = adapter.getName();
			toasttext = name + ": " + address;
		} else {
			toasttext = "Adapter not available";
			Toast.makeText(this, toasttext, Toast.LENGTH_LONG);


		//Regex pattern of how received data should look like must be defined here
		pattern = Pattern.compile("*");
		
		context = this;
		//Create two buttons which will control start and termination of connection
		this.closeButton = (Button) this.findViewById(R.id.close);
		this.startButton = (Button) this.findViewById(R.id.start);

		// Initiate connection with Bluetooth
		this.startButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				
				Toast.makeText(context, "Starting sensing...", Toast.LENGTH_SHORT).show();
				btthread = new Thread(null, gatherSensorData, "SensorStart");
				btthread.start();

			}

		});
		
		//Stop connection upon button being clicked
		this.closeButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				
				Toast.makeText(context, "Stopping sensing...", Toast.LENGTH_SHORT).show();
				cancel();

			}
		});
		
		}
		
		

	}// end of onCreate

	/*
	 * @author Rohan Balakrishnan
	 * 
	 * @return void
	 * 
	 * \brief Creates a small beep when a connection has been established with the Bluetooth device
	 * 
	 */
	private void playAudio () {
        try {
            mMediaPlayer = MediaPlayer.create(this, R.raw.beep);
            mMediaPlayer.setLooping(false);
            Log.e("beep","started0");
            //Play beeping noise
            mMediaPlayer.start();
            mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
            	public void onCompletion(MediaPlayer arg0) {
            		if (mMediaPlayer != null) {
			            mMediaPlayer.release();
			            mMediaPlayer = null;
			        }
                }
        });
        } catch (Exception e) {
            Log.e("beep", "error: " + e.getMessage(), e);
        }
    }

	/*
	 * @author Rohan Balakrishnan
	 * @param
	 * @return void
	 * 
	 * \brief Things to do when activity resumes from a state of suspended animation
	 */
	@Override
	protected void onResume() {
		super.onResume();
		
	}
	/*
	 * @author Rohan Balakrishnan
	 * @param
	 * @return void
	 * 
	 * \brief Things to do when activity stops running 
	 */
	@Override
	protected void onStop() {
		super.onStop();
		
	}
	/*
	 * @author Rohan Balakrishnan
	 * @param
	 * @return void
	 * 
	 * \brief Destroys activity by terminating connection and thread managing it
	 */
	@Override
	protected void onDestroy(){
		super.onDestroy();
		conn.cancel();
		conn.terminateThread();
		
	}

	/*
	 * @author Rohan Balakrishnan
	 * @param
	 * @return void
	 * 
	 * \brief Runnable thread
	 */
	private Runnable gatherSensorData = new Runnable() {
		public void run() {
			AcquireData();
		}
	};
	
	/*
	 * @author Rohan Balakrishnan
	 * @param
	 * @return void
	 * 
	 * \brief Attempts to connect to Bluetooth device in question
	 */
	private void AcquireData() {
		BluetoothDevice device = GetPairedDevices();
		// If connection can be established connect
		if (device == null) {
			Toast.makeText(getApplicationContext(),"Could not find device",Toast.LENGTH_SHORT).show();
		} else {
			if (EstablishConnection(device)) {
				Connect();
				listenforArduino();
				playAudio();
			} else {
				Toast.makeText(getApplicationContext(),"Could Not Establish BT Connection",Toast.LENGTH_SHORT).show();
			}
		}
	}

	public BluetoothDevice GetPairedDevices() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		//Set of paired devices
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
				.getBondedDevices();
		// If there are paired devices
		if (pairedDevices.size() > 0) {
			// Loop through paired devices
			for (BluetoothDevice device : pairedDevices) {
				//Checks whether nearby paired device is an Arduino BT board
				if (device.getName().compareToIgnoreCase("ARDUINOBT") == 0) {
					Toast.makeText(getApplicationContext(),"Device Found",Toast.LENGTH_SHORT).show();
					return device;
				}
			}
		}
		return null;
	}

	public boolean EstablishConnection(BluetoothDevice device) {
		// Get a BluetoothSocket to connect with the given BluetoothDevice
		BluetoothSocket tmp = null;

		try {
			Method m = device.getClass().getMethod("createRfcommSocket",
					new Class[] { int.class });
			tmp = (BluetoothSocket) m.invoke(device, 1);

		} catch (Exception e) {
			Log.d("ESTABLISHCONN", e.getMessage());
			return false;
		}
		mmSocket = tmp;
		return true;
	}

	private void Connect() {
		try {
			// Connect the device through the socket. This will block
			// until it succeeds or throws an exception
			mmSocket.connect();
		} catch (IOException connectException) {
			// Unable to connect; close the socket and get out
			try {
				mmSocket.close();
			} catch (IOException closeException) {
			}
			return;
		}
		// Manage socket connection
		conn = new BluetoothComm(this, mmSocket, mHandler);
	}

	//Start Bluetooth Socket Thread
	public void listenforArduino() {
		try{
			conn.start();
		}catch(NullPointerException e){
			Log.d("listenforArduinoError",e.getMessage());
		}
	}

	public BluetoothComm GetBluetoothConnection() {
		return conn;
	}

	public boolean IsConnected() {
		if (conn == null) {
			return false;
		} else {
			return true;
		}
	}

	/** Will cancel an in-progress connection, and close the socket */
	public void cancel() {
		try {
			mmSocket.close();
		} 
		catch (IOException e) {
		
		}
	}

	
	/*
	 * The following method needs to be modified as per the 
	 * specifications of the user. The Bluetooth Comm class will call the
	 * readingCallback method and pass a record as a parameter.
	 * 
	 * This record needs to be parsed, and checked using the Pattern object 'pattern'
	 * defined in the onCreate method. This object will ensure that the data format
	 * is correct.
	 */
	public void readingCallback(String reading) {

		try {
			Log.d("readingCallback ", reading);

			Matcher m = pattern.matcher(reading);
			if (!m.find()) {
				Log.d("CALLBACK ", reading);
				throw new IllegalMessageFormatException(reading);
			}
		} catch (Exception e) {
			Log.d("CALLBACK ", reading);
		}
	}
	/*
	 * @author Rohan Balakrishnan
	 * @param
	 * @return void
	 * 
	 * \brief This class is called if a received message in the readingCallback method is of an Incorrect Format
	 */

	class IllegalMessageFormatException extends Exception {
		private static final long serialVersionUID = 7713974062768949215L;
		String mistake;

		public IllegalMessageFormatException(String message) {
			super(message);
			mistake = "Message Format Illegal: " + message;
		}

		public String getError() {
			return mistake;
		}
	}

}