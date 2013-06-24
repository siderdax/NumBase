package kyi.numbase.gears;

import android.os.Message;

public class Game_Thread extends Thread {
	protected long global_t, start_t, current_t;
	private boolean loop_flag;

	protected void sleep_ms(int n){
		try {
			sleep(n);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void start() {
		// TODO Auto-generated method stub
		loop_flag = true;
		global_t = System.currentTimeMillis();

		super.start();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(loop_flag){
			ThreadLoop();
		}
		super.run();
	}

	protected void ThreadLoop(){
	}

	protected Message mTimeMessage(int time_limit){
		Message msg = new Message();

		// what : Call State
		msg.what = ActivityController.TIME;
		// arg1 : Time_limit Seconds
		msg.arg1 = time_limit*1000 - getTime() - 1;
		// arg2 : Elapsed Seconds
		msg.arg2 = getGlobalTime();

		return msg;
	}

	public int getTime(){
		current_t = System.currentTimeMillis();
		return (int)(current_t - start_t);
	}

	public int getGlobalTime(){
		return (int)(current_t - global_t);
	}
	
	public void resetGlobalTime(){
		global_t = System.currentTimeMillis();
		start_t = System.currentTimeMillis();
	}

	public void resetTime(){
		start_t = System.currentTimeMillis();
	}

	public void readyThrd(){
		loop_flag = true;
	}

	public void stopThrd(){
		loop_flag = false;
	}
}
