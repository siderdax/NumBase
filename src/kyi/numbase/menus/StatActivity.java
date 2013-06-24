package kyi.numbase.menus;

import kyi.numbase.R;
import kyi.numbase.managers.DBmanager;
import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class StatActivity extends Activity {

	private ListView stat_list;
	ArrayAdapter<String> stat_adt;
	private DBmanager dbm = new DBmanager(this, "Baseball.db", null, 1);
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_stat);
		
		stat_list = (ListView)findViewById(R.id.stat_list);
		stat_adt = new ArrayAdapter<String>(this, R.layout.view_items, R.id.game_listview_text);
		stat_list.setAdapter(stat_adt);
		
		view_list();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		dbm.close();
		super.onDestroy();
	}

	private void view_list(){		
		SQLiteDatabase db = dbm.getReadableDatabase();
		Cursor csr = db.rawQuery("SELECT * FROM GRECORD;", null);
		String str;
		
		while(csr.moveToNext()){
			if(csr.getInt(2) == 1)
				str = csr.getString(1) + "--> 승리";
			else if(csr.getInt(3) == 1)
				str = csr.getString(1) + "--> 패배";
			else
				str = csr.getString(1) + "--> 드로우";
			
			stat_adt.add(str);
		}
	}
}
