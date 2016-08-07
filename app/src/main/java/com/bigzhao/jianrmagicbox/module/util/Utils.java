package com.bigzhao.jianrmagicbox.module.util;

import com.bigzhao.jianrmagicbox.util.IOUtils;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.GZIPOutputStream;

/**
 * Created by Roy on 16-8-2.
 */
public class Utils extends IOUtils{
    public static byte[] gzip(byte[] bs){
        ByteArrayOutputStream os=null;
        GZIPOutputStream gos=null;
        try{
            os=new ByteArrayOutputStream();
            gos=new GZIPOutputStream(os);
            gos.write(bs);
            gos.finish();
            return os.toByteArray();
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            closeQuietly(gos);
            closeQuietly(os);

        }
        return bs;
    }

    public static HashMap<String,Object> jsonToMap(JSONObject obj){
        Iterator<String> iter=obj.keys();
        HashMap<String,Object> map=new HashMap<String, Object>();
        while(iter.hasNext()){
            String key=iter.next();
            Object val=obj.opt(key);
            map.put(key,val);
        }
        return map;
    }

    public static final byte[] SECRET="fdE%$GDgd5&^$#dIK* (EFcvc这是一串秘钥。%$gdf4滚键盘打出来的%$TDR4".getBytes();

    public static byte[] encrypt(byte[] bs){
        byte[] ret=new byte[bs.length];
        for (int i=0;i<bs.length;++i){
            ret[i]=(byte)(bs[i]^SECRET[i%SECRET.length]);
        }
        return ret;
    }


}
