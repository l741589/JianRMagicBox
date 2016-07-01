package com.bigzhao.jianrmagicbox;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;

import com.bigzhao.jianrmagicbox.aidl.IMagicBoxBinder;

/**
 * Created by Roy on 16-6-12.
 */
public class MagicBoxService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return MagicBox.getBinder(getApplication());
    }

}
