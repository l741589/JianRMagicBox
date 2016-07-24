package com.bigzhao.jianrmagicbox.errorlog;

import android.os.Environment;

import com.bigzhao.jianrmagicbox.App;
import com.bigzhao.jianrmagicbox.MagicBox;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

/**
 * Created by Roy on 16-7-9.
 */
public class SelfCheck {

    public static String run(String command,String... args){
        try {
            if ("fileInfo".equals(command)) return fileInfo(new File(args[0]));
            else if ("filePermission".equals(command)) return filePermission(new File(args[0]));
            else if ("checkChildren".equals(command)) return checkChildren(new StringBuilder(),true,new File(args[0]), Arrays.copyOfRange(args, 1, args.length)).toString();
            else if ("checkModule".equals(command)) return checkModule();
            else if ("tree".equals(command)) return args.length==0?tree():tree(new StringBuilder(),new File(args[0])).toString();
            else return "error command";
        }catch (Throwable e){
            return MagicBox.exceptionToString(e);
        }
    }

    public static String filePermission(File f){
        return new StringBuilder()
            .append(f.exists()?'e':'-')
            .append(f.isDirectory() ? 'd' : '-')
            .append(f.canRead()?'r':'-')
            .append(f.canWrite()?'w':'-')
            .append(f.canExecute()?'x':'-')
            .toString();
    }

    public static String fileInfo(File file){
        return filePermission(file) + " " + String.valueOf(file.getAbsoluteFile());
    }

    private static File goInto(StringBuilder sb,boolean showBase,File base,String...dirs){
        if (showBase) sb.append(fileInfo(base)).append("\n");
        if (dirs==null) return base;
        for(String s:dirs){
            if (s==null) continue;
            base = new File(base, s);
            sb.append(fileInfo(base)).append("\n");
        }
        return base;
    }

    private static StringBuilder checkChildren(StringBuilder sb,boolean showBase,File base,String...names){
        if (showBase) sb.append(fileInfo(base)).append("\n");
        if (!base.isDirectory()) return sb;
        if (names==null||names.length==0)names=base.list();
        if (names==null) return sb;
        for(String s:names){
            if (s==null) continue;
            File file = new File(base, s);
            sb.append(fileInfo(file)).append("\n");
        }
        return sb;
    }

    public static String checkModule(){
        StringBuilder sb=new StringBuilder();
        File module=goInto(sb, true, App.getApplication().getFilesDir(),"MagicBox","module");
        checkChildren(sb,false,module,"classes.dex","libMagicBox.so");
        return sb.toString();
    }

    private static StringBuilder tree(StringBuilder sb,File base){
        sb.append(fileInfo(base)).append("\n");
        if (base.exists()&&base.isDirectory()){
            File[] fs=base.listFiles();
            if (fs!=null){
                for (File f:fs) tree(sb,f);
            }
        }
        return sb;
    }

    public static String tree(){
        StringBuilder sb=new StringBuilder();
        sb.append("inner:\n");
        tree(sb,App.getApplication().getFilesDir());
        if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)){
            sb.append("sdcard:\n");
            tree(sb,App.getApplication().getExternalFilesDir(null));
        }
        return sb.toString();
    }
}
