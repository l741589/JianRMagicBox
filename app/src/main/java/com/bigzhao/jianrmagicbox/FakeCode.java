package com.bigzhao.jianrmagicbox;

import com.bigzhao.jianrmagicbox.errorlog.ErrorHandler;

/**
 * Created by Roy on 16-6-2.
 */
public class FakeCode {
    public static void loadNative(){
        try {
            CppInterface.load();
        }catch (Throwable e){
            ErrorHandler.log(e);
        }
    }
}
