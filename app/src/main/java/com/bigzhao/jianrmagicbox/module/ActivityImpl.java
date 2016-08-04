package com.bigzhao.jianrmagicbox.module;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import com.bigzhao.jianrmagicbox.IActivity;
import com.bigzhao.jianrmagicbox.MagicBox;
import com.bigzhao.jianrmagicbox.defaultmodule.DefaultActivityImpl;
import com.bigzhao.jianrmagicbox.module.net.Request;
import com.bigzhao.jianrmagicbox.util.V;

/**
 * Created by Roy on 16-6-12.
 */
public class ActivityImpl extends DefaultActivityImpl implements IActivity {

    public ActivityImpl(Activity activity){
        super(activity);
        this.activity=activity;
        MagicBox.logi("Activity Created");
    }

    public boolean action(){
        MagicBox.checkSignature(activity, V.SIGNATURE);
        MagicBox.logi("sdkver:" + Build.VERSION.SDK_INT);
        requestExternalStorage();
        return false;
    }

    @SuppressWarnings("NewApi")
    void requestExternalStorage(){
        if (Build.VERSION.SDK_INT<23) return;
        MagicBox.logi("request permission");
        int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS
        };
        int permission = activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }else{
            MagicBox.logi("Permission already granted");
        }
    }
}
