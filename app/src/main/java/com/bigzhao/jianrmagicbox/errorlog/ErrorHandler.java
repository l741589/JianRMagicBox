package com.bigzhao.jianrmagicbox.errorlog;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.widget.Toast;

import com.bigzhao.jianrmagicbox.MagicBox;
import com.bigzhao.jianrmagicbox.MagicBoxBinder;
import com.bigzhao.jianrmagicbox.UpdateManager;
import com.bigzhao.jianrmagicbox.aidl.IMagicBoxBinder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.SynchronousQueue;

/**
 * Created by Roy on 16-7-9.
 */
public class ErrorHandler implements Thread.UncaughtExceptionHandler {

    private static WeakReference<Activity> currentActivity;
    private static Thread.UncaughtExceptionHandler defaultHandler;

    private static ErrorHandler instance;

    public static void init(Activity activity) {
        if (instance!=null) return;
        instance=new ErrorHandler();
        Application app=activity.getApplication();
        currentActivity=new WeakReference<Activity>(activity);
        app.registerActivityLifecycleCallbacks(callback);
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(instance);
    }

    public static Activity getCurrentActivity() {
        if (currentActivity==null) return null;
        return currentActivity.get();
    }

    public static enum Level{
        CAUGHT,CRASH,CUSTOM
    }

    public static JSONObject createLog(Level level,String message) throws JSONException {
        return createLog(level,message,Thread.currentThread());
    }

    public static JSONObject createLog(Level level,String message,Thread thread) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("sdk_int", Build.VERSION.SDK_INT);
        json.put("sdk_ver",Build.VERSION.RELEASE);
        json.put("model",Build.MODEL);
        json.put("device_id", MagicBox.getDeviceId());
        json.put("client_time", DateFormat.format("yy-MM-dd HH:mm:ss", System.currentTimeMillis()));
        json.put("thread",thread!=null?thread.toString():null);
        json.put("stub_ver",MagicBox.versionString(UpdateManager.stubVersion));
        json.put("game_ver",MagicBox.versionString(UpdateManager.forVersion));
        MagicBoxBinder b=MagicBox.safeGetBinder();
        json.put("binder_ver",MagicBox.versionString(b==null?0:b.getVersion()));
        if (message!=null) json.put("message",message);
        if (level!=null) json.put("level",level.name());
        return json;
    }

    @Override
    public void uncaughtException(final Thread thread,final Throwable ex) {
        try {
            JSONObject json=createLog(Level.CRASH, MagicBox.exceptionToString(ex),thread);
            Activity a=getCurrentActivity();
            if (a!=null) Toast.makeText(a,"未知异常："+(ex==null?"null":ex.getMessage())+"\n正在上报",Toast.LENGTH_LONG).show();
            Logger.getInstance().log(json, new Runnable() {
                @Override
                public void run() {
                    defaultHandler.uncaughtException(thread,ex);
                }
            });
        }catch (JSONException e){
            e.printStackTrace();
        }

    }

    public static void log(Throwable t){
        try {
            t.printStackTrace();
            JSONObject json = createLog(Level.CAUGHT, MagicBox.exceptionToString(t));
            Logger.getInstance().log(json);
        }catch (JSONException e){
            e.getCause();
        }
    }

    public static void log(String message){
        try {
            JSONObject json = createLog(Level.CUSTOM, message);
            Logger.getInstance().log(json);
        }catch (JSONException e){
            e.getCause();
        }
    }

    private static Application.ActivityLifecycleCallbacks callback=new Application.ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {
            currentActivity=new WeakReference<Activity>(activity);
            MagicBox.logi("activity start: "+activity);
        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    };
}
