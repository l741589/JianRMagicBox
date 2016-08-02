package com.bigzhao.jianrmagicbox.module.net;

import com.bigzhao.jianrmagicbox.MagicBox;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * Created by Roy on 16-8-2.
 */
public class Request implements Cloneable {
    private String method = "GET";
    private HashMap<String, String> headers = new HashMap<String, String>();
    private boolean async = false;
    private ResultCallback callback = null;
    private String[] hosts = MagicBox.serverList;
    private String schema = "http";
    private String path;
    private byte[] body;
    private boolean gzip = false;

    public static Request create(){
        return new Request();
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
        return setBody(s,"UTF-8");
    }

    public Request setBody(String s,String encoding){
        try {
            setContentType("text/plain; " + encoding.toLowerCase());
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
