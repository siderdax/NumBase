package kyi.numbase.gears;

import kyi.numbase.R;
import kyi.numbase.managers.DataManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

@SuppressLint({ "HandlerLeak", "DefaultLocale" })
public class ActivityController extends Activity {
	public static final int NONE_STATE = 0;
	public static final int READY_STATE = 1;
	public static final int SET_STATE = 2;
	public static final int GO_STATE = 3;
	public static final int GO_LATE_STATE = 4;
	public static final int PAUSE_STATE = 5;
	public static final int RESUME_STATE = 6;
	public static final int END_STATE = 7;
	
	public static final int TIME = 8;

	protected TextView ballBoard, topBoard, cover_txt, pitchtxt0;
	protected TextView sr1, sr2, sr3, br1, br2, br3, clv;
	protected ListView g_list;
	protected LinearLayout cover_ll;
	protected ProgressBar time_prog;

	protected int[] ballNum;
	protected int current_state; 
	protected String ballStr;

	protected ArrayAdapter<String> adapter;
	
	private int ball_cnt = 3;
	private int time_limit = 20;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.gameui);
		
		// Cover Views
		cover_ll = (LinearLayout)findViewById(R.id.cover);
		cover_txt = (TextView)findViewById(R.id.cover_txt);

		// ListView Settings
		g_list = (ListView)findViewById(R.id.histo_list);
		adapter = new ArrayAdapter<String>(this, R.layout.view_items,
				R.id.game_listview_text);
		g_list.setAdapter(adapter);

		// Other Views
		time_prog = (ProgressBar)findViewById(R.id.progressBar);
		ballBoard = (TextView)findViewById(R.id.pitchtxt0);
		topBoard = (TextView)findViewById(R.id.histotxt);
		clv = (TextView)findViewById(R.id.clickview);
		
		sr1 = (TextView)findViewById(R.id.strike_1);
		sr2 = (TextView)findViewById(R.id.strike_2);
		sr3 = (TextView)findViewById(R.id.strike_3);
		br1 = (TextView)findViewById(R.id.ball_1);
		br2 = (TextView)findViewById(R.id.ball_2);
		br3 = (TextView)findViewById(R.id.ball_3);
		
		current_state = NONE_STATE;
	}
	
	// Set Ball number interface
	protected void setBallIF(int ball_cnt){
		this.ball_cnt = ball_cnt;
	}
	
	// Set Time limit number interface
	protected void setTimeIF(int time_limit){
		this.time_limit = time_limit;
	}

	// Setting Activity State.
	public void setActivityState(int state){
		current_state = state;

		switch (state) {
		case NONE_STATE:
			break;
		case READY_STATE:
			cover_ll.setVisibility(View.VISIBLE);
			cover_txt.setText("READY");
			break;
		case SET_STATE:
			cover_ll.setVisibility(View.VISIBLE);
			cover_txt.setText("Touch to Start!");
			cover_txt.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					DataManager.getInstance().setSound(ActivityController.this, 2);
					DataManager.getInstance().getVibOpt(ActivityController.this, 700);
					setActivityState(GO_STATE);
				}
			});
			break;
		case GO_STATE:
			cover_txt.setOnClickListener(null);
			cover_ll.setVisibility(View.GONE);
			// Starting Core
			ballNum = new int[ball_cnt];
			resetNum();
			break;
		case GO_LATE_STATE:
			cover_txt.setOnClickListener(null);
			cover_ll.setVisibility(View.VISIBLE);
			cover_txt.setText("WAITING..");
			// Starting Core
			ballNum = new int[ball_cnt];
			resetNum();
			break;
		case PAUSE_STATE:
			cover_ll.setVisibility(View.VISIBLE);
			cover_txt.setText("WAITING..");
			break;
		case RESUME_STATE:
			DataManager.getInstance().setSound(ActivityController.this, 6);
			cover_ll.setVisibility(View.GONE);
			break;
		case END_STATE:
			adapter.clear();
			cover_ll.setVisibility(View.VISIBLE);
			if(Game_core.getInstance().getState() == Game_core.WIN){
				DataManager.getInstance().setSound(ActivityController.this, 4);
				DataManager.getInstance().getVibOpt(ActivityController.this, 1000);
				cover_txt.setText("YOU WIN!!");
			}
			else if(Game_core.getInstance().getState() == Game_core.LOSE){
				DataManager.getInstance().setSound(ActivityController.this, 5);
				DataManager.getInstance().getVibOpt(ActivityController.this, 1000);
				cover_txt.setText("YOU LOSE!!");
			}

			Game_core.getInstance().reset_data();

			cover_txt.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					DataManager.getInstance().getVibOpt(ActivityController.this, 700);
					setActivityState(READY_STATE);
				}
			});
			break;
		default:
			break;
		}
	};

	public int getActivityState(){
		return current_state;
	}

	protected void resetNum(){
		for(int i = 0; i < ballNum.length; i++){
			ballNum[i] = 0;
		}
	}

	public void gButtonClick(View v){
		// TODO Auto-generated method stub
		CharSequence str = ((Button)v).getText();

		if(current_state != RESUME_STATE) return;
		
		if(v.getId() == R.id.pitchtxt0){
			if(str.charAt(str.length() - 1) != '-'){
				int cnt = 0;
				for(int i = 0; i < str.length(); i++){
					for(int j = i + 1; j < str.length(); j++){
						if(str.charAt(i) == str.charAt(j)){
							cnt++;
							break;
						}
					}
					if(cnt > 0) break;
				}

				if(cnt == 0){
					DataManager.getInstance().setSound(ActivityController.this, 3);
					DataManager.getInstance().getVibOpt(ActivityController.this, 200);
					
					int sb = Game_core.getInstance().pitching(Integer.parseInt((String) str));
					adapter.add("Number : " + str + " / S: " + sb/10 + "/ B: " + sb%10);

					sr3.setVisibility(View.INVISIBLE);
					sr2.setVisibility(View.INVISIBLE);
					sr1.setVisibility(View.INVISIBLE);
					br3.setVisibility(View.INVISIBLE);
					br2.setVisibility(View.INVISIBLE);
					br1.setVisibility(View.INVISIBLE);

					switch(sb/10){
					case 6 :
					case 5 :
					case 4 :
					case 3 :
						sr3.setVisibility(View.VISIBLE);
					case 2 :
						sr2.setVisibility(View.VISIBLE);
					case 1 :
						sr1.setVisibility(View.VISIBLE);
					}

					switch(sb%10){
					case 6 :
					case 5 :
					case 4 :
					case 3 :
						br3.setVisibility(View.VISIBLE);
					case 2 :
						br2.setVisibility(View.VISIBLE);
					case 1 :
						br1.setVisibility(View.VISIBLE);
					}
					
					clv.setText("Write number!");
					
					pitchButtonAction(v);
				}
				else{
					DataManager.getInstance().setSound(ActivityController.this, 6);
				}
			}
			resetNum();
		}
		else{
			DataManager.getInstance().setSound(ActivityController.this, 1);
			addNum(str.charAt(str.length() - 1) - 48);
			if(ballNum[ballNum.length-1] != 0) clv.setText("↓ Click!! ↓");
		}
		
		getBallStr();
		ballBoard.setText(ballStr);
	}
	
	protected int addNum(int n){
		if(n < 1 && n > 9)
			return -1;
		else{
			int i;
			for(i = 0; i < ballNum.length; i++){
				if(ballNum[i] == 0){
					ballNum[i] = n;
					return i;
				}
			}
			return i;
		}
	}

	public void pitchButtonAction(View v){
		// Use override
	}

	public void getBallStr(){
		ballStr = "";

		for(int i : ballNum){
			if(i != 0)
				ballStr = ballStr + Integer.toString(i);
			else
				ballStr = ballStr + "-";
		}
	}
	
	protected Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what){
			case NONE_STATE:
				setActivityState(ActivityController.NONE_STATE);
				break;
			case READY_STATE:
				setActivityState(ActivityController.READY_STATE);
				break;
			case SET_STATE:
				setActivityState(ActivityController.SET_STATE);
				break;
			case GO_STATE:
				setActivityState(ActivityController.GO_STATE);
				break;
			case GO_LATE_STATE:
				setActivityState(ActivityController.GO_LATE_STATE);
				break;
			case RESUME_STATE:
				setActivityState(ActivityController.RESUME_STATE);
				break;
			case END_STATE:
				setActivityState(ActivityController.END_STATE);
				break;
			
			// Time writer
			case TIME:
				int progressInt = (msg.arg1 * 100) / (time_limit*1000);
				String global_t = String.format("%.2f", msg.arg2 / 1000.0);
				time_prog.setProgress(progressInt);
				topBoard.setText("전광판 - " + Game_core.getInstance().getTrCnt() + "회, 경기 시간 : "
						+ global_t + "초");

				break;
			}

			super.handleMessage(msg);
		}
	};
}
