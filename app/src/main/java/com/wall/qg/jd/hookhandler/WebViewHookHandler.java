package com.wall.qg.jd.hookhandler;

import com.wall.qg.common.ActivityHooKHandler;
import com.wall.qg.common.HookContext;
import com.wall.qg.common.HookHelper;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.locks.LockSupport;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public class WebViewHookHandler extends ActivityHooKHandler {

    private Thread backgroundThread = null;
    private volatile boolean stop = false;

    private String enterUrl;
    private Object mJdWebView;
    private Object webView;
    private Object webEntity;

    public WebViewHookHandler(XC_MethodHook.MethodHookParam hookParam, HookContext hookContext) {
        super(hookParam, hookContext);
    }


    @Override
    public void doHandler() {
        log("进入WebView页面...");
        Object mFragment = HookHelper.getFieldValueByName(activity, "mFragment");
        mJdWebView = HookHelper.getFieldValueByName(mFragment, "mJdWebView");
        webView = HookHelper.getFieldValueByName(mJdWebView, "webView");
        webEntity = HookHelper.getFieldValueByName(mFragment, "webEntity");
        enterUrl = HookHelper.getFieldValueByName(webEntity, "url") + "";

        log("链接:" + enterUrl);
        //todo 判断是否抢购商品提交订单页面
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

//    private static final String submit_js = "onInjectJsAfter('测试啊啊啊啊');";

//    private static final String submit_js = "(function f() { var btns = document.body.getElementsByClassName('btn'); for (var i = 0; i < btns.length; i++) { if (btns[i].innerText === '提交订单') { btns[i].click(); } } var as = document.body.getElementsByTagName('a'); for (var i = 0; i < as.length; i++) { if (as[i].innerText === '提交订单') { as[i].click(); } } var need_return = false; if (document.getElementsByTagName('html')[0].innerHTML.indexOf('很遗憾') != -1) { need_return = true; } return need_return; })()";
    //判断页面是否 空白页面
    private static final String submit_js = "(function f() { var need_return = false; var inner_text = document.getElementsByTagName('html')[0].innerHTML; if (inner_text.indexOf('木有抢到...') != -1) { need_return = true; } if (inner_text == '') { need_return = true; } return need_return; })()";

    public void doSubmit() {
        log("等待提交订单...");
        InvocationHandler invocationHandler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String str = args[0].toString();
                logOnThread("是否重新加载：" + str);
                if ("true".equals(str)) {//没有进行点击
                    reEntry();
                } else {
                    int width = rootView.getWidth();
                    int height = rootView.getHeight();
                    realClick(rootView, width - 5, height - 5);
                }
                HookHelper.sleepCurrentThread(300);
                LockSupport.unpark(backgroundThread);
                return null;
            }
        };
        Class<?> aClass = XposedHelpers.findClass("com.tencent.smtt.sdk.ValueCallback", hookContext.appClassLoader);
        Object valueCallback = Proxy.newProxyInstance(hookContext.appClassLoader, new Class[]{aClass}, invocationHandler);
        backgroundThread = new Thread(new Runnable() {
            @Override
            public void run() {
                final int wait_load_time = 58;
                int wait_count = 0;
                int submit_count = 0;
                while (!stop) {
                    String currentUrl = HookHelper.getFieldValueByName(webEntity, "url") + "";
                    logOnThread(currentUrl);
                    Object isPageLoaded = HookHelper.getFieldValueByName(mJdWebView, "isPageLoaded");
                    //判断页面是否加载完毕
                    if (Boolean.FALSE.toString().equals(isPageLoaded + "")) {
                        logOnThread("页面加载中...");
                        if ((wait_count * wait_load_time) >= 8888) {
                            logOnThread("页面加载超时，退出重新抢购！");
                            reEntry();
                            return;
                        } else {
                            HookHelper.sleepCurrentThread(wait_load_time);
                            wait_count++;
                            continue;
                        }
                    } else {
                        wait_count = 0;
                        logOnThread("发起提交...");
                        rootView.post(() -> XposedHelpers.callMethod(webView, "evaluateJavascript", "javascript:" + submit_js, valueCallback));
                        LockSupport.park(backgroundThread);
                    }
                }
            }
        });
        backgroundThread.start();
    }

}
