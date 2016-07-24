package com.bigzhao.jianrmagicbox.util;

import com.bigzhao.jianrmagicbox.errorlog.ErrorHandler;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by Roy on 16-4-27.
 */
public class IOUtils {

    public static void closeQuietly(Closeable closeable){
        try {
            if (closeable != null) closeable.close();
        }catch (IOException e){
            ErrorHandler.log(e);
        }
    }
    public static byte[] readBytes(InputStream is) throws IOException {
        ByteArrayOutputStream os=new ByteArrayOutputStream();
        try {
            byte[] bs = new byte[4096];
            while (true) {
                int l=is.read(bs);
                if (l==-1) break;
                os.write(bs,0,l);
            }
            return os.toByteArray();
        }finally {
            closeQuietly(os);
        }
    }

    public static byte[] readBytes(File is) throws IOException {
        FileInputStream fs=null;
        try {
            fs = new FileInputStream(is);
            return readBytes(fs);
        }finally {
            closeQuietly(fs);
        }
    }

    public static String readString(File is) throws IOException{
        return new String(readBytes(is));
    }

    public static String readString(InputStream is) throws IOException {
        return new String(readBytes(is));
    }

    public static void writeBytes(File f,byte[]bs) throws IOException {
        FileOutputStream os=null;
        try{
            os=new FileOutputStream(f);
            os.write(bs);
        }finally {
            closeQuietly(os);
        }
    }

    private static void zip(ZipOutputStream out,String dir,File f) throws IOException {
        if (f.isDirectory()){
            File[] fs=f.listFiles();
            String newDir=dir + f.getName() + "/";
            if (fs!=null) {
                for (File e:fs) zip(out, newDir, e);
            }
        }else{
            ZipEntry entry=new ZipEntry(dir+f.getName());
            out.putNextEntry(entry);
            out.write(readBytes(f));
        }
    }

    public static void zip(File in,File out) throws IOException {
        ZipOutputStream ouf=null;
        try{;
            ouf=new ZipOutputStream(new FileOutputStream(out));
            if (in.isDirectory()){
                File[] fs=in.listFiles();
                if (fs!=null) for (File e:fs) zip(ouf, "", e);
            }
        }finally {
            closeQuietly(ouf);
        }
    }

    public static void unzip(File in,File out) throws IOException{
        ZipInputStream is=null;
        try{
            is=new ZipInputStream(new FileInputStream(in));
            for (ZipEntry e=is.getNextEntry();e!=null;e=is.getNextEntry()){
                File f=new File(out,e.getName());
                if (!f.getParentFile().exists()) f.getParentFile().mkdirs();
                byte[] bs=readBytes(is);
                writeBytes(f,bs);
            }
        }finally {
            closeQuietly(is);
        }
    }

    public static String getExt(File f){
        String name=f.getName();
        int i=name.lastIndexOf('.');
        if (i==-1) return null;
        return name.substring(i);
    }

    public static boolean delDir(File f){
        if (f==null) return false;
        if (!f.exists()) return true;
        if (f.isDirectory()){
            File[] fs=f.listFiles();
            if (fs!=null){
                for(File e:fs) {
                    if (!delDir(e)) return false;
                }
            }
        }
        return f.delete();
    }
}
