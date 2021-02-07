package com.wall.qg.tb.hookhandler;

import android.widget.TextView;

import com.wall.qg.common.ActivityHooKHandler;
import com.wall.qg.common.HookContext;
import com.wall.qg.common.HookHelper;

import de.robv.android.xposed.XC_MethodHook;

public class TBPurchaseHookHandler extends ActivityHooKHandler {

    private Thread backgroundThread = null;
    private volatile boolean stop = false;

    public TBPurchaseHookHandler(XC_MethodHook.MethodHookParam hookParam, HookContext hookContext) {
        super(hookParam, hookContext);
    }

    @Override
    public void doHandler() {
        log("进入提交订单页面...");
        log(entryIntent.getExtras().toString());
        doSubmit();
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
                    if (submitTv != null && submitTv.getWidth() != 0 && submitTv.getHeight() != 0) {
                        logOnThread("提交订单");
                        int width = submitTv.getWidth();
                        int height = submitTv.getHeight();
                        realClick(submitTv, HookHelper.randomFloat(width), HookHelper.randomFloat(height));
                        HookHelper.sleepCurrentThread(230);
                    } else {
                        HookHelper.sleepCurrentThread(50);
                    }
                }
            }
        });
        backgroundThread.start();
    }
}
