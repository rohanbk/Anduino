package com.android;
import java.util.LinkedList;
import java.util.Queue;

import android.util.Log;

public class TrafficHandler extends Thread{
	Queue<String> q;
	Driver callback;
	boolean flag;
	/*
	 * @author Rohan Balakrishnan
	 * @param: Driver object
	 * @return void
	 * 
	 * \brief Initialize queue for usage, and flag to indicate when thread should terminate
	 */
	public TrafficHandler(Driver callback) {
		this.callback=callback;
		q= new LinkedList<String>();
		flag=true;
	}
	/*
	 * @author Rohan Balakrishnan
	 * @param message: Received message from BluetoothComm class
	 * @return void
	 * 
	 * \brief When a message is received, lock the Queue and add a new entry to it
	 */
	
	public void receiveMsg(String message){
		
		synchronized (this.q) {
			try {
				Log.d("qsize", String.valueOf(q.size()));
				q.add(message);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void stopRunning(){
		flag=false;
	}
	/*
	 * @author Rohan Balakrishnan
	 * @param
	 * @return void
	 * 
	 * \brief Call thread to start running.
	 */
	@Override
	public void run(){
		
		//If flag is false, that means the main activity has decided to terminate it
		while(flag){
			//If queue has items, lock the queue, and transmit back to the Activity readingCallback method
			if(!q.isEmpty()){
				synchronized(this.q){
					try {				
						String data=q.remove();
						if(data.contains("(") && data.contains(")")){
							Log.d("TRANSMITTED",data);
							callback.readingCallback(data);
						}
						else
							Log.d("TRANSMITTED","ERROR");
					} catch (Exception e) {
						StackTraceElement[] s=e.getStackTrace();
						e.printStackTrace();
					}
				}
			}
		}
	}

}
