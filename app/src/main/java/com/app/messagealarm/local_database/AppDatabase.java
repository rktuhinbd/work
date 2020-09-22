package com.app.messagealarm.local_database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {}, exportSchema = false, version = 1)

public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "MODEL_DATABASE";
    private static AppDatabase appDatabase;

    public static synchronized AppDatabase getInstance(Context context) {
        if (appDatabase == null) {
            appDatabase = Room.databaseBuilder(context,
                    AppDatabase.class,
                    DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return appDatabase;
    }

    /*
    *dao list like this
    public abstract TestPlanDao testPlanDao();*/

}
