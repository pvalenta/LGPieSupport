package hk.valenta.lgpiesupport;

import android.view.Display;
import android.view.ViewGroup;
import android.view.WindowManager;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class PieSupport implements IXposedHookLoadPackage, IXposedHookZygoteInit {

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		// disable navigation ring
		if (lpparam.packageName.equals("com.android.systemui")) {
			try {
				XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "getNavigationBarLayoutParams", new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						// get layout params
						WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams)param.getResult();
						layoutParams.height = 0;
					}
				});
				XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "getSearchLayoutParams", ViewGroup.LayoutParams.class, new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						// get layout params
						WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams)param.getResult();
						layoutParams.height = 0;
					}
				});
				XposedHelpers.findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "getGlobalAccessWindowParams", new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						// get layout params
						WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams)param.getResult();
						layoutParams.height = 0;
					}
				});
			} catch (Exception ex) {
				// report it
				XposedBridge.log(String.format("PieSupport - failed set layout params with error: %s", ex.getMessage()));
			}
		}
	}

	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		XposedHelpers.findAndHookMethod("com.android.internal.policy.impl.PhoneWindowManager", null, "getNonDecorDisplayHeight", int.class, int.class, int.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				// get full height
				int fullHeight = (Integer)param.args[1];
				
				// set 1px less only
				param.setResult(fullHeight - 1);
			}			
		});
		XposedHelpers.findAndHookMethod("com.android.internal.policy.impl.PhoneWindowManager", null, "setInitialDisplaySize", Display.class, int.class, int.class, int.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				// get array
				int[] mNavigationBarHeightForRotation = (int[])XposedHelpers.getObjectField(param.thisObject, "mNavigationBarHeightForRotation");
				
				// set all to 0
				mNavigationBarHeightForRotation[0] = 0;
				mNavigationBarHeightForRotation[1] = 0;
				mNavigationBarHeightForRotation[2] = 0;
				mNavigationBarHeightForRotation[3] = 0;
			}
		});
	}
}
