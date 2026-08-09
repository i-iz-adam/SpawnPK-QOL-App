package com.spk.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.spk.app.data.db.dao.CompletedWatchDao
import com.spk.app.data.db.dao.SaleRecordDao
import com.spk.app.data.db.dao.TrackedAccountDao
import com.spk.app.data.db.dao.WatchedItemDao
import com.spk.app.data.db.entity.CompletedWatchEntity
import com.spk.app.data.db.entity.SaleRecordEntity
import com.spk.app.data.db.entity.TrackedAccountEntity
import com.spk.app.data.db.entity.WatchedItemEntity

@Database(
    entities = [
        WatchedItemEntity::class,
        TrackedAccountEntity::class,
        SaleRecordEntity::class,
        CompletedWatchEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun watchedItemDao(): WatchedItemDao
    abstract fun trackedAccountDao(): TrackedAccountDao
    abstract fun saleRecordDao(): SaleRecordDao
    abstract fun completedWatchDao(): CompletedWatchDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "spk.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
