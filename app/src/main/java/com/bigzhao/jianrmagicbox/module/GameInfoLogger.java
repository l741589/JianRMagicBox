package com.bigzhao.jianrmagicbox.module;

import android.os.Build;
import android.text.TextUtils;
import android.text.format.DateFormat;

import com.bigzhao.jianrmagicbox.App;
import com.bigzhao.jianrmagicbox.MagicBox;
import com.bigzhao.jianrmagicbox.MagicBoxBinder;
import com.bigzhao.jianrmagicbox.module.errorlog.ErrorHandler;
import com.bigzhao.jianrmagicbox.module.net.NetManager;
import com.bigzhao.jianrmagicbox.module.net.Request;
import com.bigzhao.jianrmagicbox.module.net.Response;
import com.bigzhao.jianrmagicbox.module.net.ResultCallback;
import com.bigzhao.jianrmagicbox.util.IOUtils;
import com.bigzhao.jianrmagicbox.util.V;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Roy on 16-7-31.
 */
public class GameInfoLogger {


    public static JSONObject createBaseLog() throws Exception {
        JSONObject json = new JSONObject();
        json.put("sdk_int", Build.VERSION.SDK_INT);
        json.put("sdk_ver",Build.VERSION.RELEASE);
        json.put("model",Build.MODEL);
        json.put("device_id", MagicBox.getDeviceId());
        json.put("client_time", DateFormat.format("yy-MM-dd HH:mm:ss", System.currentTimeMillis()));
        json.put("stub_ver",MagicBox.versionString((Integer)V.class.getField("STUB").get(null)));
        json.put("game_ver",MagicBox.versionString(V.GAME));
        MagicBoxBinder b=MagicBox.safeGetBinder();
        json.put("binder_ver",MagicBox.versionString(b==null?0:b.getVersion()));
        return json;
    }

    public static void log(String userInfo,String type,String url,String message){
        try {
            JSONObject ui=new JSONObject(userInfo);
            final JSONObject json = createBaseLog();
            json.put("uid", ui.optString("uid"));
            json.put("username", ui.optString("name"));
            json.put("server", ui.optString("server"));
            if (message != null) json.put("message", message);
            json.put("type", type);
            json.put("url", url);
            json.put("retry",0);
            startRequest(json);
        }catch (Exception e){
            ErrorHandler.log(e);
        }
    }

    private static final ReadWriteLock gameInfoWriteLock= new ReentrantReadWriteLock();

    private static void startRequest(final JSONObject json) {
        Request.create()
                //.setMock(json.optInt("retry",0)==0?new Response():null)
                .setPath("/ClientStub/logGameInfo.do")
                .setBody(json.toString())
                .setGzip(true)
                .setAsync(true)
                .setCallback(new ResultCallback() {
                    @Override
                    public void onResult(Response result) throws Throwable {
                        if (result != null && result.isSuccess()) return;
                        MagicBoxBinder b = MagicBox.getBinder();
                        gameInfoWriteLock.writeLock().lock();
                        FileOutputStream ouf = new FileOutputStream((File) b.action("getFile", "files:MagicBox/failInfo"), true);
                        ouf.write(json.toString().getBytes());
                        ouf.write("\r\n".getBytes());
                        IOUtils.closeQuietly(ouf);
                        gameInfoWriteLock.writeLock().unlock();
                    }
                }).post();
    }

    public static void retry(MagicBoxBinder binder) throws Exception {
        if (binder==null) binder=MagicBox.getBinder();
        File f=(File) binder.action("getFile", "files:MagicBox/failInfo");
        if (!f.exists()) return;
        gameInfoWriteLock.readLock().lock();
        String infos = IOUtils.readString(f);
        gameInfoWriteLock.readLock().unlock();
        try {
            if (!f.delete()) return;
            String[] lines = infos.split("[\\r\\n]+");
            for (String line : lines) {
                if (TextUtils.isEmpty(line)) continue;
                JSONObject json=new JSONObject(line);
                int retry=json.optInt("retry",0);
                json.put("retry",retry+1);
                startRequest(json);
            }
        }catch (Throwable e) {
            ErrorHandler.log(e);
            gameInfoWriteLock.writeLock().lock();
            FileOutputStream ouf = new FileOutputStream(f, true);
            ouf.write(infos.getBytes());
            IOUtils.closeQuietly(ouf);
            gameInfoWriteLock.writeLock().unlock();
        }
    }
}
