package com.wall.qg.tb.hookhandler;

import com.alibaba.fastjson.JSON;
import com.wall.qg.common.ActivityHooKHandler;
import com.wall.qg.common.HookContext;
import com.wall.qg.common.HookHelper;

import de.robv.android.xposed.XC_MethodHook;

public class TBDetailHookHandler extends ActivityHooKHandler {


    public TBDetailHookHandler(XC_MethodHook.MethodHookParam hookParam, HookContext hookContext) {
        super(hookParam, hookContext);
    }

    @Override
    public void doHandler() {
        log("进入商品详情页面...");
    }
}
