package kyi.numbase.gears;

import android.util.Log;

public class Game_core {
	public static final int LOSE = 0;
	public static final int WIN = 1;
	public static final int ING = 2;
	public static final int DONKNOW = 2;

	private static Game_core g_core = new Game_core();
	private static int S, B;
	private int BALL_LIMIT;
	private int DEAD_LINE; 
	private int mission_num;
	private int[] parsed_mn, parsed_mission_n;
	private int try_cnt; 
	private int state;

	public Game_core(){
	}
	
	public static Game_core getInstance(){
		return g_core;
	}
	
	public void setRules(int ball_limit, int dead_line){
		BALL_LIMIT = ball_limit;
		DEAD_LINE = dead_line;
		
		parsed_mn = new int[BALL_LIMIT];
		parsed_mission_n = new int[BALL_LIMIT];
		reset_data();
	}

	public void reset_data(){
		for(int i = 0; i < BALL_LIMIT; i++){
			parsed_mn[i] = 0;
			parsed_mission_n[i] = 0;
		}
		S = 0;
		B = 0;
		try_cnt = 0;
		state = DONKNOW;
	}

	public void setMissionN(){
		int rn;
		int[] n;
		n = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
		mission_num = 0;
		state = DONKNOW;
		for(int i =0; i < BALL_LIMIT; i++){
			rn = (int)(Math.random() * (9 - i));
			parsed_mission_n[i] =  n[rn];
			n[rn] = n[8 - i];
			mission_num += parsed_mission_n[i] * (int)(Math.pow(10, BALL_LIMIT-i-1));
		}
		Log.i("KYI", Integer.toString(mission_num));
	}
	
	public String getBallnum(){
		return Integer.toString(mission_num);
	}

	public int pitching(int tried_num){
		parse_num(tried_num);
		S = 0;
		B = 0;
		
		for(int m = 0; m < BALL_LIMIT; m++)
			for(int n = 0; n < BALL_LIMIT; n++){
				// m -> Object Number, n -> Tried Number
				if(parsed_mn[n] == parsed_mission_n[m]){
					if(m == n){
						S++;
						//break;
					}
					else{
						B++;
						//break;
					}
				}
			}
		try_cnt++;
		
		return S*10 + B;
	}
	
	public void setState(int state){
		this.state = state;
	}
	
	public int getState(){
		if(state != DONKNOW)
			return state;
		
		
		if(S >= BALL_LIMIT)
			return WIN;
		else if(try_cnt > DEAD_LINE)
			return LOSE;
		else
			return ING;
	}
	
	public int getTrCnt(){
		return try_cnt;
	}

	public void time_out(){
		try_cnt++;
	}

	private void parse_num(int num){
		int buf = num;
		int n;

		for(n = 0; n < BALL_LIMIT - 1; n++){
			parsed_mn[n] = buf / (int)(Math.pow(10, parsed_mn.length-n-1));
			buf = buf % (int)(Math.pow(10, parsed_mn.length-n-1));
		}
		parsed_mn[n] = buf;
	}
}
