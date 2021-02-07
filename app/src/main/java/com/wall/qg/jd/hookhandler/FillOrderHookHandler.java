package com.wall.qg.jd.hookhandler;

import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wall.qg.common.ActivityHooKHandler;
import com.wall.qg.common.HookContext;
import com.wall.qg.jd.JDConstants;
import com.wall.qg.common.HookHelper;

import de.robv.android.xposed.XC_MethodHook;

public class FillOrderHookHandler extends ActivityHooKHandler {


    private Thread backgroundThread = null;
    private volatile boolean stop = false;

    public FillOrderHookHandler(XC_MethodHook.MethodHookParam hookParam, HookContext hookContext) {
        super(hookParam, hookContext);
    }

    @Override
    public void doHandler() {
        log("进入订单提交页面...");
        Object newCurrentOrderObj = HookHelper.getFieldValueByTypeName(hookParam.thisObject, "NewCurrentOrder");
        if (newCurrentOrderObj != null) {
            JSONObject newCurrentOrder = JSON.parseObject(JSON.toJSONString(newCurrentOrderObj));
            log("skuId:" + newCurrentOrder.getString("wareId"));
            if (JDConstants.MAOTAI_SKU.equals(newCurrentOrder.getString("wareId"))) {
                doSubmit();
            }
        }
    }

    @Override
    public void destroy() {
        log("destroy...");
        stop = true;
        if (backgroundThread != null) {
            backgroundThread.interrupt();
        }
    }

    private void doSubmit() {
        log("准备提交订单");
        backgroundThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!stop) {
                    TextView submitTv = findTextViewByText("提交订单");
                    if (submitTv != null) {
                        logOnThread("提交订单");
                        callClick(submitTv);
                        HookHelper.sleepCurrentThread(200);
                    } else {
                        HookHelper.sleepCurrentThread(50);
                    }
                }
            }
        });
        backgroundThread.start();

    }


}
