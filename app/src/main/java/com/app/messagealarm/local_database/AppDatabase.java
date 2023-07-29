package com.app.messagealarm.local_database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.app.messagealarm.model.dao.AppConstrainDao;
import com.app.messagealarm.model.dao.AppDao;
import com.app.messagealarm.model.dao.ApplicationDao;
import com.app.messagealarm.model.dao.ApplicationDaoNew;
import com.app.messagealarm.model.dao.LanguageDao;
import com.app.messagealarm.model.entity.AppConstrainEntity;
import com.app.messagealarm.model.entity.AppEntity;
import com.app.messagealarm.model.entity.ApplicationEntity;
import com.app.messagealarm.model.entity.ApplicationEntityNew;
import com.app.messagealarm.model.entity.LanguageEntity;

@Database(entities = {
        ApplicationEntity.class,
        ApplicationEntityNew.class,
        AppConstrainEntity.class,
        AppEntity.class, LanguageEntity.class
}, exportSchema = false, version = 3)

public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "MODEL_DATABASE";
    private static AppDatabase appDatabase;

    public static synchronized AppDatabase getInstance(Context context) {
        Migration migration = new Migration(2, 3) {
            @Override
            public void migrate(@NonNull SupportSQLiteDatabase database) {
                /**
                 * @Notes for Mortuza, I need you to make sure this database migration will happen to our existing
                 * users perfectly and no one will have a problem.
                 */
                //This below two migration are for version 2.0.2 currently live in Google play store
                // database.execSQL("ALTER TABLE applications ADD COLUMN ignored_names TEXT DEFAULT 'None'");
                // database.execSQL("ALTER TABLE applications ADD COLUMN sound_level INTEGER NOT NULL DEFAULT 100");
                //This below migration is for version 2.0.4 our next version
                database.execSQL("ALTER TABLE applications ADD COLUMN is_flash_on BOOLEAN NOT NULL DEFAULT 0");
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
     *
     * @return
     */
    public abstract ApplicationDao applicationDao();

    public abstract ApplicationDaoNew applicationDaoNew();

    public abstract AppConstrainDao appConstrainDao();

    public abstract LanguageDao languageDao();

    public abstract AppDao appDao();
}
