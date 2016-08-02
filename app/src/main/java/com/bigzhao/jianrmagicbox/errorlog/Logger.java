package com.bigzhao.jianrmagicbox.errorlog;

import com.bigzhao.jianrmagicbox.util.IOUtils;
import com.bigzhao.jianrmagicbox.MagicBox;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Roy on 16-7-9.
 */
public class Logger {

    private static Logger instance = new Logger();
    private MessageQueue mq=new MessageQueue();

    private boolean doLog(String server, JSONObject obj) {
        URLConnection conn = null;
        try {
            String urlStr = String.format("http://%s/ClientStub/logError.do", server);
            String val=obj.toString();
            MagicBox.logi("do log:"+urlStr,val);
            URL url = new URL(urlStr);
            conn = url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(10000);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
            conn.connect();
            OutputStream os = conn.getOutputStream();
            os.write(val.getBytes("UTF-8"));
            IOUtils.closeQuietly(os);
            InputStream is = conn.getInputStream();
            String s = IOUtils.readString(is);
            IOUtils.closeQuietly(is);
            JSONObject ret = new JSONObject(s);
            return "true".equalsIgnoreCase(ret.optString("success"));
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (conn instanceof HttpURLConnection) ((HttpURLConnection) conn).disconnect();
        }
        return false;
    }

    public void log(final JSONObject obj) {
        log(obj,null);
    }

    public void log(final JSONObject obj, final Runnable onFinish) {
        mq.post(new Runnable() {
            @Override
            public void run() {
                for (String server : MagicBox.serverList) {
                    if (doLog(server, obj)) break;
                }
                if (onFinish!=null) onFinish.run();
            }
        });
    }

    public static Logger getInstance() {
        return instance;
    }
}
