package com.app.messagealarm.local_database;

import android.content.Context;
import android.database.sqlite.SQLiteTransactionListener;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.app.messagealarm.model.dao.AppConstrainDao;
import com.app.messagealarm.model.dao.AppDao;
import com.app.messagealarm.model.dao.ApplicationDao;
import com.app.messagealarm.model.dao.LanguageDao;
import com.app.messagealarm.model.entity.AppConstrainEntity;
import com.app.messagealarm.model.entity.AppEntity;
import com.app.messagealarm.model.entity.ApplicationEntity;
import com.app.messagealarm.model.entity.LanguageEntity;
import com.app.messagealarm.utils.Constants;
import com.app.messagealarm.utils.SharedPrefUtils;

@Database(entities = {ApplicationEntity.class, AppConstrainEntity.class,
        AppEntity.class, LanguageEntity.class
}, exportSchema = false, version = 2)

public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "MODEL_DATABASE";
    private static AppDatabase appDatabase;

    public static synchronized AppDatabase getInstance(Context context) {
        Migration migration = new Migration(1, 2) {
            @Override
            public void migrate(@NonNull SupportSQLiteDatabase database) {
                database.execSQL("ALTER TABLE applications ADD COLUMN ignored_names TEXT DEFAULT 'None'");
                database.execSQL("ALTER TABLE applications ADD COLUMN sound_level INTEGER NOT NULL DEFAULT 100");
            }
        };
        if (appDatabase == null) {
            appDatabase = Room.databaseBuilder(context,
                    AppDatabase.class,
                    DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .addMigrations(migration)
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
