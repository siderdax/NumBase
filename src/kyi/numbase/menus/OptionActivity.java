package kyi.numbase.menus;

import kyi.numbase.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class OptionActivity extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.app_opt);
	}
}
