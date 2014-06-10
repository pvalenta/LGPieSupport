package hk.valenta.lgpiesupport;

import android.content.res.XResources;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class PieSupport implements IXposedHookInitPackageResources, IXposedHookLoadPackage, IXposedHookZygoteInit {

	@Override
	public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
		// get config
		XSharedPreferences pref = new XSharedPreferences("hk.valenta.lgpiesupport", "config");
		
		if (pref.getBoolean("HideNavBar", true)) {
			// disable navigation bar
			XResources.setSystemWideReplacement("android", "bool", "config_showNavigationBar", false);
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
				}
			}			
		});
	}
}
