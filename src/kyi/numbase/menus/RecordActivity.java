package kyi.numbase.menus;

import kyi.numbase.R;
import kyi.numbase.managers.DBmanager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class RecordActivity extends Activity {

	private SharedPreferences sharedPref;
	private int win_cnt, lose_cnt;
	private DBmanager dbm = new DBmanager(this, "Baseball.db", null, 1);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_record);
		
		String str;
		
		//SharedPreferences
        sharedPref = getSharedPreferences("single", Activity.MODE_PRIVATE);
		
        //DB Open
        statCount();
        
        //Battle record
        TextView battle_record = (TextView) findViewById(R.id.multi_record);
        str = "대전 전적 : " + Integer.toString(win_cnt) + "승, " + Integer.toString(lose_cnt) + "패";
        battle_record.setText(str);
        battle_record.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(RecordActivity.this, StatActivity.class));
			}
		});
        
        //Single record
		TextView single_record = (TextView) findViewById(R.id.single_record);
		Float record = (float)(Integer.parseInt(sharedPref.getString("single_record", "9999000")) / 1000.0);
		str = "최고 싱글 기록 : " + record + "초";
		single_record.setText(str);
	}	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		dbm.close();
		super.onDestroy();
	}

	private void statCount(){
		
		SQLiteDatabase db = dbm.getReadableDatabase();
		Cursor csr = db.rawQuery("SELECT * FROM GRECORD;", null);
		
		win_cnt = 0;
		lose_cnt = 0;
		
		while(csr.moveToNext()){
			if(csr.getInt(2) == 1) win_cnt++;
			else if(csr.getInt(3) == 1) lose_cnt++;
			Log.d("KYI", "DB READ");
		}
		
		Log.i("KYI", "Win : " + win_cnt + ", Lose : " + lose_cnt);
	}
}
