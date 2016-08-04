package com.bigzhao.jianrmagicbox.module.net;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;

/**
 * Created by Roy on 16-8-2.
 */
public class Response {
    public byte[] data;
    public Request request;

    private String stringResult;
    private JSONObject body;
    private JSONObject jsonResult;
    private boolean success;


    private void initJsonResult(){
        try {
            if (jsonResult!=null) return;
            jsonResult=new JSONObject(toString());
            body=jsonResult.optJSONObject("data");
            success=jsonResult.optBoolean("success");
        } catch (JSONException e) {
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
