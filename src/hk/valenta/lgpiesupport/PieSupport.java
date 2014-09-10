package hk.valenta.lgpiesupport;

import android.content.res.XResources;
import android.view.Display;
import android.view.View;
import android.widget.Space;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class PieSupport implements IXposedHookInitPackageResources, IXposedHookLoadPackage, IXposedHookZygoteInit {

	@Override
	public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
		// get config
		XSharedPreferences pref = new XSharedPreferences("hk.valenta.lgpiesupport", "config");
		
		if (pref.getBoolean("HideNavBar", true)) {
			String hideNavBarMode = pref.getString("HideNavBarMode", "Disable");
			if (hideNavBarMode.equals("Disable")) {
				// disable navigation bar
				XResources.setSystemWideReplacement("android", "bool", "config_showNavigationBar", false);
			} else if (hideNavBarMode.equals("Zero")) {				
				// set 0 height for layout
				XResources.setSystemWideReplacement("android", "dimen", "navigation_bar_height", 0);
				XResources.setSystemWideReplacement("android", "dimen", "navigation_bar_height_landscape", 0);
				XResources.setSystemWideReplacement("android", "dimen", "navigation_bar_height_portrait", 0);
				XResources.setSystemWideReplacement("android", "dimen", "navigation_bar_width", 0);
				if (resparam.packageName.equals("com.android.systemui")) {
					resparam.res.setReplacement("com.android.systemui", "dimen", "navigation_bar_size", 0);
					resparam.res.hookLayout("com.android.systemui", "layout", "status_bar_recent_panel", new XC_LayoutInflated() {
						@Override
						public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
							// set 0 height to space
							Space recent_navi_space = (Space)liparam.view.findViewById(liparam.res.getIdentifier("recent_navi_space", "id", "com.android.systemui"));
							recent_navi_space.setVisibility(View.GONE);
						}
					});
				}
			}
		}
	}

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		// disable navigation ring
		if (lpparam.packageName.equals("com.android.systemui")) {
			try {
				XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "enableGlobalAccess", boolean.class, new XC_MethodHook() {
					@Override
					protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
						// get config
						XSharedPreferences pref = new XSharedPreferences("hk.valenta.lgpiesupport", "config");
						
						if (pref.getBoolean("DisableNavRing", true)) {
							// set it to false
							param.args[0] = false;
						}
					}
				});
				XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "shouldDisableNavbarGestures", new XC_MethodReplacement() {
					@Override
					protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
						// get config
						XSharedPreferences pref = new XSharedPreferences("hk.valenta.lgpiesupport", "config");
						
						if (pref.getBoolean("DisableNavRing", true)) {
							// return true
							param.setResult(true);
							return true;
						} else {
							// return false
							Object result = XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
							param.setResult(result);
							return result;
						}
					}
				});
				XSharedPreferences pref = new XSharedPreferences("hk.valenta.lgpiesupport", "config");
				if (pref.getBoolean("HideNavBar", true) && pref.getString("HideNavBarMode", "Disable").equals("Zero")) {
					XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "addNavigationBar", new XC_MethodReplacement() {					
						@Override
						protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
							// do nothing
							XposedHelpers.setObjectField(param.thisObject, "mNavigationBarView", null);
							XposedBridge.log("No Navigation Bar will be added");
							return null;
						}
					});
				}
			} catch (Exception ex) {
				// report it
				XposedBridge.log(String.format("PieSupport - failed disable global access with touches with error: %s", ex.getMessage()));
			}
		}
	}

	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		XposedHelpers.findAndHookMethod("com.android.internal.policy.impl.PhoneWindowManager", null, "getNonDecorDisplayWidth", int.class, int.class, int.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				// get full height
				int fullWidth = (Integer)param.args[0];
				
				// get config
				XSharedPreferences pref = new XSharedPreferences("hk.valenta.lgpiesupport", "config");
				
				// set 1px less only to solve weather widget bug and keyboard bug
				if (pref.getBoolean("ReduceWidth", true) && (fullWidth == 1920 || fullWidth == 2560)) {
					param.setResult(fullWidth - 1);
				} else if (pref.getBoolean("HideNavBar", true) && pref.getString("HideNavBarMode", "Disable").equals("Zero")) {
					param.setResult(fullWidth);
				}
			}			
		});
		XposedHelpers.findAndHookMethod("com.android.internal.policy.impl.PhoneWindowManager", null, "getNonDecorDisplayHeight", int.class, int.class, int.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				// get full height
				int fullHeight = (Integer)param.args[1];
				
				// get config
				XSharedPreferences pref = new XSharedPreferences("hk.valenta.lgpiesupport", "config");
				
				// set 1px less only to solve weather widget bug and keyboard bug
				if (pref.getBoolean("ReduceHeight", true) && (fullHeight == 1920 || fullHeight == 2560)) {
					param.setResult(fullHeight - 1);
				} else if (pref.getBoolean("HideNavBar", true) && pref.getString("HideNavBarMode", "Disable").equals("Zero")) {
					param.setResult(fullHeight);
				}
			}			
		});
		XposedHelpers.findAndHookMethod("com.android.internal.policy.impl.PhoneWindowManager", null, "setInitialDisplaySize", Display.class, int.class, int.class, int.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				// get config
				XSharedPreferences pref = new XSharedPreferences("hk.valenta.lgpiesupport", "config");
				if (pref.getBoolean("HideNavBar", true) && pref.getString("HideNavBarMode", "Disable").equals("Zero")) {
					// get array
					int[] mNavigationBarWidthForRotation = (int[])XposedHelpers.getObjectField(param.thisObject, "mNavigationBarWidthForRotation");
					
					// set all to 0
					mNavigationBarWidthForRotation[0] = 0;
					mNavigationBarWidthForRotation[1] = 0;
					mNavigationBarWidthForRotation[2] = 0;
					mNavigationBarWidthForRotation[3] = 0;				
					
					// get array
					int[] mNavigationBarHeightForRotation = (int[])XposedHelpers.getObjectField(param.thisObject, "mNavigationBarHeightForRotation");
					
					// set all to 0
					mNavigationBarHeightForRotation[0] = 0;
					mNavigationBarHeightForRotation[1] = 0;
					mNavigationBarHeightForRotation[2] = 0;
					mNavigationBarHeightForRotation[3] = 0;				
				}				
			}
		});
	}
}
