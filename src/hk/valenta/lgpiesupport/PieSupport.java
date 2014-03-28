package hk.valenta.lgpiesupport;

import android.content.res.XResources;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class PieSupport implements IXposedHookInitPackageResources, IXposedHookLoadPackage {

	@Override
	public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
		// disable navigation bar
		XResources.setSystemWideReplacement("android", "bool", "config_showNavigationBar", false);
		
		if (resparam.packageName.equals("com.android.systemui")) {
			resparam.res.hookLayout("com.android.systemui", "layout", "global_access_panel", new XC_LayoutInflated() {
				@Override
				public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
					// get it as linear layout
					LinearLayout panel = (LinearLayout)liparam.view;
					ViewGroup.LayoutParams params = panel.getLayoutParams();
					
					// set 0 height for global access panel
					XposedBridge.log("global_access_panel height set to 0");
					params.height = 0;
				}
			});
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
						// set it to false
						param.args[0] = false;
					}				
				});
				XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "shouldDisableNavbarGestures", new XC_MethodReplacement() {
					@Override
					protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
						// return true
						param.setResult(true);
						return true;
					}
				});
			} catch (Exception ex) {
				// report it
				XposedBridge.log(String.format("PieSupport - failed 'enableGlobalAccess' with error: %s", ex.getMessage()));
			}
		}		
	}
}
