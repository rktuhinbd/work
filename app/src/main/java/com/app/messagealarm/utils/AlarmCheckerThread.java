package com.app.messagealarm.utils;

public class AlarmCheckerThread extends Thread {

    PlayListener playListener;
    public AlarmCheckerThread(PlayListener listener){
        this.playListener = listener;
    }
    private int count = 0;

    @Override
    public void run() {
        while (true){
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
            if(count == 4){
                    playListener.isPlaying(ExoPlayerUtils.Companion.isPlaying());
                    interrupt();
            }
        }

    }

   public interface  PlayListener{
        void isPlaying(boolean s);
    }


}
