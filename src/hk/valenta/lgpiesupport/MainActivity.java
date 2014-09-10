package hk.valenta.lgpiesupport;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

	RelativeLayout hideNavBarModeGroup;
	RadioButton disableNavRadio;
	RadioButton zeroNavRadio;
	boolean radioSwitch = false;
	
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
		boolean hideNavBar = pref.getBoolean("HideNavBar", true); 
		navBar.setChecked(hideNavBar);
		navBar.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// set it in preferences
				SharedPreferences pref = getSharedPreferences("config", Context.MODE_WORLD_READABLE);
				boolean checked = buttonView.isChecked();
				pref.edit().putBoolean("HideNavBar", checked).commit();
				showHideNavMode(checked);
			}
		});
		hideNavBarModeGroup = (RelativeLayout)findViewById(R.id.hide_navigation_bar_mode_group);
		this.showHideNavMode(hideNavBar);		
		
		// hide mode
		String hideNavBarMode = pref.getString("HideNavBarMode", "Disable");
		disableNavRadio = (RadioButton)findViewById(R.id.main_hide_navbar_disable);
		zeroNavRadio = (RadioButton)findViewById(R.id.main_hide_navbar_0way);
		this.showHideNavMode(hideNavBarMode);
		disableNavRadio.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// set it in preferences
				boolean checked = buttonView.isChecked();
				if (checked) {
					setHideNavMode("Disable");
				} else {
					setHideNavMode("Zero");
				}
			}
		});
		zeroNavRadio.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// set it in preferences
				boolean checked = buttonView.isChecked();
				if (checked) {
					setHideNavMode("Zero");
				} else {
					setHideNavMode("Disable");
				}
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
	
	private void showHideNavMode(boolean show) {
		if (show) {
			hideNavBarModeGroup.setVisibility(View.VISIBLE);
		} else {
			hideNavBarModeGroup.setVisibility(View.GONE);
		}		
	}
	
	private void showHideNavMode(String mode) {
		if (mode.equals("Zero")) {
			disableNavRadio.setChecked(false);
			zeroNavRadio.setChecked(true);
		} else {
			disableNavRadio.setChecked(true);
			zeroNavRadio.setChecked(false);			
		}
	}
	
	@SuppressLint("WorldReadableFiles")
	@SuppressWarnings("deprecation")
	private void setHideNavMode(String mode) {
		// made sure only one time called
		if (radioSwitch) return;
		
		radioSwitch = true;
		
		// show it
		showHideNavMode(mode);
		
		// set in config
		SharedPreferences pref = getSharedPreferences("config", Context.MODE_WORLD_READABLE);
		pref.edit().putString("HideNavBarMode", mode).commit();
		
		radioSwitch = false;
	}
}
