package kyi.numbase.managers;

import java.util.Calendar;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBmanager extends SQLiteOpenHelper {
	public DBmanager(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String qry = "CREATE TABLE GRECORD ( NO INTEGER PRIMARY KEY AUTOINCREMENT, " +
				"TIME TEXT, WIN INTEGER, LOSE INTEGER);";
		db.execSQL(qry);
		Log.i("KYI", "DBH onCreate");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		// TODO Auto-generated method stub
		Log.i("KYI", "on Upgrade");
		db.execSQL("DROP TABLE IF EXISTS GRECORD");
		onCreate(db);
	}
	
	public void ws_sql_ins(int win, int lose){
		SQLiteDatabase db = getWritableDatabase();
		
		String str = "INSERT INTO GRECORD (TIME, WIN, LOSE) VALUES (\"" + get_Time()
				+ "\", " + Integer.toString(win) + ", "
				+ Integer.toString(lose) + ")"; 
		
		
		Log.i("KYI", str);
		
		db.execSQL(str);
	}
	
	private String get_Time(){
    	Calendar cal = Calendar.getInstance();
    	String current_time = cal.get(Calendar.YEAR) + "-" +
    			cal.get(Calendar.MONTH) + "-" +
    			cal.get(Calendar.DAY_OF_MONTH) + " " +
    			String.format("%02d", cal.get(Calendar.HOUR_OF_DAY)) + ":" +
    			String.format("%02d", cal.get(Calendar.MINUTE)) + ":" +
    			String.format("%02d", cal.get(Calendar.SECOND));
    	return current_time;
    }
}
