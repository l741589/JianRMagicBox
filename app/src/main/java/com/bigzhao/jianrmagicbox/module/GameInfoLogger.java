package com.bigzhao.jianrmagicbox.module;

import android.os.Build;
import android.text.format.DateFormat;

import com.bigzhao.jianrmagicbox.MagicBox;
import com.bigzhao.jianrmagicbox.MagicBoxBinder;
import com.bigzhao.jianrmagicbox.module.errorlog.ErrorHandler;
import com.bigzhao.jianrmagicbox.module.net.NetManager;
import com.bigzhao.jianrmagicbox.module.net.Request;
import com.bigzhao.jianrmagicbox.util.V;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Roy on 16-7-31.
 */
public class GameInfoLogger {


    public static JSONObject createBaseLog() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("sdk_int", Build.VERSION.SDK_INT);
        json.put("sdk_ver",Build.VERSION.RELEASE);
        json.put("model",Build.MODEL);
        json.put("device_id", MagicBox.getDeviceId());
        json.put("client_time", DateFormat.format("yy-MM-dd HH:mm:ss", System.currentTimeMillis()));
        json.put("stub_ver",MagicBox.versionString(V.STUB));
        json.put("game_ver",MagicBox.versionString(V.GAME));
        MagicBoxBinder b=MagicBox.safeGetBinder();
        json.put("binder_ver",MagicBox.versionString(b==null?0:b.getVersion()));
        return json;
    }

    public static void log(String uid,String type,String url,String message){
        try {
            JSONObject json = createBaseLog();
            json.put("uid", uid);
            if (message != null) json.put("message", message);
            json.put("type", type);
            json.put("url", url);
            Request.create()
                    .setPath("/ClientStub/logGameInfo.do")
                    .setBody(json.toString())
                    .setGzip(true)
                    .setAsync(true)
                    .post();
        }catch (Exception e){
            ErrorHandler.log(e);
        }
    }
}
