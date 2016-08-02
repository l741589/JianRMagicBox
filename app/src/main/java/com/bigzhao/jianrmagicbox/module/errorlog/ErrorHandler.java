package com.bigzhao.jianrmagicbox.module.errorlog;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.text.format.DateFormat;
import android.widget.Toast;

import com.bigzhao.jianrmagicbox.App;
import com.bigzhao.jianrmagicbox.MagicBox;
import com.bigzhao.jianrmagicbox.MagicBoxBinder;
import com.bigzhao.jianrmagicbox.errorlog.Logger;
import com.bigzhao.jianrmagicbox.util.V;

import org.json.JSONException;
import org.json.JSONObject;

import com.bigzhao.jianrmagicbox.errorlog.ErrorHandler.Level;
/**
 * Created by Roy on 16-7-9.
 */
public class ErrorHandler {
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
