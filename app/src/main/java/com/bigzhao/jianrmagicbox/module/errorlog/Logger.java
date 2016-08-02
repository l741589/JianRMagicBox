package com.bigzhao.jianrmagicbox.module.errorlog;

import com.bigzhao.jianrmagicbox.errorlog.MessageQueue;
import com.bigzhao.jianrmagicbox.module.NetManager;
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

    public void log(final JSONObject obj) {
        log(obj,null);
    }

    public void log(final JSONObject obj, final Runnable onFinish) {
        mq.post(new Runnable() {
            @Override
            public void run() {
                NetManager.post("/ClientStub/logError.do",obj.toString());
                if (onFinish!=null) onFinish.run();
            }
        });
    }

    public static Logger getInstance() {
        return instance;
    }
}
