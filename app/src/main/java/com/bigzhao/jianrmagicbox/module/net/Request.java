package com.bigzhao.jianrmagicbox.module.net;

import android.text.TextUtils;

import com.bigzhao.jianrmagicbox.MagicBox;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Roy on 16-8-2.
 */
public class Request implements Cloneable {
    private String method = "GET";
    private String encoding = "UTF-8";
    private HashMap<String, String> headers = new HashMap<String, String>();
    private boolean async = false;
    private ResultCallback callback = null;
    private String[] hosts = MagicBox.serverList;
    private String schema = "http";
    private String path;
    private byte[] body;
    private boolean gzip = false;
    private Map<String,Object> params;
    private Response mock=null;

    public Response getMock() {
        return mock;
    }

    public Request setMock(Response mock) {
        this.mock = mock;
        return this;
    }


    public static Request create(){
        return new Request();
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getMethod() {

        return method;
    }

    public Request setMethod(String method) {
        this.method = method;
        return this;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public Request setHeaders(HashMap<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public boolean isAsync() {
        return async;
    }

    public Request setAsync(boolean async) {
        this.async = async;
        return this;
    }

    public ResultCallback getCallback() {
        return callback;
    }

    public Request setCallback(ResultCallback callback) {
        this.async=true;
        this.callback = callback;
        return this;
    }

    public String[] getHosts() {
        return hosts;
    }

    public Request setHosts(String[] hosts) {
        this.hosts = hosts;
        return this;
    }

    public String getSchema() {
        return schema;
    }

    public Request setSchema(String schema) {
        this.schema = schema;
        return this;
    }

    public String getPath() {
        return path;
    }

    public Request setPath(String path) {
        this.path = path;
        return this;
    }

    public byte[] getBody() {
        return body;
    }

    public Request setBody(String s){
        return setBody(s,encoding);
    }

    public Request setBody(String s,String encoding){
        try {
            if (TextUtils.isEmpty(encoding)) setContentType("text/plain");
            else setContentType("text/plain; charset=" + encoding);
            setBody(s.getBytes(encoding));
            return this;
        }catch (UnsupportedEncodingException e){
            throw new RuntimeException(e);
        }
    }

    public Request setBody(byte[] body) {
        this.body = body;
        if ("GET".equalsIgnoreCase(method)) method="POST";
        return this;
    }

    public boolean isGzip() {
        return gzip;
    }

    public Request setGzip(boolean gzip) {
        if (gzip) addHeader("Content-Encoding","gzip");
        this.gzip = gzip;
        return this;
    }

    @Override
    public Request clone() {
        try {
            return (Request) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Request addHeader(String name, String val) {
        headers.put(name,val);
        return this;
    }

    public Request setContentType(String contentType){
        return addHeader("Content-Type",contentType);
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public Request setParams(Map<String, Object> params) {
        this.params = params;
        return this;
    }

    public Request addParam(String key,Object val){
        if (params==null) params=new HashMap<String, Object>();
        params.put(key,val);
        return this;
    }

    public String generateQuery(){
        try {
            if (params == null||params.size()==0) return null;
            boolean start = true;
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Object> e : params.entrySet()) {
                if (start) start = false;
                else sb.append("&");
                sb.append(e.getKey()).append("=").append(URLEncoder.encode(String.valueOf(e.getValue()), encoding));
            }
            return sb.toString();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public Response exec(){
        return NetManager.request(this);
    }

    public Response post(){
        return NetManager.request(this.setMethod("POST"));
    }

    public Response get(){
        return NetManager.request(this.setMethod("GET"));
    }
}
