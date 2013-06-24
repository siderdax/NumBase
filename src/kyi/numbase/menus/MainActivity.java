package kyi.numbase.menus;

import kyi.numbase.R;
import kyi.numbase.managers.DataManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
	private RelativeLayout att_rl;
	private Button start_btn;
	private Button record_btn;
	private Button opt_btn;
	private boolean cover_flag;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		
		// Start Title cover
		cover_flag = true;
		att_rl = (RelativeLayout)findViewById(R.id.main_title_rl);
		TextView att_title = (TextView)findViewById(R.id.att_title);
		TextView att_txt = (TextView)findViewById(R.id.att_text);
		
		OnClickListener title_listener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				att_rl.setVisibility(View.GONE);
				cover_flag = false;
				DataManager.getInstance().setBGM(MainActivity.this, DataManager.MOTHER_BOARD);
				DataManager.getInstance().startBGM(MainActivity.this);
			}
		};
		
		att_rl.setOnClickListener(title_listener);
		att_title.setOnClickListener(title_listener);
		att_txt.setOnClickListener(title_listener);
		
		// Start Menu
		start_btn = (Button) findViewById(R.id.g_start);
		record_btn = (Button) findViewById(R.id.record);
		opt_btn = (Button) findViewById(R.id.option);

		OnClickListener clk_listener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				DataManager.getInstance().getVibOpt(MainActivity.this, 150);
				DataManager.getInstance().setSound(MainActivity.this, 0);
				
				Intent intent;
				switch (v.getId()) {
				case R.id.g_start:
					start_btn.setVisibility(View.INVISIBLE);
					record_btn.setVisibility(View.INVISIBLE);
					opt_btn.setVisibility(View.INVISIBLE);
					
					intent = new Intent(MainActivity.this,
							GSelectActivity.class);
					startActivity(intent);
					break;
				case R.id.option:
					intent = new Intent(MainActivity.this, OptionActivity.class);
					startActivity(intent);
					break;
				case R.id.record:
					intent = new Intent(MainActivity.this, RecordActivity.class);
					startActivity(intent);
					break;
				}
			}
		};

		start_btn.setOnClickListener(clk_listener);
		record_btn.setOnClickListener(clk_listener);
		opt_btn.setOnClickListener(clk_listener);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		DataManager.getInstance().releaseBGM();
		
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		start_btn.setVisibility(View.VISIBLE);
		record_btn.setVisibility(View.VISIBLE);
		opt_btn.setVisibility(View.VISIBLE);
		
		// Play BGM
		if(cover_flag == true){
			DataManager.getInstance().setBGM(this, DataManager.ATTACK_ON_TITAN);
		}
		else if(DataManager.getInstance().getBGMTracknum() != DataManager.MOTHER_BOARD)
			DataManager.getInstance().setBGM(this, DataManager.MOTHER_BOARD);
		DataManager.getInstance().startBGM(this);
		
		super.onResume();
	}
}
