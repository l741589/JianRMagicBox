package com.bigzhao.jianrmagicbox;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by Roy on 16-4-30.
 */
public class LoaderActivity extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Button button=new Button(this);
        button.setText("Start Game");
        setContentView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Class c=Class.forName("org.cocos2dx.cpp.AppActivity");
                    Intent intent=new Intent(LoaderActivity.this,c);
                    startActivity(intent);;
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
