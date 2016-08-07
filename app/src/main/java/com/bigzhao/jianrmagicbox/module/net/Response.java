package com.bigzhao.jianrmagicbox.module.net;

import android.content.res.Resources;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;

/**
 * Created by Roy on 16-8-2.
 */
public class Response {


    private byte[] data;
    private Request request;

    private String stringResult;
    private JSONObject body;
    private JSONObject jsonResult;
    private boolean success=false;
    private Throwable throwable;

    public Response(){

    }
    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Response(Throwable e) {
        success=false;
        throwable=e;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    private void initJsonResult(){
        try {
            if (jsonResult!=null||data==null) return;
            jsonResult=new JSONObject(toString());
            body=jsonResult.optJSONObject("data");
            success=jsonResult.optBoolean("success");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    };
    public boolean isSuccess(){
        initJsonResult();
        return success;
    }

    public JSONObject getBody(){
        initJsonResult();
        return body;
    }

    public JSONObject getJsonResult() {
        initJsonResult();
        return jsonResult;
    }

    @Override
    public String toString() {
        if (stringResult!=null) return stringResult;
        return stringResult=new String(data, Charset.forName(request.getEncoding()));
    }
}
