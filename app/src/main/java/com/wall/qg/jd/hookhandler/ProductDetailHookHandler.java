package com.wall.qg.jd.hookhandler;

import android.content.Intent;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wall.qg.common.ActivityHooKHandler;
import com.wall.qg.common.HookContext;
import com.wall.qg.common.HookHelper;
import com.wall.qg.jd.JDConstants;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.LockSupport;

import de.robv.android.xposed.XC_MethodHook;

public class ProductDetailHookHandler extends ActivityHooKHandler {


    private Thread backgroundThread = null;
    private volatile boolean stop = false;
    private Timer timer;

    public ProductDetailHookHandler(XC_MethodHook.MethodHookParam hookParam, HookContext hookContext) {
        super(hookParam, hookContext);
    }


    @Override
    public void doHandler() {
        log("进入商品详情页面...");
        Object productInfoObj = HookHelper.getFieldValueByTypeName(hookParam.thisObject, "ProductDetailEntity");
        if (productInfoObj != null) {
            JSONObject productInfo = JSON.parseObject(JSON.toJSONString(productInfoObj));
            log("name:" + productInfo.getString("defaultName"));
            log("skuId:" + productInfo.getString("skuId"));
            if (JDConstants.MAOTAI_SKU.equals(productInfo.getString("skuId"))) {
                doOrder();
            }
        }
    }

    @Override
    public void destroy() {
        log("destroy...");
        stop = true;
        try {
            if (backgroundThread != null) {
                backgroundThread.interrupt();
            }
        }catch (Exception e){
        }
        cancelTimer();
    }

    //      1等待页面加载时间拉长、、3秒
//     2判断页面是否在提交订单？如果在没有抢到则后退， 如果在订单提交页面  无需处理，循环提交（不做刷新机制）
    private void doOrder() {
        log("进入抢购商品");
        backgroundThread = new Thread(new Runnable() {
            @Override
            public void run() {
                final int orderWaitTime = 100;
                int orderCount = 0;
                while (!stop) {
                    TextView orderTv = findTextViewByText("立即抢购");
                    TextView preOrderTv = findTextViewByText("立即预约");
                    TextView waitTv = findTextViewByText("已预约");
                    TextView plusTv = findTextViewByText("开通PLUS立即参与");
                    TextView reloadTv = findTextViewByText("重试");
                    if (orderTv == null && preOrderTv == null && waitTv == null && plusTv == null && reloadTv == null) {
                        HookHelper.sleepCurrentThread(50);
                    }else if(reloadTv != null){
                        callClick(reloadTv);
                        HookHelper.sleepCurrentThread(100);
                    } else if (orderTv != null) {//下单逻辑
                        if (orderCount > 5) {//点击5次重新进入页面
                            reEntry();
                            return;
                        }
                        callClick(orderTv);
                        orderCount++;
                        HookHelper.sleepCurrentThread(orderWaitTime);
                    } else if (plusTv != null) {
                        logOnThread("请开通plus会员...");
                        return;
                    } else if (waitTv != null) {//等待逻辑
                        logOnThread("已预约");
                        submitTimer();
                        LockSupport.park(backgroundThread);
                    } else if (preOrderTv != null) {//预约逻辑
                        logOnThread("预约中");
                        callClick(preOrderTv);
                        HookHelper.sleepCurrentThread(1000);
                    }
                }
            }
        });
        backgroundThread.start();
    }

    private void cancelTimer() {
        if (timer != null) {
            try {
                timer.cancel();
                logOnThread("清除定时任务");
            } catch (Exception e) {
            }
        }
    }

    private void submitTimer() {
        cancelTimer();
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
//                reEntry();
                LockSupport.unpark(backgroundThread);
            }
        };
        Date submitDate = getSubmitDate();
        if (new Date().after(submitDate)) {
            logOnThread("立即执行任务");
            timer.schedule(task, 88);
        } else {
            logOnThread("定时执行任务：" + simpleDateFormat.format(submitDate));
            timer.schedule(task, submitDate);
        }
    }

    private Date getSubmitDate() {
        Date jdOpenTime = getJDOpenTime();
        Date jdEndTime = getJDEndTime();
        Date now = new Date();
        if (now.after(jdEndTime)) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(jdOpenTime);
            cal.add(Calendar.DAY_OF_MONTH, 1);
            jdOpenTime = cal.getTime();
        }
        return jdOpenTime;
    }

    private Date getJDOpenTime() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 30);
        return cal.getTime();
    }

    private Date getJDEndTime() {
        Date jdOpenTime = getJDOpenTime();
        Calendar cal = Calendar.getInstance();
        cal.setTime(jdOpenTime);
        cal.add(Calendar.MINUTE, 5);
        return cal.getTime();
    }


}
