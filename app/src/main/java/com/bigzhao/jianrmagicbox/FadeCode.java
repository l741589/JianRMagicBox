package com.bigzhao.jianrmagicbox;

/**
 * Created by Roy on 16-6-2.
 */
public class FadeCode {
    public static void loadNative(){
        System.loadLibrary("MagicBox");
        CppInterface.init();
    }
}
