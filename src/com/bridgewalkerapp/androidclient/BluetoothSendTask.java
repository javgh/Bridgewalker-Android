package com.bridgewalkerapp.androidclient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.bridgewalkerapp.androidclient.data.Maybe;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class BluetoothSendTask implements Runnable {
	private static final UUID BITCOIN_BLUETOOTH_UUID = UUID.fromString("3357A7BB-762D-464A-8D9A-DCA592D57D5B");
	
	private BlockingQueue<Maybe<String>> queue;
	private BluetoothAdapter bluetoothAdapter;
	private String bluetoothAddress;
	
	public BluetoothSendTask(BlockingQueue<Maybe<String>> queue, BluetoothAdapter bluetoothAdapter, String bluetoothAddress) {
		this.queue = queue;
		this.bluetoothAdapter = bluetoothAdapter;
		this.bluetoothAddress = bluetoothAddress;
	}
	
	private byte[] hexStringToByteArray(String s) {
		if (s == null)
			return null;
		
		if (s.length() % 2 != 0)
			return null;
		
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
	private String expandAddress(String bluetoothAddress) {
		if (bluetoothAddress == null)
			return null;
		
		if (bluetoothAddress.length() < 2)
			return null;
		
		if (bluetoothAddress.length() % 2 != 0)
			return null;
		
		StringBuilder result = new StringBuilder();                           
		for (int i = 0; i < bluetoothAddress.length(); i += 2)                      
			result.append(bluetoothAddress.substring(i, i + 2)).append(':');           
		result.setLength(result.length() - 1);                                         
		                                                                                 
		return result.toString();    
	}
	
	// String tx
	
	@Override
	public void run() {
		if (bluetoothAddress == null)
			return;
		
		String expandedBtAddr = expandAddress(bluetoothAddress);
		if (expandedBtAddr == null)
			return;
		
		BluetoothSocket socket = null;
		DataOutputStream out = null;
		DataInputStream in = null;
		
		try {
			// already prepare connection to save time
			BluetoothDevice device = bluetoothAdapter.getRemoteDevice(expandedBtAddr);
			socket = device.createInsecureRfcommSocketToServiceRecord(BITCOIN_BLUETOOTH_UUID);
			
			socket.connect();
			in = new DataInputStream(socket.getInputStream());                                                   
			out = new DataOutputStream(socket.getOutputStream());
			
			// wait for transaction - but no longer than 30 seconds
			Maybe<String> mTx = this.queue.poll(30, TimeUnit.SECONDS);
			if (mTx == null)
				return;
			
			String tx = mTx.getValue();
			if (tx == null)
				return;
			
			byte[] txBytes = hexStringToByteArray(tx);
			if (txBytes == null)
				return;
			
			out.writeInt(1);                                                                                      
			out.writeInt(txBytes.length);                                                                    
			out.write(txBytes);                                                                              
			out.flush();      
			
			in.readBoolean();	/* read ack, but ignore for now */
		} catch (IOException x) {
			// ignore
		} catch (InterruptedException x) {
			// ignore
		} finally {                                                                                                
			if (out != null) {
				try { out.close(); } catch (IOException x) { /* ignore */ }
			}
			if (in != null) {
				try { in.close(); } catch (IOException x) { /* ignore */ }
			}
			if (socket != null) {
				try { socket.close(); } catch (IOException x) { /* ignore */ }
			}
		}
	}
}
