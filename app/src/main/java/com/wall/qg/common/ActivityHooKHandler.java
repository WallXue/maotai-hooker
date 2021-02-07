package com.wall.qg.common;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

import de.robv.android.xposed.XC_MethodHook;

//                    int identifier = activity.getResources().getIdentifier("purchase_bottom_layout", "id", activity.getClass().getPackage().getName());
//                    View viewById = activity.findViewById(identifier);

public abstract class ActivityHooKHandler {


    protected final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final int CONSOLE_TV_ID = 191922301;
    private static final int SCROLLVIEW_ID = 191922302;
    private static final int IS_BOTTOM_ID = 191922303;
    private static final int LAYOUT_ID = 191922304;
    protected final HookContext hookContext;
    protected final XC_MethodHook.MethodHookParam hookParam;
    private final AtomicLong logLineNum = new AtomicLong(0);
    protected final Activity activity;
    protected final ViewGroup rootView;
    protected final Intent entryIntent;
    private TextView consoleTv;
    private ScrollView scrollView;
    private CheckBox isBottom;

    public ActivityHooKHandler(XC_MethodHook.MethodHookParam hookParam, HookContext hookContext) {
        this.hookParam = hookParam;
        this.hookContext = hookContext;
        activity = (Activity) hookParam.thisObject;
        entryIntent = (Intent) activity.getIntent().clone();
        Object decorView = activity.getWindow().getDecorView();
        if (decorView instanceof ViewGroup) {
            rootView = (ViewGroup) decorView;
            if (activity.findViewById(LAYOUT_ID) != null) {
                findConsoleView();
            } else {
                generateConsoleView();
            }
        } else {
            rootView = null;
            HookHelper.toastActivity(activity, "无法找到rootView！" + activity.getClass().getName());
        }
    }

    public abstract void doHandler();

    public void destroy() {

    }

    public void showChildView() {
        this.printShowChildView(rootView, "");
    }

    private void printShowChildView(View v, String tab) {
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            log(tab + v.getClass().getSimpleName());
            for (int i = 0; i < vg.getChildCount(); i++) {
                printShowChildView(vg.getChildAt(i), tab + "  ");
            }
        } else {
            log(tab + v.getClass().getSimpleName());
        }
    }

    private void findConsoleView() {
        scrollView = activity.findViewById(SCROLLVIEW_ID);
        consoleTv = activity.findViewById(CONSOLE_TV_ID);
        isBottom = activity.findViewById(IS_BOTTOM_ID);
    }

    private void generateConsoleView() {
        RelativeLayout.LayoutParams layout_LP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layout_LP.setMargins(0, HookHelper.dip2px(activity, 110), 0, 0);
        LinearLayout layout = new LinearLayout(activity);
        layout.setId(LAYOUT_ID);
        layout.setOrientation(LinearLayout.VERTICAL);
//        layout.setBackgroundColor(Color.BLACK);


        scrollView = new ScrollView(activity);
        scrollView.setId(SCROLLVIEW_ID);
        scrollView.setPadding(5, 5, 5, 5);
        scrollView.setBackgroundColor(Color.BLACK);
        scrollView.setVisibility(hookContext.SHOW_DEBUG_CONSOLE.get() ? View.VISIBLE : View.INVISIBLE);

        consoleTv = new TextView(activity);
        consoleTv.setId(CONSOLE_TV_ID);
        consoleTv.setTextSize(8);
        consoleTv.setTextColor(Color.WHITE);
        scrollView.addView(consoleTv, ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.MATCH_PARENT);

        isBottom = new CheckBox(activity);
        isBottom.setId(IS_BOTTOM_ID);
        isBottom.setChecked(true);
        isBottom.setText("保持滚动");
        isBottom.setTextSize(8);
        isBottom.setTextColor(Color.RED);
        isBottom.setBackgroundColor(Color.BLACK);
        isBottom.setVisibility(hookContext.SHOW_DEBUG_CONSOLE.get() ? View.VISIBLE : View.INVISIBLE);

        CheckBox isShowDebug = new CheckBox(activity);
        isShowDebug.setChecked(hookContext.SHOW_DEBUG_CONSOLE.get());
        isShowDebug.setText("显示调试窗口");
        isShowDebug.setTextSize(8);
        isShowDebug.setTextColor(Color.RED);
        isShowDebug.setBackgroundColor(Color.BLACK);
        CompoundButton.OnCheckedChangeListener checkedChangeListener = (buttonView, isChecked) -> {
            hookContext.SHOW_DEBUG_CONSOLE.set(isChecked);
            if (isChecked) {
                scrollView.setVisibility(View.VISIBLE);
                isBottom.setVisibility(View.VISIBLE);
            } else {
                scrollView.setVisibility(View.INVISIBLE);
                isBottom.setVisibility(View.INVISIBLE);
            }
        };
        isShowDebug.setOnCheckedChangeListener(checkedChangeListener);

        layout.addView(isShowDebug, HookHelper.dip2px(activity, 90), RelativeLayout.LayoutParams.WRAP_CONTENT);
        layout.addView(isBottom, HookHelper.dip2px(activity, 90), RelativeLayout.LayoutParams.WRAP_CONTENT);
        layout.addView(scrollView, RelativeLayout.LayoutParams.MATCH_PARENT, HookHelper.dip2px(activity, 180));
        rootView.addView(layout, layout_LP);
//        EditText et=new EditText(activity);
//        et.setText(str);
//        et.setTextColor(Color.argb(0xff, 0x00, 0x00, 0x00));
//        et.setTextSize(15);
//        et.setLayoutParams(LP_WW);
//        layout_sub_Lin.addView(et);
    }

    public Button findButtonByText(String text) {
        try {

            ArrayList<Button> results = new ArrayList<>();
            toFindTextViewByText(results, Button.class, rootView, text);
            return results.stream().filter(btn-> btn.getVisibility()== View.VISIBLE).findFirst().orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    public TextView findTextViewByText(String text) {
        try {

            ArrayList<TextView> results = new ArrayList<>();
            toFindTextViewByText(results, TextView.class, rootView, text);
            return results.stream().filter(tv-> tv.getVisibility()== View.VISIBLE).findFirst().orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private <T> void toFindTextViewByText(ArrayList<T> result, Class<T> clazz, ViewGroup vg, String text) {
        for (int i = 0; i < vg.getChildCount(); i++) {
            View child = vg.getChildAt(i);
            if (child instanceof ViewGroup) {
                toFindTextViewByText(result, clazz, (ViewGroup) child, text);
            } else if (clazz.isInstance(child)) {
                if (child instanceof TextView) {
                    TextView v = (TextView) child;
                    if (v.getText().toString().startsWith(text)) {
                        result.add((T) child);
                    }
                } else if (child instanceof Button) {
                    Button b = (Button) child;
                    if (b.getText().toString().startsWith(text)) {
                        result.add((T) child);
                    }
                }
            }
        }

    }

    protected void callClick(View view) {
        if (view != null) {
            try {
                view.callOnClick();
            } catch (Exception ignored) {
            }
        }
    }

    public void realClick(View view, float x, float y) {
        if (view == null) {
            logOnThread("模拟点击对象不能为空！");
            return;
        }
        try {
            logOnThread("模拟点击！at:" + view.getClass().getSimpleName() + " x:" + x + " y:" + y);
            long downTime = SystemClock.uptimeMillis();
            long eventTime = SystemClock.uptimeMillis() + 100;
            int metaState = 0;
            MotionEvent motionEvent = MotionEvent.obtain(downTime, eventTime,
                    MotionEvent.ACTION_DOWN, x, y, metaState);
            view.dispatchTouchEvent(motionEvent);
            MotionEvent upEvent = MotionEvent.obtain(downTime + 1000, eventTime + 1000,
                    MotionEvent.ACTION_UP, x, y, metaState);
            view.dispatchTouchEvent(upEvent);
        } catch (Exception ignored) {
        }
    }


    public void reEntry() {
        logOnThread("重新进入页面");
        if (entryIntent == null) {
            logOnThread("错误：entryIntent为空！");
            return;
        }
        activity.finish();
        activity.startActivity(entryIntent);
    }


    public void exit() {
        logOnThread("退出页面");
        activity.finish();
    }

    public void log(String str) {
        if (consoleTv != null) {
            consoleTv.append(logLineNum.addAndGet(1) + " " + str + "\n");
        }
        if (scrollView != null) {
            if (isBottom.isChecked()) {
                scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
            }
        }
    }

    public void logOnThread(String str) {
        activity.runOnUiThread(() -> log(str));
    }
}
