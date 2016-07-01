package com.bigzhao.jianrmagicbox;

import android.content.Context;
import android.media.SoundPool;

/**
 * Created by Roy on 16-6-2.
 */
public class CppInterface {

    public static void load() throws Exception {
        Object obj=MagicBox.getBinder(MagicBox.application).action("getNativePath");
        if (obj==null) return;
        MagicBox.logi("CppInterface:"+CppInterface.class.getClassLoader());
        String soPath=obj.toString();
        MagicBox.logi(soPath);
        System.load(soPath);
        CppInterface.init();
        CppInterface.nativeAction(0, null);
    }

    public native static void init();
    public native static Object nativeAction(int action,String[] args);
    public static Object action(String action,String[] args) throws Exception {
        return MagicBox.getBinder(MagicBox.application).action(action,args);
    }
}
