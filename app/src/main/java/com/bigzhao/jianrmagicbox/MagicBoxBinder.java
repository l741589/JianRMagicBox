package com.bigzhao.jianrmagicbox;

import android.os.Build;
import android.os.RemoteException;

import com.bigzhao.jianrmagicbox.errorlog.ErrorHandler;

/**
 * Created by Roy on 16-6-17.
 */
public abstract class MagicBoxBinder extends com.bigzhao.jianrmagicbox.aidl.IMagicBoxBinder.Stub{

    @Override
    abstract public int getVersion();

    abstract public Object action(String action, String... args) throws Exception;
    @Override
    public String action_remote(String action, String[] args) throws RemoteException {
        try {
            Object o = action(action, args);
            if (o == null) return null;
            return o.toString();
        }catch (Throwable e){
            ErrorHandler.log(e);
            if (Build.VERSION.SDK_INT>=15) {
                throw new RemoteException(MagicBox.exceptionToString(e));
            }else{
                throw new RemoteException();
            }
        }
    }

    public String[] moreServerList(){
        try{
            String[] ret=(String[])action("$moreServerList");
            if (ret==null) return new String[0];
            return ret;
        }catch (Throwable e){
            ErrorHandler.log(e);
        }
        return new String[0];
    }

    public String getVersionMoreArgs() {
        try {
            String s=(String)action("$moreVersionArgs");
            return s==null?"":s;
        } catch (Throwable e) {
            ErrorHandler.log(e);
        }
        return "";
    }
}
