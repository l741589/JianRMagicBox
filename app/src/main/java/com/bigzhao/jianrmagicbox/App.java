package com.bigzhao.jianrmagicbox;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bigzhao.jianrmagicbox.errorlog.ErrorHandler;

import java.lang.ref.WeakReference;
/**
 * Created by Roy on 16-7-16.
 */
public class App extends Application{

    private static Application application;

    public static Application getApplication() {
        if (application==null) throw new RuntimeException("application is null");
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        onCreate(this);
    }


    private static WeakReference<Activity> currentActivity;
    public static Activity getCurrentActivity() {
        if (currentActivity==null) return null;
        return currentActivity.get();
    }

    public static void onCreate(Application app){
        application=app;
        ErrorHandler.init(app);
        app.registerActivityLifecycleCallbacks(callback);
        MagicBox.forceInit();
    }



    private static Application.ActivityLifecycleCallbacks callback=new Application.ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {
            currentActivity=new WeakReference<Activity>(activity);
            MagicBox.logi("onActivityResumed: "+activity);
        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    };
}
