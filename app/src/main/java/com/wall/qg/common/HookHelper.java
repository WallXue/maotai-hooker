package com.wall.qg.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class HookHelper {

    private static final String XPOSED_BRIDGE = "de.robv.android.xposed.XposedBridge";

    public static void hiddenXposedBridge() {
        XposedHelpers.findAndHookMethod(ClassLoader.class, "loadClass", String.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (param.hasThrowable()) return;
                        Class<?> cls = (Class<?>) param.getResult();
                        if (cls != null && cls.getName().contains(XPOSED_BRIDGE)) {
                            XposedBridge.log("111111try to load " + XPOSED_BRIDGE);
                            param.setResult(null);
                        }
                    }
                });
    }

    public static void hiddenJDXposedTip(ClassLoader appClassLoader) {
        XposedHelpers.findAndHookMethod("com.jd.stat.security.jma.JMA",
                appClassLoader, "needXposedDialog", new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                        XposedBridge.log("去除xposed提示");
                        return false;
                    }
                });
    }

    public static void showActivityNameHelper(ClassLoader appClassLoader) {
        XposedHelpers.findAndHookMethod(Activity.class.getName(), appClassLoader,
                "onCreate", Bundle.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                        Activity activity = (Activity) param.thisObject;
                        HookHelper.toastActivity(activity, "activity:" + activity);
                    }
                });
    }


    public static void toastActivity(Context activity, String text) {
//        Toast toast = Toast.makeText(activity, text, Toast.LENGTH_LONG);
//        toast.setGravity(Gravity.BOTTOM, 0, 0);
//        toast.show();

        AlertDialog.Builder alertdialogbuilder = new AlertDialog.Builder(activity);
        alertdialogbuilder.setMessage(text);
        alertdialogbuilder.setPositiveButton("确定", null);
        alertdialogbuilder.setNeutralButton("取消", null);
        final AlertDialog alertdialog1 = alertdialogbuilder.create();
        alertdialog1.show();
    }

    public static Object getFieldValueByName(Object obj, String name) {
        try {
            Field field = getAllFiled(obj.getClass()).stream().filter(f -> f.getName().equals(name)).findFirst().orElseThrow(NullPointerException::new);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object getFieldValueByTypeName(Object obj, String typeName) {
        try {
            Field field = getAllFiled(obj.getClass()).stream().filter(f -> f.getType().getSimpleName().equals(typeName)).findFirst().orElseThrow(NullPointerException::new);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Field> getAllFiled(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        do {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        } while ((clazz = clazz.getSuperclass()) != null);
        return fields;
    }

    public static String deCodeClass(Class<?> clazz) {
        StringBuilder sb = new StringBuilder();
//        do {
        Method[] declaredMethods = clazz.getDeclaredMethods();
        sb.append(Arrays.stream(declaredMethods).map(m -> {
            Class<?>[] parameterTypes = m.getParameterTypes();
            String pa = "";
            if (parameterTypes != null) {
                pa = Arrays.stream(parameterTypes).map(Class::getSimpleName).collect(Collectors.joining(","));
            }
            return m.getReturnType().getSimpleName() + " " + m.getName() + " (" + pa + ")";
        }).collect(Collectors.joining("\n")));
//        } while ((clazz = clazz.getSuperclass()) != null);
        return sb.toString();
    }

    public static void sleepCurrentThread(int sleep) {
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
// 绕过加固
    //        XposedHelpers.findAndHookMethod("com.jingdong.app.mall.JDApp", lpparam.classLoader, "attachBaseContext", Context.class, new XC_MethodHook() {
//            //        XposedHelpers.findAndHookMethod(Application.class.getName(), lpparam.classLoader, "attach", Context.class, new XC_MethodHook() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                super.afterHookedMethod(param);
//                //获取到Context对象，通过这个对象来获取classloader
//                Context context = (Context) param.args[0];
//                //获取classloader，之后hook加固后的就使用这个classloader
//                ClassLoader classLoader = context.getClassLoader();
//
//
//            }
//        });

    public static int dip2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }

    public static int randomFloat(int val) {
        return new Random().nextInt(val) + 1;
    }
}
