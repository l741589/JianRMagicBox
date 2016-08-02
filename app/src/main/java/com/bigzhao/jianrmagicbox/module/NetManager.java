package com.bigzhao.jianrmagicbox.module;


import com.bigzhao.jianrmagicbox.MagicBox;
import com.bigzhao.jianrmagicbox.errorlog.MessageQueue;
import com.bigzhao.jianrmagicbox.module.errorlog.ErrorHandler;
import com.bigzhao.jianrmagicbox.util.IOUtils;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Roy on 16-7-31.
 */
public class NetManager {

    public interface StringResultCallback{
        void onResult(String result);
        StringResultCallback Null=new StringResultCallback() {
            @Override
            public void onResult(String result) {

            }
        };
    }

    private static MessageQueue mq=new MessageQueue();

    public static void post(final String path,final String body,final StringResultCallback callback){
        mq.post(new Runnable() {
            @Override
            public void run() {
                String result=post(path,body);
                if (callback!=null) callback.onResult(result);
            }
        });
    }

    public static String post(String path,String body){
        try {
            return post(path,"text/plain; charset=UTF-8",body.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            ErrorHandler.log(e);
            e.printStackTrace();
        }
        return null;
    }

    public static String post(String path, String contentType, byte[] body) {
        if (path.startsWith("/")) path=path.substring(1);
        for (String server:MagicBox.serverList) {
            HttpURLConnection conn = null;
            try {
                String urlStr = String.format("http://%s/%s", server,path);
                MagicBox.logi("POST:" + urlStr);
                URL url = new URL(urlStr);
                conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(10000);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", contentType);
                conn.connect();
                OutputStream os = conn.getOutputStream();
                os.write(body);
                IOUtils.closeQuietly(os);
                InputStream is = conn.getInputStream();
                String s = IOUtils.readString(is);
                IOUtils.closeQuietly(is);
                return s;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (conn!=null) conn.disconnect();
            }
        }
        return null;
    }

    public static String get(String path) {
        for (String server:MagicBox.serverList) {
            HttpURLConnection conn = null;
            try {
                String urlStr = String.format("http://%s/%s", server,path);
                MagicBox.logi("GET:" + urlStr);
                URL url = new URL(urlStr);
                conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("GET");
                conn.connect();
                InputStream is = conn.getInputStream();
                String s = IOUtils.readString(is);
                IOUtils.closeQuietly(is);
                return s;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (conn!=null) ((HttpURLConnection) conn).disconnect();
            }
        }
        return null;
    }
}
