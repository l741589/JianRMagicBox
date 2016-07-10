package com.bigzhao.jianrmagicbox.module;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.bigzhao.jianrmagicbox.MagicBox;
import com.bigzhao.jianrmagicbox.IOUtils;
import com.bigzhao.jianrmagicbox.defaultmodule.DefaultBinderImpl;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by Roy on 16-6-12.
 */
public class BinderImpl extends DefaultBinderImpl  {

    public static int version = 0x01000100;

    public BinderImpl(Context context) {
        super(context);
        MagicBox.logi("Binder Created");
    }

    @Override
    public int getVersion() {
        return version;
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

}
