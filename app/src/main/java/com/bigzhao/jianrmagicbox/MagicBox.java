package com.bigzhao.jianrmagicbox;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.bigzhao.jianrmagicbox.defaultmodule.DefaultActivityImpl;
import com.bigzhao.jianrmagicbox.defaultmodule.DefaultBinderImpl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import dalvik.system.DexClassLoader;

/**
 * Created by Roy on 16-6-12.
 */
public class MagicBox {
    String ACTION_SERVICE="com.bigzhao.jianrmagicbox.action.SERVICE";
    String ACTION_RECEIVER="com.bigzhao.jianrmagicbox.action.RECEIVER";

    public static File FILES_DIR;

    private static MagicBoxBinder magicBoxBinder;
    private static IActivity activity;
    private static ClassLoader classLoader;
    public static Context application;
    private static boolean initialized=false;

    public static IActivity getActivityDelegate(Activity context){
        try {
            init(context);
            if (activity==null) activity=(IActivity)classLoader.loadClass("com.bigzhao.jianrmagicbox.module.ActivityImpl").getConstructor(Activity.class).newInstance(context);
            return activity;
        }catch (Exception e){
            e.printStackTrace();
            return activity=new DefaultActivityImpl();
        }finally {
            MagicBox.logi("get IActivity:"+ activity);
        }
    }
    public static MagicBoxBinder getBinder(Context context){
        try {
            init(context);
            if (magicBoxBinder==null) magicBoxBinder=(MagicBoxBinder)classLoader.loadClass("com.bigzhao.jianrmagicbox.module.BinderImpl").getConstructor(Context.class).newInstance(context);
            return magicBoxBinder;
        }catch (Throwable e){
            e.printStackTrace();
            return magicBoxBinder=new DefaultBinderImpl(context);
        }finally {
            MagicBox.logi("get IMagicBoxBinder:"+ magicBoxBinder);
        }
    }

    public static void logi(String s){
        Log.i("MagicBox",s);
    }

    public static void logi(String fmt,Object...args){
        Log.i("MagicBox", String.format(fmt, args));
    }

    private static void createClassLoader() throws IOException {
        if (application.getClassLoader()!=classLoader) return;
        MagicBox.logi("Creating ClassLoader");
        File dex=new File(FILES_DIR,"MagicBox/module/classes.dex");
        if (!dex.exists()) return;
        File dexPath=dex.getParentFile();
        File dexoptPath=new File(dexPath,"opt");
        if (!dexoptPath.exists()) dexoptPath.mkdirs();
        MagicBox.logi("SysClassLoader:"+application.getClassLoader());
        classLoader=new FakeClassLoader(dex.getCanonicalPath(),
                dexoptPath.getCanonicalPath(),
                dexPath.getCanonicalPath(),
                application.getClassLoader());
        MagicBox.logi("ClassLoader Created");
    }

    public static void loadSo(String path){
        System.load(path);
    }

    private static void init(Context context){
        if (initialized) return;
        initialized=true;
        try {
            initModule(context);
            createClassLoader();
        }catch (Exception e){
            e.printStackTrace();
        }
        new UpdateManager(context).execute();
    }

    private static void initModule(Context context) throws IOException {
        InputStream is = null;
        try {
            application = context.getApplicationContext();
            classLoader = context.getClassLoader();
            MagicBox.FILES_DIR = context.getFilesDir();
            File module = new File(context.getFilesDir(), "MagicBox/module");
            File module_new = new File(context.getFilesDir(), "MagicBox/module.new");
            if (module_new.exists()) {
                IOUtils.delDir(module);
                if (!module_new.renameTo(module)) return;
            }

            if (module.exists()||!module.mkdirs()) return;

            is = application.getAssets().open("MagicBox/module/libMagicBox.so");
            byte[] bs = IOUtils.readBytes(is);
            IOUtils.closeQuietly(is);
            IOUtils.writeBytes(new File(module, "libMagicBox.so"), bs);

            is = application.getAssets().open("MagicBox/module/classes.dex");
            bs = IOUtils.readBytes(is);
            IOUtils.closeQuietly(is);
            IOUtils.writeBytes(new File(module, "classes.dex"), bs);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    public static boolean isWifiConnected()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)application.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifiNetworkInfo.isConnected();
    }
}
