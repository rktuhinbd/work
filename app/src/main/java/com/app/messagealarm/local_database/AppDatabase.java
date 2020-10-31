package com.app.messagealarm.local_database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.app.messagealarm.model.dao.AppConstrainDao;
import com.app.messagealarm.model.dao.AppDao;
import com.app.messagealarm.model.dao.ApplicationDao;
import com.app.messagealarm.model.dao.LanguageDao;
import com.app.messagealarm.model.entity.AppConstrainEntity;
import com.app.messagealarm.model.entity.AppEntity;
import com.app.messagealarm.model.entity.ApplicationEntity;
import com.app.messagealarm.model.entity.LanguageEntity;

@Database(entities = {ApplicationEntity.class, AppConstrainEntity.class,
        AppEntity.class, LanguageEntity.class
}, exportSchema = false, version = 1)

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

    /**
     * DAO of application table
     * @return
     */
    public abstract ApplicationDao applicationDao();
    public abstract AppConstrainDao appConstrainDao();
    public abstract LanguageDao languageDao();
    public abstract AppDao appDao();
}
