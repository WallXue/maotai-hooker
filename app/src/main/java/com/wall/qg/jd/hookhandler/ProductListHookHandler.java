package com.wall.qg.jd.hookhandler;

import com.wall.qg.common.ActivityHooKHandler;
import com.wall.qg.common.HookContext;

import de.robv.android.xposed.XC_MethodHook;

public class ProductListHookHandler extends ActivityHooKHandler {


    public ProductListHookHandler(XC_MethodHook.MethodHookParam hookParam, HookContext hookContext) {
        super(hookParam, hookContext);
    }

    @Override
    public void doHandler() {
        log("进入商品列表页面...");
//        String s = HookHelper.deCodeClass(hookParam.thisObject.getClass());
//        log(s);
//        try {
//
//            log("to start ProductDetailActivity...");
//            Intent intent = new Intent(activity, activity.getClassLoader().loadClass("com.jd.lib.productdetail.ProductDetailActivity"));
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            activity.startActivityForResult(intent, 0);
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }


    }
}
