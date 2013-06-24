package kyi.numbase.plays;

import kyi.numbase.gears.ActivityController;
import kyi.numbase.gears.Bluesvc;
import kyi.numbase.gears.Game_Thread;
import kyi.numbase.gears.Game_core;
import kyi.numbase.managers.DBmanager;
import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class Battle_Thread extends Game_Thread {
	public static final int DICE = 0;
	public static final int URHIGHER = 1;
	public static final int URLOWER = 2;
	
	public static final int YOURTURN = 3;
	public static final int ENDGAME = 4;
	public static final int TIME_OUT = 3;
	public static final int ENEMNUMBER = 5;
	
	private static final int FIRST_TURN = 0;
	private static final int LAST_TURN = 1;
	private static final int RESET_TURN = 2;
	
	private Bluesvc bluesvc;
	private Handler handler;
	private int time_limit;
	private BattleActivity bact;
	
	private boolean turn_flag;
	private int dice;
	private static int first_turn;
	
	DBmanager dbm;

	public Battle_Thread(BattleActivity bact, Handler handler,
			int ball_limit, int dead_line, int time_limit){
		
		this.bact = bact;
		this.handler = handler;		
		this.time_limit = time_limit;
		dbm = new DBmanager(bact, "Baseball.db", null, 1);

		Game_core.getInstance().setRules(ball_limit, dead_line);
		
		///////// Game component ///////
		dice = 0;
		turn_flag = false;
		first_turn = RESET_TURN;
	}
	
	@Override
	public void stopThrd() {
		// TODO Auto-generated method stub
		dbm.close();
		super.stopThrd();
	}

	public void setBlue(Bluesvc bluesvc){
		this.bluesvc = bluesvc;
	}


	@Override
	protected void ThreadLoop() {
		// TODO Auto-generated method stub
		super.ThreadLoop();

		// Time Stopper
		if(bact.getActivityState() != ActivityController.GO_STATE 
				&& bact.getActivityState() != ActivityController.RESUME_STATE){
			resetTime();
		}
		// Time limit Checker
		if(getTime() >= time_limit * 1000){
			bact.activity_control_handler.sendEmptyMessage(TIME_OUT);
			Log.i("KYI", "time_out");
		}
		

		/////// Play Area ///////
		switch(bact.getActivityState()){
		case ActivityController.READY_STATE:
			Log.i("KYI", "State : READY");
			resetGlobalTime();
			Game_core.getInstance().setMissionN();
			placeTurn(); // set turn (Dice Game)
			break;
		case ActivityController.SET_STATE:
			Log.i("KYI", "State : SET");
			handler.sendEmptyMessage(ActivityController.NONE_STATE);
			break;
		case ActivityController.GO_STATE:
			Log.i("KYI", "State : GO");
			handler.sendEmptyMessage(ActivityController.RESUME_STATE);
			break;
		case ActivityController.GO_LATE_STATE:
			Log.i("KYI", "State : GO");
			handler.sendEmptyMessage(ActivityController.PAUSE_STATE);
			break;
		case ActivityController.PAUSE_STATE:
			//Log.i("KYI", "State : PAUSE");
			resetTime();
			if(turn_flag == true){
				turn_flag = false;
				handler.sendEmptyMessage(ActivityController.RESUME_STATE);
			}
			break;
		case ActivityController.RESUME_STATE:
			//Log.i("KYI", "State : RESUME, ");
			if(Game_core.getInstance().getState() != Game_core.ING){
				if(Game_core.getInstance().getState() == Game_core.LOSE){
					dbm.ws_sql_ins(0, 1);
					bluesvc.write(Integer.toString(ENDGAME)+Integer.toString(Game_core.WIN));
				}
				else if(Game_core.getInstance().getState() == Game_core.WIN){
					dbm.ws_sql_ins(1, 0);
					bluesvc.write(Integer.toString(ENDGAME)+Integer.toString(Game_core.LOSE));
				}
				handler.sendEmptyMessage(ActivityController.END_STATE);
			}
			break;
		case ActivityController.END_STATE:
			first_turn = LAST_TURN;
			resetGlobalTime();
			Game_core.getInstance().setMissionN();
			handler.sendEmptyMessage(ActivityController.NONE_STATE);
			//Log.i("KYI", "State : END");
			break;
		}

		// arg1 = Limit counter, arg2 = Elapsed time, what = TIME
		handler.sendMessage(mTimeMessage(time_limit));

		// Loop Interval
		sleep_ms(333);
	}
	
	private void placeTurn(){
		switch(first_turn){
		case FIRST_TURN :
			bluesvc.write(Integer.toString(URLOWER)+Integer.toString(dice));
			handler.sendEmptyMessage(ActivityController.SET_STATE);
			break;
		case LAST_TURN :
			bluesvc.write(Integer.toString(URHIGHER)+Integer.toString(dice));
			handler.sendEmptyMessage(ActivityController.GO_LATE_STATE);
			break;
		case RESET_TURN :
			dice = 1 + (int)(Math.random() * 6);
			Log.i("KYI", "Dice score : " + dice);
			if(bluesvc != null){
				bluesvc.write(Integer.toString(DICE) + Integer.toString(dice));
			}
			Log.i("KYI", "write : " + Integer.toString(DICE) + Integer.toString(dice));
			handler.sendEmptyMessage(ActivityController.NONE_STATE);
			break;
		}
	}


	public Handler blueHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			Log.d("KYI", "Message Received");
			byte[] buffer = (byte[])msg.obj;
			String data = new String(buffer, 1, msg.arg1 - 1);
			Log.d("KYI", "data : " + data);
			int header = buffer[0] - 48;
			Log.d("KYI", "header : " + header);
			
			switch(header){
			case DICE :
				if(dice < Integer.parseInt(data)) first_turn = LAST_TURN;
				else if(dice == Integer.parseInt(data)) first_turn = RESET_TURN;
				else first_turn = FIRST_TURN;
				Toast.makeText(bact, "Enemy Dice : " + data + ", My Dice : " +  dice
						, Toast.LENGTH_SHORT).show();
				handler.sendEmptyMessage(ActivityController.READY_STATE);
				break;
			case URHIGHER :
				handler.sendEmptyMessage(ActivityController.SET_STATE);
				break;
			case URLOWER :
				handler.sendEmptyMessage(ActivityController.GO_LATE_STATE);
				break;
				
			// play state
			case YOURTURN :
				handler.sendEmptyMessage(ActivityController.RESUME_STATE);
				Message act_msg = new Message();
				act_msg.what = ENEMNUMBER;
				act_msg.obj = msg.obj;
				act_msg.arg1 = msg.arg1;
				bact.activity_control_handler.sendMessage(act_msg);
				break;
			case ENDGAME :
				if(Integer.parseInt(data) == Game_core.WIN){
					dbm.ws_sql_ins(1, 0);
					Game_core.getInstance().setState(Game_core.WIN);
				}
				else{
					dbm.ws_sql_ins(0, 1);
					Game_core.getInstance().setState(Game_core.LOSE);
				}
				handler.sendEmptyMessage(ActivityController.END_STATE);
				break;
			}
			
			super.handleMessage(msg);
		}	
	};
	
}
