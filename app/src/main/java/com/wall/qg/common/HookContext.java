package com.wall.qg.common;

import com.wall.qg.jd.JDConstants;
import com.wall.qg.jd.hookhandler.FillOrderHookHandler;
import com.wall.qg.jd.hookhandler.ProductDetailHookHandler;
import com.wall.qg.jd.hookhandler.ProductListHookHandler;
import com.wall.qg.jd.hookhandler.WebViewHookHandler;
import com.wall.qg.tb.TBConstants;
import com.wall.qg.tb.hookhandler.TBDetailHookHandler;
import com.wall.qg.tb.hookhandler.TBMainHookHandler;
import com.wall.qg.tb.hookhandler.TBPurchaseHookHandler;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import de.robv.android.xposed.XC_MethodHook;

public class HookContext {
    public final ClassLoader appClassLoader;
    public final AtomicReference<ActivityHooKHandler> HANDLER_REF = new AtomicReference<>();
    //    public final AtomicReference<Intent> LAST_INTENT = new AtomicReference<>();
    public final AtomicBoolean SHOW_DEBUG_CONSOLE = new AtomicBoolean(true);

    public HookContext(ClassLoader appClassLoader) {
        this.appClassLoader = appClassLoader;
    }

    public ActivityHooKHandler initHandler(String activityName, XC_MethodHook.MethodHookParam param) {
        ActivityHooKHandler oldHandler = HANDLER_REF.get();
        if (oldHandler != null) {
            oldHandler.destroy();
        }
        ActivityHooKHandler handler = null;
        if (JDConstants.JD_PROD_DTL_ACT.equals(activityName)) {
            handler = new ProductDetailHookHandler(param, this);
        } else if (JDConstants.JD_PROD_LIST_ACT.equals(activityName)) {
            handler = new ProductListHookHandler(param, this);
        } else if (JDConstants.JD_FILL_ORD_ACT.equals(activityName)) {
            handler = new FillOrderHookHandler(param, this);
        } else if (JDConstants.JD_WEB_VIEW_ACT.equals(activityName)) {
            handler = new WebViewHookHandler(param, this);
        } else if (TBConstants.DETAIL_ACT.equals(activityName)) {
            handler = new TBDetailHookHandler(param, this);
        } else if (TBConstants.MAIN_ACT.equals(activityName)) {
            handler = new TBMainHookHandler(param, this);
        } else if (TBConstants.PURCHASE_ACT.equals(activityName)) {
            handler = new TBPurchaseHookHandler(param, this);
        }
        HANDLER_REF.set(handler);
        return handler;
    }


    public void destroyHandler(String activityName) {
        ActivityHooKHandler handler = HANDLER_REF.get();
        if (handler != null && activityName != null) {
            if (JDConstants.JD_PROD_DTL_ACT.equals(activityName)) {
                if (handler instanceof ProductDetailHookHandler) {
                    handler.destroy();
                    HANDLER_REF.set(null);
                }
            } else if (JDConstants.JD_PROD_LIST_ACT.equals(activityName)) {
                if (handler instanceof ProductListHookHandler) {
                    handler.destroy();
                    HANDLER_REF.set(null);
                }
            } else if (JDConstants.JD_FILL_ORD_ACT.equals(activityName)) {
                if (handler instanceof FillOrderHookHandler) {
                    handler.destroy();
                    HANDLER_REF.set(null);
                }
            } else if (JDConstants.JD_WEB_VIEW_ACT.equals(activityName)) {
                if (handler instanceof WebViewHookHandler) {
                    handler.destroy();
                    HANDLER_REF.set(null);
                }
            } else if (TBConstants.DETAIL_ACT.equals(activityName)) {
                if (handler instanceof TBDetailHookHandler) {
                    handler.destroy();
                    HANDLER_REF.set(null);
                }
            } else if (TBConstants.MAIN_ACT.equals(activityName)) {
                if (handler instanceof TBMainHookHandler) {
                    handler.destroy();
                    HANDLER_REF.set(null);
                }
            }
        } else if (handler != null && activityName == null) {
            handler.destroy();
            HANDLER_REF.set(null);
        }
    }

    public void log(String str) {
        ActivityHooKHandler activityHooKHandler = HANDLER_REF.get();
        if (activityHooKHandler != null) {
            activityHooKHandler.logOnThread(str);
        }
    }
}
