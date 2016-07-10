package com.bigzhao.jianrmagicbox.errorlog;

import com.bigzhao.jianrmagicbox.MagicBox;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Roy on 16-7-9.
 */
public class MessageQueue implements Runnable{
    public ConcurrentLinkedQueue<Runnable> queue=new ConcurrentLinkedQueue<Runnable>();
    private volatile boolean running=false;
    private Thread thread;

    public void loop(){
        if (running) return;
        running=true;
        thread=new Thread(this);
        thread.start();
    }

    public void post(Runnable r){
        queue.offer(r);
        loop();
    }

    public void send(Runnable r){
        queue.offer(r);
        loop();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            MagicBox.logi("MessageQueue Started");
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    continue;
                }
                if (queue.isEmpty()) continue;
                Runnable r = queue.poll();
                if (r == null) continue;
                r.run();
                if (queue.isEmpty()&&ErrorHandler.getCurrentActivity()==null) return;
            }
        }finally {
            MagicBox.logi("MessageQueue Stopped");
            running=false;
        }
    }
}
