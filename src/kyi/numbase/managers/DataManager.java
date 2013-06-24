package kyi.numbase.managers;

import kyi.numbase.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.preference.PreferenceManager;

public class DataManager {
	public static final int MOTHER_BOARD = 0;
	public static final int RIGHT_IN = 1;
	public static final int BLACK_DADA = 2;
	public static final int NIGHT_SHIFT = 3;
	public static final int ATTACK_ON_TITAN = 4;
	public static final int NONE_TRACK = 5;
	
	private static DataManager _dm = new DataManager();
	private static String targetDeviceAddress;
	private static String targetDeviceName;
	private static MediaPlayer mp, sndp;
	private static int track_num;

	public DataManager(){
		track_num = NONE_TRACK;
	}

	public static DataManager getInstance(){
		return _dm;
	}

	public void setTargetDeviceAddress(String addr){
		targetDeviceAddress = addr;
	}

	public String getTargetDeviceAddress(){
		return targetDeviceAddress;
	}

	public void setTargetDeviceName(String name){
		targetDeviceName = name;
	}

	public String getTargetDeviceName(){
		return targetDeviceName;
	}

	public void getVibOpt(final Context context, final int delay){
		SharedPreferences sf = PreferenceManager.getDefaultSharedPreferences(context);
		if(sf.getBoolean("vib_opt", true))
			new Thread(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				((Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE))
				.vibrate(delay);
				super.run();
			}

		}.start();
	}
	
	public int getSBallOpt(final Context context){
		SharedPreferences sf = PreferenceManager.getDefaultSharedPreferences(context);
		return Integer.parseInt(sf.getString("ball_cnt", "3"));
	}
	
	public int getSTimeOpt(final Context context){
		SharedPreferences sf = PreferenceManager.getDefaultSharedPreferences(context);
		return Integer.parseInt(sf.getString("time_opt", "20"));
	}
	
	public int getSRoundOpt(final Context context){
		SharedPreferences sf = PreferenceManager.getDefaultSharedPreferences(context);
		return Integer.parseInt(sf.getString("round_opt", "9"));
	}

	public void setSRecord(int ms){
		
	}
	
	public void setBGM(Context context, int track){		
		track_num = track;
		
		if(mp != null)
			mp.release();
		
		switch(track){
		case MOTHER_BOARD :
			mp = MediaPlayer.create(context, R.raw.motherboard);
			break;
		case RIGHT_IN :
			mp = MediaPlayer.create(context, R.raw.right_in);
			break;
		case BLACK_DADA :
			mp = MediaPlayer.create(context, R.raw.black_dada);
			break;
		case NIGHT_SHIFT :
			mp = MediaPlayer.create(context, R.raw.nightshift);
			break;
		case ATTACK_ON_TITAN :
			mp = MediaPlayer.create(context, R.raw.attack_on_titan);
			break;
		default :
			mp = MediaPlayer.create(context, R.raw.motherboard);
			break;
		}
		
		mp.setLooping(true);	
	}
	
	public void startBGM(Context context){
		SharedPreferences sf = PreferenceManager.getDefaultSharedPreferences(context);
		if(sf.getBoolean("sound_opt", true) == false){
			if(mp.isPlaying() == true)
				mp.pause();
			track_num = NONE_TRACK;
			return;
		}
		
		if(mp.isPlaying() == false)
			mp.start();
	}
	
	public void pauseBGM(){
		if(mp.isPlaying() == true)
			mp.pause();
	}
	
	public void releaseBGM(){
		mp.stop();
		mp.release();
		track_num = NONE_TRACK;
	}
	
	public int getBGMTracknum(){
		return track_num;
	}

	public void setSound(Context context, int sound_num){
		SharedPreferences sf = PreferenceManager.getDefaultSharedPreferences(context);
		if(sf.getBoolean("effect_opt", true) == false) return;
		
		if(sndp != null){
			sndp.release();
		}

		switch(sound_num){
		case 0 :
			sndp = MediaPlayer.create(context, R.raw.tick);
			break;
		case 1 :
			sndp = MediaPlayer.create(context, R.raw.blop);
			break;
		case 2 :
			sndp = MediaPlayer.create(context, R.raw.gun_cocking);
			break;
		case 3 :
			sndp = MediaPlayer.create(context, R.raw.gun_shot);
			break;
		case 4 :
			sndp = MediaPlayer.create(context, R.raw.laugh);
			break;
		case 5 :
			sndp = MediaPlayer.create(context, R.raw.cry);
			break;
		case 6 :
			sndp = MediaPlayer.create(context, R.raw.fast_cocking);
			break;
		default :
			sndp = MediaPlayer.create(context, R.raw.tick);
			break;
		}

		sndp.setLooping(false);	
		sndp.start();
	}
}
