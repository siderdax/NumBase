package kyi.numbase.plays;

import kyi.numbase.gears.ActivityController;
import kyi.numbase.gears.Game_Thread;
import kyi.numbase.gears.Game_core;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Tutorial_Thread extends Game_Thread{

	private int time_limit;
	private Handler handler; 
	private TutorialActivity tact;

	public Tutorial_Thread(TutorialActivity tact, Handler handler, 
			int ball_limit, int dead_line, int time_limit){

		this.tact = tact;
		this.handler = handler;		
		this.time_limit = time_limit;
		
		Game_core.getInstance().setRules(ball_limit, dead_line);
	}

	@Override
	protected void ThreadLoop() {
		// TODO Auto-generated method stub
		super.ThreadLoop();

		// Time Stopper
		if(tact.getActivityState() != ActivityController.GO_STATE 
				&& tact.getActivityState() != ActivityController.RESUME_STATE){
			resetTime();
		}

		// Time limit Checker
		if(getTime() >= time_limit * 1000){
			Log.i("KYI", "time_out");
			Game_core.getInstance().time_out();
			resetTime();
		}

		/////// Play Area ///////
		switch(tact.getActivityState()){
		case ActivityController.READY_STATE:
			resetGlobalTime();
			Game_core.getInstance().setMissionN();
			sleep_ms(1000);
			handler.sendEmptyMessage(ActivityController.SET_STATE);
			Log.i("KYI", "State : READY");
			break;
		case ActivityController.SET_STATE:
			handler.sendEmptyMessage(ActivityController.NONE_STATE);
			Log.i("KYI", "State : SET");
			break;
		case ActivityController.GO_STATE:
			resetGlobalTime();
			handler.sendEmptyMessage(ActivityController.RESUME_STATE);
			Log.i("KYI", "State : GO");
			break;
		case ActivityController.RESUME_STATE:
			if(Game_core.getInstance().getState() != Game_core.ING){
				//Record message
				Message msg = new Message();
				msg.obj = Integer.toString(getGlobalTime());
				tact.recordHandler.sendMessage(msg);
				
				handler.sendEmptyMessage(ActivityController.END_STATE);
			}
			//Log.i("KYI", "State : RESUME, ");
			break;
		case ActivityController.END_STATE:
			//Log.i("KYI", "State : END");
			break;
		}

		// arg1 = Limit counter, arg2 = Elapsed time, what = TIME
		handler.sendMessage(mTimeMessage(time_limit));

		// Loop Interval
		sleep_ms(100);
	}
}
