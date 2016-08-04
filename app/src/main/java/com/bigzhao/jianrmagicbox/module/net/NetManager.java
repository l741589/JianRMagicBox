package com.bigzhao.jianrmagicbox.module.net;


import android.content.res.Resources;

import com.bigzhao.jianrmagicbox.MagicBox;
import com.bigzhao.jianrmagicbox.errorlog.MessageQueue;
import com.bigzhao.jianrmagicbox.module.errorlog.ErrorHandler;
import com.bigzhao.jianrmagicbox.module.util.Utils;
import com.bigzhao.jianrmagicbox.util.IOUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 * Created by Roy on 16-7-31.
 */
public class NetManager {


    private static MessageQueue mq=new MessageQueue();

    public static Response request(final Request req){
        if (req.isAsync()){
            mq.post(new Runnable() {
                @Override
                public void run() {
                    Request req2 = req.clone();
                    req2.setAsync(false);
                    Response bs=requestSync(req2);
                    if (req.getCallback()!=null) req.getCallback().onResult(bs);
                }
            });
            return null;
        }else {
            return requestSync(req);
        }
    }

    private static Response requestSync(Request req) {
        if (req.getPath().startsWith("/")) req.setPath(req.getPath().substring(1));
        boolean hasBody="POST".equalsIgnoreCase(req.getMethod())||"PUT".equalsIgnoreCase(req.getMethod());
        for (String server:req.getHosts()) {
            HttpURLConnection conn = null;
            InputStream is=null;
            OutputStream os=null;
            GZIPOutputStream gos=null;
            try {
                String urlStr = String.format("%s://%s/%s", req.getSchema(),server,req.getPath());
                if (!hasBody){
                    String q=req.generateQuery();
                    if (q!=null) urlStr+="?"+q;
                }
                MagicBox.logi(req.getMethod()+":" + urlStr);
                URL url = new URL(urlStr);
                conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(10000);
                for (Map.Entry<String,String> p:req.getHeaders().entrySet()) conn.setRequestProperty(p.getKey(), p.getValue());
                if (hasBody){
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                }
                conn.setRequestMethod(req.getMethod().toUpperCase());
                conn.connect();
                if (hasBody) {
                    if (req.getBody()==null){
                        String q=req.generateQuery();
                        req.setBody(q.getBytes(req.getEncoding()));
                    }
                    os = conn.getOutputStream();
                    if (req.isGzip()){
                        gos=new GZIPOutputStream(os);
                        gos.write(req.getBody());
                        gos.finish();
                    }else {
                        os.write(req.getBody());
                    }
                    IOUtils.closeQuietly(os);
                }
                is = conn.getInputStream();
                byte[] bs=IOUtils.readBytes(is);
                Response res=new Response();
                res.data=bs;
                res.request=req;
                return res;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (conn!=null) conn.disconnect();
                IOUtils.closeQuietly(is);
                IOUtils.closeQuietly(os);
                IOUtils.closeQuietly(gos);
            }
        }
        return null;
    }


}
