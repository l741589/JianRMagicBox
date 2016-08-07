package com.bigzhao.jianrmagicbox;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.bigzhao.jianrmagicbox.errorlog.ErrorHandler;
import com.bigzhao.jianrmagicbox.util.IOUtils;
import com.bigzhao.jianrmagicbox.util.V;

import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Roy on 16-6-15.
 */
public class UpdateManager extends AsyncTask<Object,Object,Object>{

    private Context context;

    public UpdateManager(Context context){
        this.context=context;
    }

    public String readString(String location){
        InputStream is=null;
        try {
            URL url = new URL(location);
            URLConnection conn=url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(10000);
            is = conn.getInputStream();
            return IOUtils.readString(is);
        }catch (Throwable e){
            ErrorHandler.log(e);
            return null;
        } finally{
            IOUtils.closeQuietly(is);
        }
    }



    @Override
    protected Object doInBackground(Object... params) {
        InputStream is=null;

        try{
            MagicBoxBinder binder=MagicBox.getBinder();
            String s=null;
            Iterable<String> serverList=MagicBox.getServerList();
            for (String server : serverList) {
                s = getString(server);
                if (!TextUtils.isEmpty(s)) break;
            }
            MagicBox.logi("response: "+s);
            if (TextUtils.isEmpty(s)) return null;
            JSONObject json=new JSONObject(s);
            int newVersion=json.optInt("version");
            if (newVersion==0||newVersion<=binder.getVersion()) return null;
            int forV=json.optInt("for",0);
            if (forV!=0&&forV!= V.GAME) return null;
            String downloadUrl=json.optString("url");
            Boolean wifiUpdate=json.optBoolean("wifiUpdate");
            if (!wifiUpdate||MagicBox.isWifiConnected()) {
                URL url = new URL(downloadUrl);
                File tmp = new File(context.getFilesDir(), "MagicBox/tmp.zip");
                File module = new File(context.getFilesDir(), "MagicBox/module.new");
                if (tmp.exists()) tmp.delete();
                is = url.openStream();
                IOUtils.writeBytes(tmp, IOUtils.readBytes(is));
                IOUtils.unzip(tmp, module);
                IOUtils.closeQuietly(is);
                MagicBox.logi("module updated");
                MagicBox.getBinder().action("onModuleUpdated", module.getCanonicalPath());
            }
            return null;
        } catch (Throwable e) {
            ErrorHandler.log(e);
            return null;
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    private String getString(String server) {
        MagicBoxBinder binder=MagicBox.getBinder();
        int version=binder.getVersion();
        String appendArgs = binder.getVersionMoreArgs();
        String s;
        String location = String.format("http://%s/ClientStub/checkVersion.do?v=%d&for=%d&stub=%d&imei=%s",
                server, version, V.GAME, V.STUB,MagicBox.getDeviceId());
        location+=appendArgs;
        MagicBox.logi("request: " + location);
        s = readString(location);
        return s;
    }
}
