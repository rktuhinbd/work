package com.app.messagealarm;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.app.messagealarm.service.notification_service.NotificationListener;

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //catch global exceptions
        final Thread.UncaughtExceptionHandler oldHandler =
                Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(
                new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(
                            Thread paramThread,
                            Throwable paramThrowable
                    ) {
                        PendingIntent service = PendingIntent.getService(
                                getApplicationContext(),
                                1001,
                                new Intent(getApplicationContext(), NotificationListener.class),
                                PendingIntent.FLAG_ONE_SHOT);
                        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                        if (alarmManager != null) {
                            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000, service);
                        }
                        //System.exit(2);
                    }
                });
    }

}
