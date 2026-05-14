package com.bhoomika.kreedaankana.data.local.db

import android.content.Context
import androidx.room.*
import com.bhoomika.kreedaankana.data.local.Converters
import com.bhoomika.kreedaankana.data.local.dao.BookingDao
import com.bhoomika.kreedaankana.data.local.entity.Booking

@Database(entities = [Booking::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun bookingDao(): BookingDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kreeda_db"
                ).build()
            }
        }
    }
}