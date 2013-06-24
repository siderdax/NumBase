package kyi.numbase.gears;

import kyi.numbase.R;
import kyi.numbase.managers.DataManager;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class BlueActivity extends Activity {
	private BluetoothAdapter mBluetoothAdapter = null;

	// Member fields
	private ArrayAdapter<String> mNewDevicesArrayAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_blue);

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		
		//ListView Setting
		mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_item);
		mNewDevicesArrayAdapter.add("Select Menu.");
		ListView DeviceList = (ListView)findViewById(R.id.conn_list);
		DeviceList.setAdapter(mNewDevicesArrayAdapter);
		DeviceList.setOnItemClickListener(mDeviceClickListener);
		
		
		// Button Setting
		// Discover
		Button discover_btn = (Button)findViewById(R.id.find_device);
		discover_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mNewDevicesArrayAdapter.clear();
				doDiscovery();
			}
		});
		// Ensure
		Button notice_btn = (Button)findViewById(R.id.notify_device);
		notice_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ensureDiscoverable();
				finish();
			}
		});
		
		
		// Register Receiver
		// Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);
        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }
        this.unregisterReceiver(mReceiver);
        
		super.onDestroy();
	}

	// The on-click listener for all devices in the ListViews
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            mBluetoothAdapter.cancelDiscovery();
            
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            DataManager.getInstance().setTargetDeviceAddress(address);
            finish();
        }
    };
	
	private void doDiscovery() {
        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);
        setTitle("검색중");

        // If we're already discovering, stop it
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBluetoothAdapter.startDiscovery();
    }
	
	private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 60);
            startActivity(discoverableIntent);
        }
    }
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// add list

				mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
				// When discovery is finished, change the Activity title
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				setProgressBarIndeterminateVisibility(false);
				setTitle("디바이스 선택");
				if (mNewDevicesArrayAdapter.getCount() == 0) {
					String noDevices = "Any Devices not found.";
					mNewDevicesArrayAdapter.add(noDevices);
				}
			}
		}
	};
}
