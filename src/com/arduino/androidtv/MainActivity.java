package com.arduino.androidtv;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final String TAG = "bluetooth";

	private BluetoothAdapter btAdapter = null;
	private BluetoothSocket btSocket = null;
	private OutputStream outStream = null;

	// SPP UUID service
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-0000-8000-00805F9B34FB");

	// ImageButtons
	private ImageButton btnPower, btnV_up, btnV_down, btnC_up, btnC_down;

	// MAC-address of Bluetooth module (you must edit this line)
	private static String address;;
	private static String deviceName;
	private Intent i;
	private TextView tvBTstate;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		btnPower = (ImageButton) findViewById(R.id.btnPower);
		btnV_up = (ImageButton) findViewById(R.id.btnV_up);
		btnV_down = (ImageButton) findViewById(R.id.btnV_down);
		btnC_up = (ImageButton) findViewById(R.id.btnC_up);
		btnC_down = (ImageButton) findViewById(R.id.btnC_down);

		btAdapter = BluetoothAdapter.getDefaultAdapter();
		i = getIntent();

		tvBTstate = (TextView) findViewById(R.id.conTxt);
		// et.setText(i.getStringExtra("device_name"));
		deviceName = i.getStringExtra("device_name");
		checkBTState();

		// POWER ON >> 0
		btnPower.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				sendData("0");
			}
		});

		// VOL UP >> 1
		btnV_up.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				sendData("1");
			}
		});

		// VOL DOWN >> 2
		btnV_down.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				sendData("2");
			}
		});

		// CHANNEL UP >> 3
		btnC_up.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				sendData("3");
			}
		});

		// CHANNEL DOWN >> 4
		btnC_down.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				sendData("4");
			}
		});

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			android.os.Process.killProcess(android.os.Process.myPid());
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private BluetoothSocket createBluetoothSocket(BluetoothDevice device)
			throws IOException {
		if (Build.VERSION.SDK_INT >= 10) {
			try {
				final Method m = device.getClass().getMethod(
						"createInsecureRfcommSocketToServiceRecord",
						new Class[] { UUID.class });
				return (BluetoothSocket) m.invoke(device, MY_UUID);
			} catch (Exception e) {
				Log.e(TAG, "Could not create Insecure RFComm Connection", e);
			}
		}
		return device.createRfcommSocketToServiceRecord(MY_UUID);
	}

	@Override
	public void onResume() {
		super.onResume();

		Log.d(TAG, "...onResume - try connect...");

		// Set up a pointer to the remote node using it's address.
		Log.d(TAG, "BT DEVICE");
		BluetoothDevice device = null;
		if (address != null) {
			device = btAdapter.getRemoteDevice(address);
		} else {
			tvBTstate.setText(R.string.not_connected);
		}

		Log.d(TAG, "BT DEVICE END");

		// Two things are needed to make a connection:
		// A MAC address, which we got above.
		// A Service ID or UUID. In this case we are using the
		// UUID for SPP.

		Log.d(TAG, "BT SOCKET");

		if (device != null) {
			try {
				btSocket = createBluetoothSocket(device);
			} catch (IOException e1) {
				errorExit(
						"Fatal Error",
						"In onResume() and socket create failed: "
								+ e1.getMessage() + ".");
			}
		} else {
			//finish();
		}
		Log.d(TAG, "BT SOCKET END");

		/*
		 * try { btSocket = device.createRfcommSocketToServiceRecord(MY_UUID); }
		 * catch (IOException e) { errorExit("Fatal Error",
		 * "In onResume() and socket create failed: " + e.getMessage() + "."); }
		 */

		// Discovery is resource intensive. Make sure it isn't going on
		// when you attempt to connect and pass your message.

		Log.d(TAG, "BT ADAPTER");
		if (btAdapter != null) {
			
			btAdapter.cancelDiscovery();

			if (btSocket != null) {
				// Establish the connection. This will block until it connects.
				Log.d(TAG, "...Connecting...");
				try {
					btSocket.connect();
					Log.d(TAG, "...Connection ok...");
					tvBTstate.setText(R.string.connected);
				} catch (IOException e) {
					try {
						btSocket.close();
					} catch (IOException e2) {
						errorExit("Fatal Error",
								"In onResume() and unable to close socket during connection failure"
										+ e2.getMessage() + ".");
					}
				}

				// Create a data stream so we can talk to server.
				Log.d(TAG, "...Create Socket...");

				try {
					outStream = btSocket.getOutputStream();
				} catch (IOException e) {
					errorExit("Fatal Error",
							"In onResume() and output stream creation failed:"
									+ e.getMessage() + ".");
				}
			} 
		}
		Log.d(TAG, "BT ADAPTER END");
	}

	@Override
	public void onPause() {
		super.onPause();

		Log.d(TAG, "...In onPause()...");

		if (outStream != null) {
			try {
				outStream.flush();
			} catch (IOException e) {
				errorExit(
						"Fatal Error",
						"In onPause() and failed to flush output stream: "
								+ e.getMessage() + ".");
			}
			
			
		}
		
		if(btSocket != null){
			try {
				btSocket.close();
			} catch (IOException e2) {
				errorExit("Fatal Error", "In onPause() and failed to close socket."
						+ e2.getMessage() + ".");
			}
		}

		
	}

	private void checkBTState() {
		// Check for Bluetooth support and then check to make sure it is turned
		// on
		// Emulator doesn't support Bluetooth and will return null
		if (btAdapter == null) {
			errorExit("Fatal Error", "Bluetooth not support");
		} else {
			if (btAdapter.isEnabled()) {
				Log.d(TAG, "...Bluetooth ON...");

				Set<BluetoothDevice> pairedDevices = btAdapter
						.getBondedDevices();
				Iterator<BluetoothDevice> i = pairedDevices.iterator();

				while (i.hasNext()) {
					BluetoothDevice bd = i.next();
					try {
						if (bd.getName().equalsIgnoreCase(deviceName)) {
							address = bd.getAddress().toString();
						} else {
							// finish();
						}
					} catch (Exception e) {
						// errorExit("Fatal Error",
						// "Invalied bluetooth device name");
					}
				}

			} else {
				// Prompt user to turn on Bluetooth
				Intent enableBtIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, 1);
			}
		}
	}

	private void errorExit(String title, String message) {
		Toast.makeText(getBaseContext(), title + " - " + message,
				Toast.LENGTH_LONG).show();
		finish();
	}

	private void sendData(String message) {
		
		byte[] msgBuffer = message.getBytes();
		byte[] msgBuffer2 = "\n".getBytes();
		int i = Integer.parseInt(message);

		Log.d(TAG, "...Send data: " + message + "...");

		if (outStream != null) {
			try {
				outStream.write(msgBuffer);
				outStream.write(msgBuffer2);
			} catch (IOException e) {
				String msg = "In onResume() and an exception occurred during write: "
						+ e.getMessage();
				if (address.equals("00:00:00:00:00:00"))
					msg = msg
							+ ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 35 in the java code";
				msg = msg + ".\n\nCheck that the SPP UUID: "
						+ MY_UUID.toString() + " exists on server.\n\n";

				errorExit("Fatal Error",
						"Cannot connect to the bluetooth shield");
			}
		} else {
			Toast.makeText(getBaseContext(),
					"Please enter correct bluetooth shield name",
					Toast.LENGTH_LONG).show();

			// finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.

		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.exit:
			if (outStream != null)
				exitApp();
			else
				exitAll();
			break;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);

	}

	void exitApp() {
		// Intent intent = new Intent(Intent.ACTION_MAIN);
		// intent.addCategory(Intent.CATEGORY_HOME);
		// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// startActivity(intent);
		finish();
	}

	@Override
	protected void onDestroy() {
		android.os.Process.killProcess(android.os.Process.myPid());
		super.onDestroy();
	}

	void exitAll() {
		Intent intent = new Intent(this, StartPage.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("Exit me", true);
		startActivity(intent);
		android.os.Process.killProcess(android.os.Process.myPid());
	}
	
	
    

}
