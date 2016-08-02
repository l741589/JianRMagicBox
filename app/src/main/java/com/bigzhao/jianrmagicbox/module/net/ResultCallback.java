package com.bigzhao.jianrmagicbox.module.net;

/**
 * Created by Roy on 16-8-2.
 */
public interface ResultCallback {
    void onResult(Response result);

    ResultCallback Null = new ResultCallback() {
        @Override
        public void onResult(Response result) {

        }
    };
}
