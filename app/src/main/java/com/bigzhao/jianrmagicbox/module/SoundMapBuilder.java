package com.bigzhao.jianrmagicbox.module;

import android.text.TextUtils;

import com.bigzhao.jianrmagicbox.IOUtils;
import com.bigzhao.jianrmagicbox.MagicBox;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.util.Iterator;

/**
 * Created by Roy on 16-6-14.
 */
public class SoundMapBuilder {

    public JSONObject map=new JSONObject();

    public String build()  throws Exception{
        File base = (File) MagicBox.getBinder(MagicBox.application).action("getFile", "magicbox:CV");
        try {loadPackage(base);}catch (Throwable e){e.printStackTrace();}
        base = (File) MagicBox.getBinder(MagicBox.application).action("getFile", "inner_magicbox:CV");
        try {loadPackage(base);}catch (Throwable e){e.printStackTrace();}
        try{loadAssets();}catch (Throwable e){e.printStackTrace();}
        JSONObject json = new JSONObject();
        json.put("map", map);
        String ret=json.toString();
        MagicBox.logi("SoundMap: " + ret);
        return ret;
    }

    private void loadAssets() throws Exception {
        InputStream is=null;
        try {
            is=MagicBox.application.getAssets().open("MagicBox/CV/manifest.json");
            JSONObject json=new JSONObject(IOUtils.readString(is));
            loadMap(json,null,"assets/MagicBox/CV");
            MagicBox.logi("map size:"+map.length());
        }finally {
            IOUtils.closeQuietly(is);
        }
    }

    public void loadRawPackage(File dir,String base) throws Exception {
        if (!dir.isDirectory()) return;
        File[] fs = dir.listFiles();
        if (fs == null) return;
        for (File f : fs) {
            if (f.isDirectory()) {
                String name=f.getName().replace("_",".");
                loadRawPackage(f, base == null ? name : base + "." + name);
            } else {
                File nf=f;
                for(int _i=0;_i<64;++_i){
                    String ext = IOUtils.getExt(nf);
                    if (!f.exists()||f.isDirectory()) break;
                    if (!".txt".equalsIgnoreCase(ext)) break;
                    String s=IOUtils.readString(nf).trim();
                    if (TextUtils.isEmpty(s)) break;
                    nf = s.startsWith("/")?new File(s):new File(dir, s);
                }
                if (f!=nf) MagicBox.logi("SoundMap: redirect: "+f.getCanonicalPath()+" -> "+nf.getCanonicalPath());
                if (!f.exists()||f.isDirectory()) {
                    MagicBox.logi("SoundMap: warn:"+f.getCanonicalPath()+" not exists or it is a directory");
                    continue;
                }
                String ext = IOUtils.getExt(f);
                if (ext == null || ".bat".equals(ext)) {
                    MagicBox.logi("SoundMap: file "+f.getCanonicalPath()+" is ignored");
                    continue;
                }
                String name = f.getName().replace("_",".");
                name = name.substring(0, name.length() - ext.length());
                String key = base == null ? name : base + "." + name;
                if (!map.has(key)) {
                    map.put(key, nf.getCanonicalPath());
                    MagicBox.logi("SoundMap: add " +key+ " : "+ nf.getCanonicalPath());
                }else{
                    MagicBox.logi("SoundMap: conflict " +key+" : "+nf.getCanonicalPath()+" : "+map.get(key));
                }
            }
        }
    }

    public void loadMap(JSONObject json,String base,String pathPrefix) throws JSONException {
        Iterator<String> iter = json.keys();
        while (iter.hasNext()) {
            String key = iter.next();
            Object val = json.get(key);
            String name = base == null ? key : base + "." + key;
            if (val instanceof String) {
                String value= pathPrefix == null ? val.toString() : pathPrefix + "/" + val;
                if (!map.has(name)) {
                    MagicBox.logi("SoundMap: add " +key+" : "+value);
                    map.put(name, value);
                }else{
                    MagicBox.logi("SoundMap: conflict "+key+" : " +value+" : "+map.get(name));
                }
            } else {
                JSONObject obj = json.optJSONObject(key);
                loadMap(obj, name, pathPrefix);
            }
        }
    }

    public void loadPackage(File dir) throws Exception {
        if (dir==null) return;
        File manifest=new File(dir,"manifest.json");
        if (manifest.exists()){
            String s=IOUtils.readString(manifest);
            JSONObject json=new JSONObject(s);
            JSONObject map=json.optJSONObject("map");
            loadMap(map,null,null);
            MagicBox.logi("map size:" + map.length());
            Object mods=json.opt("mod");
            if (mods instanceof String){
                if ("*".equals(mods)){
                    File[] fs=dir.listFiles();
                    if (fs!=null) for(File f:fs) loadPackage(f);
                }else if ("raw".equalsIgnoreCase(mods.toString())){
                    loadRawPackage(dir,null);
                }
            } else {
                JSONArray mod = json.optJSONArray("mod");
                for (int i = 0; i < mod.length(); ++i) {
                    String name = mod.optString(i);
                    if (name == null) continue;
                    File sub = new File(dir, name);
                    if (!sub.exists()) continue;
                    loadPackage(sub);
                }
            }
        } else {
            loadRawPackage(dir,null);
        }
    }

}
