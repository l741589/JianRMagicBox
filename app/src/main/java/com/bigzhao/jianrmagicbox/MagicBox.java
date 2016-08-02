package com.bigzhao.jianrmagicbox;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.bigzhao.jianrmagicbox.defaultmodule.DefaultActivityImpl;
import com.bigzhao.jianrmagicbox.defaultmodule.DefaultBinderImpl;
import com.bigzhao.jianrmagicbox.errorlog.ErrorHandler;
import com.bigzhao.jianrmagicbox.util.IOUtils;
import com.bigzhao.jianrmagicbox.util.MergeIterable;
import com.bigzhao.jianrmagicbox.util.V;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

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
    private static boolean initialized=false;

    public static String serverList[]={
            //*
            "192.168.2.100:3000",/*/
            "jianr.bigzhao.com",
            "jianr.bigzhao.com:8080",
            "www.yutou233.cn:3000",
            "jianr.yutou233.cn",//*/
    };

    public static MagicBoxBinder safeGetBinder() {
        return magicBoxBinder;
    }

    public static IActivity getActivityDelegate(Activity context){
        try {
            init();
            if (activity==null) activity=(IActivity)classLoader.loadClass("com.bigzhao.jianrmagicbox.module.ActivityImpl").getConstructor(Activity.class).newInstance(context);
            return activity;
        }catch (Exception e){
            ErrorHandler.log(e);
            return activity=new DefaultActivityImpl(context);
        }finally {
            MagicBox.logi("get IActivity:"+ activity);
        }
    }
    public static MagicBoxBinder getBinder(){
        try {
            init();
            if (magicBoxBinder==null) magicBoxBinder=(MagicBoxBinder)classLoader.loadClass("com.bigzhao.jianrmagicbox.module.BinderImpl").getConstructor(Context.class).newInstance(App.getApplication());
            return magicBoxBinder;
        }catch (Throwable e){
            ErrorHandler.log(e);
            return magicBoxBinder=new DefaultBinderImpl(App.getApplication());
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
        if (App.getApplication().getClassLoader()!=classLoader) return;
        MagicBox.logi("Creating ClassLoader");
        File dex=new File(FILES_DIR,"MagicBox/module/classes.dex");
        if (!dex.exists()) return;
        File dexPath=dex.getParentFile();
        File dexoptPath=new File(dexPath,"opt");
        if (!dexoptPath.exists()) dexoptPath.mkdirs();
        MagicBox.logi("SysClassLoader:"+App.getApplication().getClassLoader());
        classLoader=new FakeClassLoader(dex.getCanonicalPath(),
                dexoptPath.getCanonicalPath(),
                dexPath.getCanonicalPath(),
                App.getApplication().getClassLoader());
        MagicBox.logi("ClassLoader Created");
    }

    public static void loadSo(String path){
        System.load(path);
    }

    public static void forceInit(){
        initialized=false;
        init();;
    }

    private static void init(){
        if (initialized) return;
        initialized=true;
        try {
            initModule();
            createClassLoader();
        }catch (Exception e){
            ErrorHandler.log(e);
        }
        new UpdateManager(App.getApplication()).execute();
    }

    private static void initModule() throws IOException, JSONException {
        InputStream is = null;
        try {
            Application app=App.getApplication();
            classLoader = app.getClassLoader();
            MagicBox.FILES_DIR = app.getFilesDir();
            File module = new File(app.getFilesDir(), "MagicBox/module");
            File module_new = new File(app.getFilesDir(), "MagicBox/module.new");
            File versionStub=new File(module,versionString(V.STUB));
            if (module_new.exists()) {
                IOUtils.delDir(module);
                if (!module_new.renameTo(module)){
                    throw new IOException("Rename directory '"
                            +module_new.getAbsolutePath()+"' to '"+module.getAbsolutePath()+"'failed");
                }
                if (!versionStub.createNewFile()) throw new IOException("Create versionStub file failed");
            }

            if (module.exists()&&versionStub.exists()) return;

            if (module.exists()){
                if (!IOUtils.delDir(module))
                    throw new IOException("Delete directory '"+module.getAbsolutePath()+"' failed");
            }
            if (!module.mkdirs()) {
                throw new IOException("Create directory '"+module.getAbsolutePath()+"' failed");
            }

            MagicBox.logi("initialize module");
            is = app.getAssets().open("MagicBox/module/manifest.json");
            String jsonStr = IOUtils.readString(is);
            IOUtils.closeQuietly(is);
            JSONObject json=new JSONObject(jsonStr);
            JSONArray deploy=json.optJSONArray("deploy");
            for (int i=0;i<deploy.length();++i){
                JSONObject c=deploy.getJSONObject(i);
                String action=c.getString("action");
                if ("copyModule".equals(action)){
                    is = app.getAssets().open(c.getString("from"));
                    byte[] bs = IOUtils.readBytes(is);
                    IOUtils.closeQuietly(is);
                    IOUtils.writeBytes(new File(module, c.getString("to")), bs);
                }
            }
            if (!versionStub.createNewFile()) throw new IOException("Create versionStub file failed");
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    @SuppressWarnings("deprecation")
    public static boolean isWifiConnected()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)App.getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifiNetworkInfo.isConnected();
    }

    public static String getDeviceId(){
        try {
            if (App.getApplication() == null) return null;
            return ((TelephonyManager) App.getApplication().getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        }catch (Throwable e){
            e.printStackTrace();
            return "getDeviceIdFailed";
        }
    }

    public static String exceptionToString(Throwable t){
        ByteArrayOutputStream os=new ByteArrayOutputStream();
        PrintStream ps=new PrintStream(os);
        ps.println(t.getMessage());
        while(t!=null){
            t.printStackTrace(ps);
            if (t.getCause()==t) {
                ps.println("...");
                break;
            }
            t=t.getCause();
        }
        String s=os.toString();
        IOUtils.closeQuietly(os);
        IOUtils.closeQuietly(ps);
        return s;
    }

    public static Iterable<String> getServerList(){
        return new MergeIterable<String>(MagicBox.getBinder().moreServerList(),MagicBox.serverList);
    }

    public static String versionString(int version){
        return String.format("%d.%d.%d.%d",
                version/(1<<24)%256,
                version/(1<<16)%256,
                version/(1<<8)%256,
                version%256
                );
    }

    public static void checkSignature(Context context,String shouldBe) {
        String pkgname = context.getPackageName();
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(pkgname, PackageManager.GET_SIGNATURES);
            for (Signature signature : packageInfo.signatures) {
                String sig=signature.toCharsString();
                if (!shouldBe.equalsIgnoreCase(sig)){
                    logi("INVALID SIGNATURE: "+sig);
                    ErrorHandler.log("INVALID_SIGNATURE: "+sig);
                }else{
                    logi("SIGNATURE: " + sig);
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
