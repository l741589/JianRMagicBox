package com.bigzhao.jianrmagicbox;

import android.util.Log;

import java.io.File;

/**
 * Created by Roy on 16-6-2.
 */
public class FakeCode {
    public static void loadNative(){
        try {
            CppInterface.load();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
