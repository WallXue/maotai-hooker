package com.wall.qg.tb.hookhandler;

import android.widget.TextView;

import com.wall.qg.common.ActivityHooKHandler;
import com.wall.qg.common.HookContext;
import com.wall.qg.common.HookHelper;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class TBMainHookHandler extends ActivityHooKHandler {
    private static final String CART_TITLE = "购物车";

    private Thread backgroundThread = null;
    private Timer submitTimer;
    private volatile boolean stop = false;
    private volatile boolean plan = false;

    private Object mTabHost;
    private String tabTitle = "";

    public TBMainHookHandler(XC_MethodHook.MethodHookParam hookParam, HookContext hookContext) {
        super(hookParam, hookContext);
    }

    @Override
    public void destroy() {
        log("destroy...");
        stop = true;
        if (backgroundThread != null) {
            backgroundThread.interrupt();
        }
        cancelTimer();
    }

    @Override
    public void doHandler() {
        log("进入首页...");
        mTabHost = HookHelper.getFieldValueByName(hookParam.thisObject, "mTabHost");
        doCheckCart();
    }

    private void doCheckCart() {
        backgroundThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!stop) {
                    //不在购物车页面
                    if (!CART_TITLE.equals(checkTabChange())) {
                        if(plan){
                            cancelTimer();
                            plan = false;
                        }
                        HookHelper.sleepCurrentThread(300);
                        continue;
                    }
                    //开启定时提交任务
                    if (!plan) {
                        logOnThread("将需要结算的商品手动选中");
                        planSubmitTask();
                        plan = true;
                    }
                    HookHelper.sleepCurrentThread(300);
                }
            }
        });
        backgroundThread.start();
    }

    private String checkTabChange() {
        Object getCurrentTabView = XposedHelpers.callMethod(mTabHost, "getCurrentTabView");
        Object mTitle = HookHelper.getFieldValueByName(getCurrentTabView, "mTitle");
        if (mTitle != null && !tabTitle.equals(mTitle + "")) {
            logOnThread("进入 " + mTitle);
            tabTitle = mTitle + "";
        }
        return mTitle + "";
    }


    private void planSubmitTask() {
        cancelTimer();
        submitTimer = new Timer();
        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                try {
                    logOnThread("提交结算");
                    TextView jsTv = findTextViewByText("结算");
                    if (jsTv == null) {
                        jsTv = findTextViewByText("领券结算");
                    }
                    if (jsTv != null && jsTv.getWidth() != 0 && jsTv.getHeight() != 0) {
                        int width = jsTv.getWidth();
                        int height = jsTv.getHeight();
                        realClick(jsTv, HookHelper.randomFloat(width), HookHelper.randomFloat(height));
                    }
                } catch (Exception e) {
                    logOnThread(e.toString());
                }
                plan = false;
            }
        };
        Date submitDate = getSubmitDate();
        if (new Date().after(submitDate)) {
            logOnThread("立即执行结算");
            submitTimer.schedule(task, 88);
        } else {
            logOnThread("定时执行结算：" + simpleDateFormat.format(submitDate));
            submitTimer.schedule(task, submitDate);
        }
    }

    public void cancelTimer() {
        if (submitTimer != null) {
            try {
                submitTimer.cancel();
                logOnThread("清除定时任务");
            } catch (Exception ignored) {
            }
        }
    }

    private Date getSubmitDate() {
        Date openDate = getTBOpenDate();
        Date endDate = getTBEndDate();
        Date now = new Date();
        if (now.after(endDate)) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(openDate);
            cal.add(Calendar.DAY_OF_MONTH, 1);
            openDate = cal.getTime();
        }
        return openDate;
    }

    private Date getTBOpenDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.set(Calendar.HOUR_OF_DAY, 20);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 100);
        return cal.getTime();
    }

    private Date getTBEndDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getTBOpenDate());
        cal.add(Calendar.MINUTE, 5);
        return cal.getTime();
    }
}
