package com.bigzhao.jianrmagicbox;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Roy on 16-6-15.
 */
public class UpdateManager extends AsyncTask<Object,Object,Object>{

    private Context context;

    private final int forVersion=0x02040000;
    private final int stubVersion=0x01000100;

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
            e.printStackTrace();
            return null;
        } finally{
            IOUtils.closeQuietly(is);
        }
    }
    public String moreServerList[]={};
    public String serverList[]={
        "jianr.bigzhao.com",
        "jianr.bigzhao.com:8080",
        "www.yutou233.cn:3000",
        "jianr.yutou233.cn",
    };

    @Override
    protected Object doInBackground(Object... params) {
        InputStream is=null;
        try{
            MagicBoxBinder binder=MagicBox.getBinder(context);
            String s=null;
            moreServerList=binder.moreServerList();
            String appendArgs=binder.getVersionMoreArgs();
            if (moreServerList!=null) {
                for (String server : moreServerList) {
                    s = getString(server);
                    if (!TextUtils.isEmpty(s)) break;
                }
            }
            if (TextUtils.isEmpty(s)) {
                for (String server : serverList) {
                    s = getString(server);
                    if (!TextUtils.isEmpty(s)) break;
                }
            }
            MagicBox.logi("response: "+s);
            if (TextUtils.isEmpty(s)) return null;
            JSONObject json=new JSONObject(s);
            int newVersion=json.optInt("version");
            if (newVersion==0||newVersion<=binder.getVersion()) return null;
            int forV=json.optInt("for",0);
            if (forV!=0&&forV!=forVersion) return null;
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
                MagicBox.getBinder(context).action("onModuleUpdated", module.getCanonicalPath());
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    private String getString(String server) {
        MagicBoxBinder binder=MagicBox.getBinder(context);
        int version=binder.getVersion();
        String appendArgs = binder.getVersionMoreArgs();
        String s;
        String location = String.format("http://%s/ClientStub/checkVersion.do?v=%d&for=%d&stub=%d", server, version, forVersion, stubVersion);
        location+=appendArgs;
        MagicBox.logi("request: " + location);
        s = readString(location);
        return s;
    }
}
