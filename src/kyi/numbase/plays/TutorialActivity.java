package kyi.numbase.plays;

import kyi.numbase.gears.ActivityController;
import kyi.numbase.gears.Game_core;
import kyi.numbase.managers.DataManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

@SuppressLint("HandlerLeak")
public class TutorialActivity extends ActivityController {

	private Tutorial_Thread t_thrd;
	
	private SharedPreferences sharedPref;
	private Editor sharedEditor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActivityState(READY_STATE);
		
		int ball_limit = DataManager.getInstance().getSBallOpt(this);
		int time_limit = DataManager.getInstance().getSTimeOpt(this);
		int round_limit = DataManager.getInstance().getSRoundOpt(this);
		
		if(time_limit == 0) time_limit = 20;
		if(round_limit == 0) round_limit = 9;
		
		setBallIF(ball_limit); // Setting ball interface
		setTimeIF(time_limit); // Setting ball interface
		t_thrd = new Tutorial_Thread(TutorialActivity.this, mHandler, ball_limit,
				round_limit, time_limit);
		t_thrd.start();
		setActivityState(READY_STATE);
		
		//SharedPreferences
        sharedPref = getSharedPreferences("single", Activity.MODE_PRIVATE);
        sharedEditor = sharedPref.edit();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		// Play BGM
		if(DataManager.getInstance().getBGMTracknum() != DataManager.RIGHT_IN)
			DataManager.getInstance().setBGM(this, DataManager.RIGHT_IN);
		DataManager.getInstance().startBGM(this);
		
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if(t_thrd != null) t_thrd.stopThrd();
		super.onDestroy();
	}
	
	public Handler recordHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			int recent_record = Integer.parseInt(msg.obj.toString());
			int before_record = Integer.parseInt(sharedPref.getString("single_record", "9999000"));
			
			if(recent_record < before_record && Game_core.getInstance().getState() == Game_core.WIN){
				sharedEditor.putString("single_record", msg.obj.toString());
				sharedEditor.commit();
			}
						
			super.handleMessage(msg);
		}
		
	};
}