package com.bigzhao.jianrmagicbox;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.bigzhao.jianrmagicbox.errorlog.ErrorHandler;

/**
 * Created by Roy on 16-4-30.
 */
public class LoaderActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MagicBox.application=getApplication();
        ErrorHandler.init(this);
        IActivity ia=MagicBox.getActivityDelegate(this);
        if (ia.action()) return;
        try {
            Class c = Class.forName("org.cocos2dx.cpp.AppActivity");
            Intent intent = new Intent(LoaderActivity.this, c);
            startActivity(intent);
            finish();
        }catch (Exception e){
            ErrorHandler.log(e);
        }
        //throw new RuntimeException("test");
    }

}
