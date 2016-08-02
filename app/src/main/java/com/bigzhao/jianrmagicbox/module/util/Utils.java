package com.bigzhao.jianrmagicbox.module.util;

import com.bigzhao.jianrmagicbox.util.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
}
