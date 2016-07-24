package com.bigzhao.jianrmagicbox.defaultmodule;

import android.app.Activity;

import com.bigzhao.jianrmagicbox.IActivity;
import com.bigzhao.jianrmagicbox.MagicBox;
import com.bigzhao.jianrmagicbox.util.V;

/**
 * Created by Roy on 16-6-15.
 */
public class DefaultActivityImpl implements IActivity {
    protected   Activity activity;

    public DefaultActivityImpl(Activity activity){
        this.activity=activity;
    }
    @Override
    public boolean action() {
        MagicBox.checkSignature(activity, V.SIGNATURE);
        return false;
    }
}
