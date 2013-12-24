package com.bridgewalkerapp.androidclient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class BluetoothUtils {
	private static final UUID BITCOIN_BLUETOOTH_UUID = UUID.fromString("3357A7BB-762D-464A-8D9A-DCA592D57D5B");
	
	public static byte[] hexStringToByteArray(String s) {
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
	
	public static String expandAddress(String bluetoothAddress) {
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
	
	public static void broadcastTransaction(BluetoothAdapter bluetoothAdapter, String bluetoothAddress, String tx) {
		if (bluetoothAddress == null)
			return;
		
		String expandedBtAddr = expandAddress(bluetoothAddress);
		if (expandedBtAddr == null)
			return;
		
		byte[] txBytes = hexStringToByteArray(tx);
		if (txBytes == null)
			return;
		
		BluetoothSocket socket = null;
		DataOutputStream out = null;
		DataInputStream in = null;
		
		try {
			BluetoothDevice device = bluetoothAdapter.getRemoteDevice(expandedBtAddr);
			socket = device.createInsecureRfcommSocketToServiceRecord(BITCOIN_BLUETOOTH_UUID);
			
			socket.connect();
			in = new DataInputStream(socket.getInputStream());                                                   
			out = new DataOutputStream(socket.getOutputStream());
			
			out.writeInt(1);                                                                                      
			out.writeInt(txBytes.length);                                                                    
			out.write(txBytes);                                                                              
			out.flush();      
			
			in.readBoolean();	/* read ack, but ignore for now */
		} catch (IOException x) {
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