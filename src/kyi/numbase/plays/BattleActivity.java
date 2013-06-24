package kyi.numbase.plays;

import kyi.numbase.R;
import kyi.numbase.gears.ActivityController;
import kyi.numbase.gears.BlueActivity;
import kyi.numbase.gears.Bluesvc;
import kyi.numbase.gears.Game_core;
import kyi.numbase.managers.DBmanager;
import kyi.numbase.managers.DataManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class BattleActivity extends ActivityController {
	private static final int BALL_LIMIT = 3;
	private static final int ROUND = 9;
	private static final int TIME_LIMIT = 20;
	
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "tstmsg";

	public static final int CONNECT_BLUE = 10;
	public static final int ON_BLUE = 11;

	private BluetoothAdapter mBluetoothAdapter;
	private Bluesvc bluesvc;
	private Battle_Thread b_thrd;
	
	DBmanager dbm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// Initializing
		dbm = new DBmanager(this, "Baseball.db", null, 1);
		b_thrd = new Battle_Thread(this, mHandler, BALL_LIMIT, ROUND, TIME_LIMIT);
		bluesvc = new Bluesvc(connChkHandler);
		bluesvc.setDataHandler(b_thrd.blueHandler);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		// Play BGM
		if(DataManager.getInstance().getBGMTracknum() != DataManager.BLACK_DADA)
			DataManager.getInstance().setBGM(this, DataManager.BLACK_DADA);
		DataManager.getInstance().startBGM(this);
		
		super.onResume();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub

		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, ON_BLUE);
		}
		else{
			if (bluesvc.getState() == Bluesvc.STATE_NONE) bluesvc.start();
			startActivityForResult(
					new Intent(BattleActivity.this, BlueActivity.class), CONNECT_BLUE);
		}

		super.onStart();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if(b_thrd != null) b_thrd.stopThrd();
		if(bluesvc != null) bluesvc.stop();
		Toast.makeText(this, "Connection destroyed.", Toast.LENGTH_SHORT).show();

		dbm.close();
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if(requestCode == ON_BLUE){
			if(resultCode == Activity.RESULT_OK){
				if (bluesvc.getState() == Bluesvc.STATE_NONE) bluesvc.start();
				startActivityForResult(
						new Intent(BattleActivity.this, BlueActivity.class), CONNECT_BLUE);
			}
			else
				finish();
		}

		else if (requestCode == CONNECT_BLUE) connectDevice();

		super.onActivityResult(requestCode, resultCode, data);
	}

	private void connectDevice() {
		// Get the device MAC address
		String address = DataManager.getInstance().getTargetDeviceAddress();
		if (address != null) {
			// Get the BLuetoothDevice object
			BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
			// Attempt to connect to the device
			bluesvc.connect(device);
		}
	}

	private final Handler connChkHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Bluesvc.STATE_CONNECTED:
				//Toast.makeText(BattleActivity.this, "Connected", Toast.LENGTH_SHORT).show();
				if(DataManager.getInstance().getBGMTracknum() != DataManager.NIGHT_SHIFT)
					DataManager.getInstance().setBGM(BattleActivity.this, DataManager.NIGHT_SHIFT);
				DataManager.getInstance().startBGM(BattleActivity.this);
				
				b_thrd.setBlue(bluesvc);
				b_thrd.start();

				setActivityState(READY_STATE);
				break;
			case Bluesvc.STATE_DISCONNECTED:
				onDestroy();
				// for end
				finish();
				break;
			default:
				break;
			}

			super.handleMessage(msg);
		}
	};

	public Handler activity_control_handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(msg.what == Battle_Thread.TIME_OUT){
				Game_core.getInstance().time_out();
				bluesvc.write(Integer.toString(Battle_Thread.YOURTURN) + "Time out");
				if(Game_core.getInstance().getState() == Game_core.ING)
					setActivityState(PAUSE_STATE);
				else
					setActivityState(END_STATE);
			}
			else if(msg.what == Battle_Thread.ENEMNUMBER){
				int obj_length = msg.arg1;
				String data1 = new String((byte[]) msg.obj, 1, obj_length - BALL_LIMIT - 1);
				String data2 = new String((byte[]) msg.obj, obj_length - BALL_LIMIT, BALL_LIMIT);
				String str = "Enemy Number : " + data1 + "(Dst Num : " + data2 + ")";
				adapter.add(str);
			}

			super.handleMessage(msg);
		}

	};

	@Override
	public void pitchButtonAction(View v) {
		// TODO Auto-generated method stub
		String data = ((Button)v).getText().toString();

		if(v.getId() == R.id.pitchtxt0){
			if(Game_core.getInstance().getState() == Game_core.ING){
				bluesvc.write(Integer.toString(Battle_Thread.YOURTURN) + data 
						+ Game_core.getInstance().getBallnum());
				setActivityState(PAUSE_STATE);
			}
			else{
				if(Game_core.getInstance().getState() == Game_core.LOSE){
					dbm.ws_sql_ins(0, 1);
					bluesvc.write(Integer.toString(Battle_Thread.ENDGAME)+Integer.toString(Game_core.WIN));
				}
				else if(Game_core.getInstance().getState() == Game_core.WIN){
					dbm.ws_sql_ins(1, 0);
					bluesvc.write(Integer.toString(Battle_Thread.ENDGAME)+Integer.toString(Game_core.LOSE));
				}
				setActivityState(END_STATE);
			}
		}

		super.pitchButtonAction(v);
	}	
}
