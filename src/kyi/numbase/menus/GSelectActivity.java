package kyi.numbase.menus;

import kyi.numbase.R;
import kyi.numbase.managers.DataManager;
import kyi.numbase.plays.BattleActivity;
import kyi.numbase.plays.TutorialActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;

public class GSelectActivity extends Activity {

	static final int BLUE_CONNECT = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_gselect);
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
	    		WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		
		Button battle_btn = (Button)findViewById(R.id.go_battle);
		battle_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				DataManager.getInstance().setSound(GSelectActivity.this, 0);
				DataManager.getInstance().getVibOpt(GSelectActivity.this, 150);
				startActivity(new Intent(GSelectActivity.this, BattleActivity.class));
			}
		});
		
		Button tuto_btn = (Button)findViewById(R.id.excer);
		tuto_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				DataManager.getInstance().setSound(GSelectActivity.this, 0);
				DataManager.getInstance().getVibOpt(GSelectActivity.this, 150);
				startActivity(new Intent(GSelectActivity.this, TutorialActivity.class));
			}
		});
		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		// Play BGM
		if(DataManager.getInstance().getBGMTracknum() != DataManager.MOTHER_BOARD)
			DataManager.getInstance().setBGM(this, DataManager.MOTHER_BOARD);
		DataManager.getInstance().startBGM(this);

		super.onResume();
	}
	
	

}
