/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kyi.numbase.gears;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import kyi.numbase.managers.DataManager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Bluesvc {
	private static final String NBName = "numbasebluetooth";

	// Unique UUID for this application
	private static final UUID MY_UUID =
			UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

	// Member fields
	private Handler cHandler, dHandler;
	private final BluetoothAdapter mAdapter;
	private AcceptThread mSecureAcceptThread;
	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;
	private int mState;

	// Constants that indicate the current connection state
	public static final int STATE_NONE = 0;       // we're doing nothing
	public static final int STATE_LISTEN = 1;     // now listening for incoming connections
	public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
	public static final int STATE_CONNECTED = 3;  // now connected to a remote device
	public static final int STATE_DISCONNECTED = 4;  // now connected to a remote device

	public Bluesvc(Handler cchk_handler) {
		cHandler = cchk_handler;
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		mState = STATE_NONE;
	}
	
	public void setDataHandler(Handler dat_handler){
		dHandler = dat_handler;
	}

	public synchronized void start() {
	
		// Cancel any thread attempting to make a connection
		if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
	
		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
	
		setState(STATE_LISTEN);
	
		// Start the thread to listen on a BluetoothServerSocket
		if (mSecureAcceptThread == null) {
			mSecureAcceptThread = new AcceptThread();
			mSecureAcceptThread.start();
		}
	}

	public synchronized void stop() {
	
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}
	
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
	
		if (mSecureAcceptThread != null) {
			mSecureAcceptThread.cancel();
			mSecureAcceptThread = null;
		}
	
		setState(STATE_NONE);
	}

	private synchronized void setState(int state) {
		mState = state;
		Log.i("KYI", "State : " + state);
	}

	public synchronized int getState() {
		return mState;
	}

	public synchronized void connect(BluetoothDevice device) {

		// Cancel any thread attempting to make a connection
		if (mState == STATE_CONNECTING) {
			if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

		// Start the thread to connect with the given device
		mConnectThread = new ConnectThread(device);
		mConnectThread.start();
		setState(STATE_CONNECTING);
	}

	public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {

		// Cancel the thread that completed the connection
		if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

		// Cancel the accept thread because we only want to connect to one device
		if (mSecureAcceptThread != null) {
			mSecureAcceptThread.cancel();
			mSecureAcceptThread = null;
		}

		// Start the thread to manage the connection and perform transmissions
		mConnectedThread = new ConnectedThread(socket);
		mConnectedThread.start();

		// Send the name of the connected device back to the UI Activity
		DataManager.getInstance().setTargetDeviceName(device.getName());

		setState(STATE_CONNECTED);
		cHandler.sendEmptyMessage(STATE_CONNECTED);
	}

	private class AcceptThread extends Thread {
		// The local server socket
		private final BluetoothServerSocket mmServerSocket;
	
		public AcceptThread() {
			BluetoothServerSocket tmp = null;
	
			// Create a new listening server socket
			try {
				tmp = mAdapter.listenUsingRfcommWithServiceRecord(
						NBName, MY_UUID);
	
			} catch (IOException e) {
				Log.e("KYI", "Socket Type: listen() failed", e);
			}
			mmServerSocket = tmp;
		}
	
		public void run() {
			setName("AcceptThread NumbaseBluetooth");
	
			BluetoothSocket socket = null;
	
			// Listen to the server socket if we're not connected
			while (mState != STATE_CONNECTED) {
				try {
					// This is a blocking call and will only return on a
					// successful connection or an exception
					socket = mmServerSocket.accept();
				} catch (IOException e) {
					Log.e("KYI", "Socket Type: accept() failed", e);
					break;
				}
	
				// If a connection was accepted
				if (socket != null) {
					synchronized (Bluesvc.this) {
						switch (mState) {
						case STATE_LISTEN:
						case STATE_CONNECTING:
							// Situation normal. Start the connected thread.
							connected(socket, socket.getRemoteDevice());
							break;
						case STATE_NONE:
						case STATE_CONNECTED:
							// Either not ready or already connected. Terminate new socket.
							try {
								socket.close();
							} catch (IOException e) {
								Log.e("KYI", "Could not close unwanted socket", e);
							}
							break;
						}
					}
				}
			}
		}
	
		public void cancel() {
			try {
				mmServerSocket.close();
			} catch (IOException e) {
				Log.e("KYI", "Socket Type close() of server failed", e);
			}
		}
	}

	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;
	
		public ConnectThread(BluetoothDevice device) {
			mmDevice = device;
			BluetoothSocket tmp = null;
	
			// Get a BluetoothSocket for a connection with the
			// given BluetoothDevice
			try {
				tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) {
				Log.e("KYI", "Socket failed", e);
			}
			mmSocket = tmp;
		}
	
		public void run() {
			setName("ConnectThread numbasebluetooth");
	
			// Always cancel discovery because it will slow down a connection
			mAdapter.cancelDiscovery();
	
			// Make a connection to the BluetoothSocket
			try {
				// This is a blocking call and will only return on a
				// successful connection or an exception
				mmSocket.connect();
			} catch (IOException e) {
				// Close the socket
				try {
					mmSocket.close();
				} catch (IOException e2) {
					Log.e("KYI", "unable to close() ", e2);
				}
				Log.e("KYI", "Connection Failed");
				Bluesvc.this.start();
				return;
			}
	
			// Reset the ConnectThread because we're done
			synchronized (Bluesvc.this) {
				mConnectThread = null;
			}
	
			// Start the connected thread
			connected(mmSocket, mmDevice);
		}
	
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e("KYI", "close() of connect failed", e);
			}
		}
	}

	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;
	
		public ConnectedThread(BluetoothSocket socket) {
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;
	
			// Get the BluetoothSocket input and output streams
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				Log.e("KYI", "temp sockets not created", e);
			}
	
			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}
	
		////////////// Read loop ///////////////
		public void run() {
			byte[] buffer = new byte[1024];
			int bytes;
	
			// Keep listening to the InputStream while connected
			while (true) {
				try {
					// Read from the InputStream
					bytes = mmInStream.read(buffer);
	
					// Send the obtained bytes to the UI Activity
					Message msg = new Message();
					msg.obj = buffer;
					msg.arg1 = bytes;
					if(dHandler != null) dHandler.sendMessage(msg);
				} catch (IOException e) {
					Log.e("KYI", "Disconnected, connection lost.", e);
					cHandler.sendEmptyMessage(STATE_DISCONNECTED);
					break;
				}
			}
		}
		//////////// Read loop end ///////////////
	
		public void write(byte[] buffer) {
			try {
				mmOutStream.write(buffer);
			} catch (IOException e) {
				Log.e("KYI", "Exception during write", e);
			}
		}
	
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e("KYI", "close() of connect socket failed", e);
			}
		}
	}

	public void write(String str) {
		// Create temporary object
		ConnectedThread r;
		// Synchronize a copy of the ConnectedThread
		synchronized (this) {
			if (mState != STATE_CONNECTED) return;
			r = mConnectedThread;
		}
		// Perform the write unsynchronized
		r.write(str.getBytes());
	}
}
