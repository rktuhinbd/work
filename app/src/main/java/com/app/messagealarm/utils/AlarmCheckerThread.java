package com.app.messagealarm.utils;

import android.util.Log;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AlarmCheckerThread extends Thread {

    private static Lock lock = new ReentrantLock();
    private PlayListener playListener;

    public AlarmCheckerThread(PlayListener playListener) {
        this.playListener = playListener;
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
                    Log.e("COUNT", String.valueOf(count));
                    if(count == 5){
                        count = 0;
                        if(!MediaUtils.Companion.isPlaying()){
                            if(!SharedPrefUtils.INSTANCE.readBoolean(Constants.PreferenceKeys.IS_ACTIVITY_STARTED)){
                                playListener.isPlaying(MediaUtils.Companion.isPlaying());
                            }
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
        void isPlaying(boolean s);
    }


}
