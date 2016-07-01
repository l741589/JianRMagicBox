package com.bigzhao.jianrmagicbox;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import dalvik.system.DexClassLoader;

/**
 * Created by Roy on 16-6-15.
 */
class FakeClassLoader extends DexClassLoader{


    public FakeClassLoader(String dexPath, String optimizedDirectory, String libraryPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, libraryPath, parent);
    }

    @Override
    public String findLibrary(String name) {
        MagicBox.logi("FindLibrary:"+name);
        return super.findLibrary(name);
    }
}
