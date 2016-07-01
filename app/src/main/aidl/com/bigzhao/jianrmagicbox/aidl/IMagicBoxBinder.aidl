// IMagicBoxBinder.aidl
package com.bigzhao.jianrmagicbox.aidl;

// Declare any non-default types here with import statements

interface IMagicBoxBinder {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */

    int getVersion();
    String action_remote(in String action,in String[] args);
}
