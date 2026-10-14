package com.example.alarmboss.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Alarm::class, UserProfile::class, WakeEvent::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AlarmDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun wakeEventDao(): WakeEventDao

    companion object {
        @Volatile private var INSTANCE: AlarmDatabase? = null

        fun getInstance(context: Context): AlarmDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AlarmDatabase::class.java,
                    "alarmboss.db"
                ).fallbackToDestructiveMigration(true).build().also { INSTANCE = it }
            }
    }
}
