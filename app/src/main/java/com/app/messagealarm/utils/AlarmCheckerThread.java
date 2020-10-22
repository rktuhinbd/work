package com.app.messagealarm.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AlarmCheckerThread extends Thread {

    private static Lock lock = new ReentrantLock();
    private PlayListener playListener;
    private Context context;

    public AlarmCheckerThread(PlayListener playListener, Context context) {
        this.playListener = playListener;
        this.context = context;
    }
    private int count = 0;

    public void execute(){
        if(!isAlive()){
          start();
        }
    }

    @Override
    public void run() {
        if (AlarmCheckerThread.lock.tryLock())
        {
            try
            {
                // TODO something
                while (true){
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    count++;
                    if(count == 5){
                        count = 0;
                        final boolean[] isPLaying = {false};
                        // Get a handler that can be used to post to the main thread
                        Handler mainHandler = new Handler(context.getMainLooper());
                        Runnable myRunnable = new Runnable() {
                            @Override
                            public void run() {
                                isPLaying[0] = ExoPlayerUtils.Companion.isPlaying();
                            } // This is your code
                        };
                        mainHandler.post(myRunnable);
                        if(!isPLaying[0]){
                            playListener.isPlaying(isPLaying);
                        }
                        interrupt();
                        break;
                    }
                }
            }
            finally
            {
                AlarmCheckerThread.lock.unlock();
            }
        }


    }

    @Override
    public void interrupt() {
        super.interrupt();
    }

    public interface  PlayListener{
        void isPlaying(boolean[] s);
    }


}
