package com.bigzhao.jianrmagicbox;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.bigzhao.jianrmagicbox.errorlog.ErrorHandler;

/**
 * Created by Roy on 16-6-12.
 */
public class MagicBoxReciever extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        try{
            MagicBoxBinder binder = MagicBox.getBinder(context.getApplicationContext());
            if (binder==null) {
                Log.e("MagicBox","create binder failed");
            }else {
                String action = intent.getStringExtra("action");
                String[] args = intent.getStringArrayExtra("args");
                if (args == null) {
                    String sp = intent.getStringExtra("args_sp");
                    String as = intent.getStringExtra("args_str");
                    if (as != null) args = as.split(sp==null?",":sp);
                    else args = new String[0];
                }
                binder.action(action, args);
            }
        } catch (Exception e) {
            ErrorHandler.log(e);
        }
    }
}
