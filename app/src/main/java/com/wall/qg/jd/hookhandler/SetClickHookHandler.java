package com.wall.qg.jd.hookhandler;

import android.view.View;
import android.widget.TextView;

import com.wall.qg.common.HookContext;

import de.robv.android.xposed.XC_MethodHook;

public class SetClickHookHandler extends XC_MethodHook {

    private HookContext hookContext;

    public SetClickHookHandler(HookContext hookContext) {
        this.hookContext = hookContext;
    }

    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        View b = (View) param.thisObject;
        final View.OnClickListener listener = (View.OnClickListener) param.args[0];
//        if (b instanceof TextView) {
//            TextView tv = (TextView) b;
//            String tagName = tv.getText().toString();
//            hookContext.log("set ---> " + tagName);
//        }

//        hookContext.log("set ---> " + b);
        View.OnClickListener newListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hookContext.log(v.getClass().getName() + ":Clicked!!!!!!!");
                listener.onClick(v);
            }
        };
        param.args[0] = newListener;
    }

}
