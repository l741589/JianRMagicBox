package com.bigzhao.jianrmagicbox.module.errorlog;

import com.bigzhao.jianrmagicbox.errorlog.MessageQueue;
import com.bigzhao.jianrmagicbox.module.net.NetManager;
import com.bigzhao.jianrmagicbox.module.net.Request;

import org.json.JSONObject;

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
                Request.create().setPath("/ClientStub/logError.do").setBody(obj.toString()).post();
                if (onFinish!=null) onFinish.run();
            }
        });
    }

    public static Logger getInstance() {
        return instance;
    }
}
