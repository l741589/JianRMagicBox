package com.bigzhao.jianrmagicbox.defaultmodule;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.bigzhao.jianrmagicbox.util.IOUtils;
import com.bigzhao.jianrmagicbox.MagicBox;
import com.bigzhao.jianrmagicbox.MagicBoxBinder;
import com.bigzhao.jianrmagicbox.errorlog.ErrorHandler;
import com.bigzhao.jianrmagicbox.errorlog.SelfCheck;
import com.bigzhao.jianrmagicbox.util.V;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by Roy on 16-6-15.
 */
public class DefaultBinderImpl extends MagicBoxBinder{

    protected Context context;

    public DefaultBinderImpl(Context context){
        this.context= context;
    }

    @Override
    public int getVersion() {
        return V.BASE;
    }

    public File getFile(String path){
        if (path.startsWith("/")) return new File(path);
        else if (path.contains(":")) {
            int i=path.indexOf(':');
            String schema=path.substring(0,i);
            path=path.substring(i + 1);
            path=path.replaceAll("^/*","");
            if ("file".equals(schema)) return new File("/"+path);
            else if ("magicbox".equalsIgnoreCase(schema)) return new File(getWorkingDir(),path);
            else if ("inner_magicbox".equalsIgnoreCase(schema)) return new File(getInnerWorkingDir(),path);
            else if ("inner".equalsIgnoreCase(schema)) return new File(context.getFilesDir().getParentFile(),path);
            else if ("files".equalsIgnoreCase(schema)) return new File(context.getFilesDir(),path);
            else if ("cache".equalsIgnoreCase(schema)) return new File(context.getCacheDir(),path);
            else if ("sdcard".equalsIgnoreCase(schema)) return new File(Environment.getExternalStorageDirectory(),path);
            else if ("tmp".equalsIgnoreCase(schema)) return new File("/data/local/tmp",path);
            else if ("ext_files".equalsIgnoreCase(schema)) return new File(context.getExternalFilesDir(null),path);
            else if ("ext_cache".equalsIgnoreCase(schema)) return new File(context.getExternalCacheDir(),path);
            return new File(getWorkingDir(),path.substring(1));
        }else return new File(getWorkingDir(), path);
    }

    private void makeParents(File f) throws FileNotFoundException {
        if (!f.getParentFile().exists() && !(f.getParentFile().mkdirs()))
            mkdirFail(f.getParentFile());
    }

    public void del(String file) throws IOException {
        File f = getFile(file);
        MagicBox.logi("del "+f.getAbsolutePath());
        if (f.exists()) {
            if (IOUtils.delDir(f)) MagicBox.logi("del success");
            else {
                MagicBox.logi("del failed");
                throw new IOException("delete " + file + " failed");
            }
        } else {
            MagicBox.logi("del:not exist");
        }
    }
    public void copy(String src, String dest) throws IOException {
        File inf = getFile(src);
        File ouf= getFile(dest);
        MagicBox.logi("copy %s -> %s",inf.getAbsoluteFile(), ouf.getAbsolutePath());
        makeParents(ouf);
        byte[] bs = IOUtils.readBytes(inf);
        IOUtils.writeBytes(ouf, bs);
        MagicBox.logi("copy success");
    }

    @Override
    public Object action(String action, String... args) throws Exception {
        MagicBox.logi("%s(%s)", action, TextUtils.join(", ", args));
        if ("copy".equalsIgnoreCase(action)) {
            copy(args[0], args[1]);
        } else if ("del".equalsIgnoreCase(action)||"delete".equalsIgnoreCase(action)) {
            del(args[0]);
        } else if ("getFile".equalsIgnoreCase(action)) {
            return getFile(args[0]);
        } else if ("getFilePath".equalsIgnoreCase(action)) {
            File f=getFile(args[0]);
            return f.exists()?f.getCanonicalPath():f.getAbsolutePath();
        } else if ("list".equalsIgnoreCase(action)) {
            return list(args[0]);
        } else if ("version".equalsIgnoreCase(action)) {
            MagicBox.logi(Integer.toHexString(getVersion()));
        } else if ("selfcheck".equalsIgnoreCase(action)) {
            String s=SelfCheck.run(args[0], Arrays.copyOfRange(args,1,args.length));
            ErrorHandler.log(s);
            MagicBox.logi(s);
            return s;
        }
        return null;
    }

    public File[] list(String dir) {
        File d = getFile(dir);
        MagicBox.logi("list: " + d.getAbsolutePath());
        if (!d.exists()) {
            MagicBox.logi("list: not exist");
            return null;
        } else if (!d.isDirectory()) {
            MagicBox.logi("list: not a dir");
            return null;
        } else {
            File[] fs = d.listFiles();
            for (File f : fs) {
                MagicBox.logi("%s\t%s", f.isDirectory() ? "D" : "F", f.getName());
            }
            return fs;
        }
    }

    public File getInnerWorkingDir() {
        File f  = new File(context.getFilesDir(), "MagicBox");
        if (f.exists() || f.mkdirs()) return f;
        return null;
    }

    public File getWorkingDir() {
        File f = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED);
        if (sdCardExist) f = context.getExternalFilesDir("MagicBox");
        if (f == null) f = new File(context.getFilesDir(), "MagicBox");
        if (f.exists() || f.mkdirs()) return f;
        return null;
    }

    public void mkdirFail(File dir) throws FileNotFoundException {
        throw new FileNotFoundException("create directory " + dir.getAbsolutePath() + " failed");
    }
}
