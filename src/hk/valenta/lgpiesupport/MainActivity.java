package hk.valenta.lgpiesupport;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class MainActivity extends Activity {

	@SuppressLint("WorldReadableFiles")
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// set halo theme
		setTheme(android.R.style.Theme_Holo_Light_NoActionBar);		
		
		// super
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// set version number
		TextView version = (TextView)findViewById(R.id.main_version);
		try {
			PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
			version.setText(info.versionName);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			version.setText(e.getMessage());
		}
		
		// get current configuration
		SharedPreferences pref = getSharedPreferences("config", Context.MODE_WORLD_READABLE);
		
		// hide navbar
		CheckBox navBar = (CheckBox)findViewById(R.id.main_hide_navbar);
		navBar.setChecked(pref.getBoolean("HideNavBar", true));
		navBar.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// set it in preferences
				SharedPreferences pref = getSharedPreferences("config", Context.MODE_WORLD_READABLE);
				pref.edit().putBoolean("HideNavBar", buttonView.isChecked()).commit();
			}
		});
		
		// disable navring
		CheckBox navRing = (CheckBox)findViewById(R.id.main_disable_navring);
		navRing.setChecked(pref.getBoolean("DisableNavRing", true));
		navRing.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// set it in preferences
				SharedPreferences pref = getSharedPreferences("config", Context.MODE_WORLD_READABLE);
				pref.edit().putBoolean("DisableNavRing", buttonView.isChecked()).commit();
			}
		});
		
		// reduce width
		CheckBox reduceWidth = (CheckBox)findViewById(R.id.main_reduce_width);
		reduceWidth.setChecked(pref.getBoolean("ReduceWidth", true));
		reduceWidth.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// set it in preferences
				SharedPreferences pref = getSharedPreferences("config", Context.MODE_WORLD_READABLE);
				pref.edit().putBoolean("ReduceWidth", buttonView.isChecked()).commit();
			}
		});
		
		// reduce height
		CheckBox reduceHeight = (CheckBox)findViewById(R.id.main_reduce_height);
		reduceHeight.setChecked(pref.getBoolean("ReduceHeight", true));
		reduceHeight.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// set it in preferences
				SharedPreferences pref = getSharedPreferences("config", Context.MODE_WORLD_READABLE);
				pref.edit().putBoolean("ReduceHeight", buttonView.isChecked()).commit();
			}
		});
	}
}
