package com.bigzhao.jianrmagicbox.errorlog;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.Toast;

import com.bigzhao.jianrmagicbox.App;
import com.bigzhao.jianrmagicbox.MagicBox;
import com.bigzhao.jianrmagicbox.MagicBoxBinder;
import com.bigzhao.jianrmagicbox.util.V;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

/**
 * Created by Roy on 16-7-9.
 */
public class ErrorHandler implements Thread.UncaughtExceptionHandler {


    private static Thread.UncaughtExceptionHandler defaultHandler;

    private static ErrorHandler instance;

    public static void init(Application application) {
        if (instance!=null) return;
        instance=new ErrorHandler();
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(instance);
    }

    public static enum Level{
        CAUGHT,CRASH,CUSTOM,NATIVE
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
        json.put("stub_ver",MagicBox.versionString(V.STUB));
        json.put("game_ver",MagicBox.versionString(V.GAME));
        MagicBoxBinder b=MagicBox.safeGetBinder();
        json.put("binder_ver",MagicBox.versionString(b==null?0:b.getVersion()));
        if (message!=null) json.put("message",message);
        if (level!=null) json.put("level",level.name());
        return json;
    }

    @Override
    public void uncaughtException(final Thread thread,final Throwable ex) {
        try {
            ex.printStackTrace();
            JSONObject json=createLog(Level.CRASH, MagicBox.exceptionToString(ex),thread);
            Activity a= App.getCurrentActivity();
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


    public static void logNative(String message) {
        try {
            JSONObject json = createLog(Level.NATIVE, message);
            Logger.getInstance().log(json);
        }catch (JSONException e){
            e.getCause();
        }
    }


}
