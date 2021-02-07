package com.wall.qg.tb;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.wall.qg.common.ActivityHooKHandler;
import com.wall.qg.common.HookContext;
import com.wall.qg.common.HookHelper;
import com.wall.qg.jd.hookhandler.SetClickHookHandler;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;


public class TaobaoHooker implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!TBConstants.TB_PKG_NAME.equals(lpparam.packageName)) {
            return;
        }
        final ClassLoader appClassLoader = lpparam.classLoader;
        HookHelper.hiddenXposedBridge();
//        HookHelper.showActivityNameHelper(appClassLoader);

        final HookContext hookContext = new HookContext(appClassLoader);
        XposedHelpers.findAndHookMethod(Activity.class.getName(), appClassLoader,
                "onResume", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                        String activityName = param.thisObject.getClass().getSimpleName();
                        ActivityHooKHandler handler = hookContext.initHandler(activityName, param);
                        if (handler != null) {
                            handler.doHandler();
                        }
                    }
                });
        XposedHelpers.findAndHookMethod(Activity.class.getName(), appClassLoader,
                "onPause", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                        String activityName = param.thisObject.getClass().getSimpleName();
                        hookContext.destroyHandler(activityName);
                    }
                });
        XposedHelpers.findAndHookMethod("com.taobao.tao.TaobaoApplication", appClassLoader,
                "onTerminate", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                        hookContext.destroyHandler(null);
                    }
                });
//        XposedHelpers.findAndHookMethod(View.class.getName(), appClassLoader, "setOnClickListener", View.OnClickListener.class, new SetClickHookHandler(hookContext));

//        XposedHelpers.findAndHookMethod(Activity.class.getName(), appClassLoader, "startActivityForResult", Intent.class, int.class, Bundle.class, new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                Intent intent = (Intent) param.args[0];
//                Intent clone = (Intent) intent.clone();
////                hookContext.LAST_INTENT.set(clone);
//            }
//        });

    }

}


