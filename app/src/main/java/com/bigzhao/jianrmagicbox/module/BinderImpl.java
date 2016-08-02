package com.bigzhao.jianrmagicbox.module;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.text.TextUtils;

import com.bigzhao.jianrmagicbox.App;
import com.bigzhao.jianrmagicbox.MagicBox;
import com.bigzhao.jianrmagicbox.module.errorlog.ErrorHandler;
import com.bigzhao.jianrmagicbox.util.IOUtils;
import com.bigzhao.jianrmagicbox.defaultmodule.DefaultBinderImpl;
import com.bigzhao.jianrmagicbox.util.V;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by Roy on 16-6-12.
 */
public class BinderImpl extends DefaultBinderImpl  {

    public BinderImpl(Context context) {
        super(context);
        MagicBox.logi("Binder Created");
    }

    @Override
    public int getVersion() {
        return V.BINDER;
    }

    @Override
    public Object action(String action, String... args) throws Exception{
        MagicBox.logi("%s(%s)", action, TextUtils.join(", ", args));
        if ("install".equalsIgnoreCase(action)) {
            copy(args[0], args[1]);
        } else if ("uninstall".equalsIgnoreCase(action)) {
            del(args[0]);
        } else if ("install_cv".equalsIgnoreCase(action)) {
            install_cv(args[0], args[1]);
        } else if ("buildSoundMap".equals(action)) {
            return new SoundMapBuilder().build();
        } else if ("getNativePath".equalsIgnoreCase(action)) {
            return getNativePath();
        }else if ("$moreVersionArgs".equalsIgnoreCase(action)){
            return doGetVersionMoreArgs();
        } else if ("logError".equalsIgnoreCase(action)) {
            if (args.length > 0) ErrorHandler.logNative(args[0]);
        } else if ("logGameInfo".equalsIgnoreCase(action)) {
            GameInfoLogger.log(args[0], args[1],args[2],args[3]);
        } else if ("isSignatureValid".equalsIgnoreCase(action)){
            return checkSignature()?"1":"0";
        } else {
            return super.action(action, args);
        }
        return null;
    }

    private String doGetVersionMoreArgs() {
        return "";
    }


    private void install_cv(String file, String token) throws IOException, JSONException {
        ZipFile zip = null;
        try {
            MagicBox.logi("copy start");
            zip = new ZipFile(file);
            ZipEntry e = zip.getEntry("manifest.json");
            JSONObject json = new JSONObject(new String(IOUtils.readBytes(zip.getInputStream(e))));
            String name = json.getString("name");
            String author = json.getString("author");
            File f = new File(getWorkingDir(), String.format("CV/%s.%s", author, name));
            if (!f.mkdirs()) mkdirFail(f);
            IOUtils.unzip(new File(file), f);
            MagicBox.logi("copy success");
        } finally {
            IOUtils.closeQuietly(zip);
        }
    }


    public String getNativePath(){
        return new File(MagicBox.FILES_DIR,"MagicBox/module/libMagicBox.so").getAbsolutePath();
    }

    public static boolean checkSignature() {
        String pkgname = App.getApplication().getPackageName();
        try {
            PackageInfo packageInfo = App.getApplication().getPackageManager().getPackageInfo(pkgname, PackageManager.GET_SIGNATURES);
            for (Signature signature : packageInfo.signatures) {
                String sig=signature.toCharsString();
                if (!V.SIGNATURE.equalsIgnoreCase(sig)){
                    MagicBox.logi("INVALID SIGNATURE: " + sig);
                    return false;
                }else{
                    MagicBox.logi("SIGNATURE: " + sig);
                }
            }
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }

}
