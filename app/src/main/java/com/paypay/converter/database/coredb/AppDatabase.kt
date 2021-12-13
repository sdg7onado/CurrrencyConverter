/*
 * *
 *  * Created by Okechukwu Agufuobi on 13/12/2021, 2:43 PM
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 13/12/2021, 11:27 AM
 *
 */

package com.paypay.converter.database.coredb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.*
import com.paypay.converter.database.daos.ConversionRateDao
import com.paypay.converter.database.daos.CurrencyDao
import com.paypay.converter.models.ConversionRate
import com.paypay.converter.models.Currency
import com.paypay.converter.workers.ConverterWorker
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * The type App database.
 */
@Database(
    entities = arrayOf(Currency::class,ConversionRate::class),
    version = 1,
    exportSchema = false
)

public abstract class AppDatabase : RoomDatabase() {

    abstract fun currencyDao(): CurrencyDao
    abstract fun conversionRateDao(): ConversionRateDao

    companion object {

        private const val NUMBER_OF_THREADS = 4

        // For Singleton instantiation
        @Volatile
        private var instance: AppDatabase? = null

        /**
         * The Database write executor.
         */
        val databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS)


        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        // Create and pre-populate the database. See this article for more details:
        // https://medium.com/google-developers/7-pro-tips-for-room-fbadea4bfbd1#4785
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, "paypay_convert_database")
                .addCallback(
                    object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {

                            super.onCreate(db)

                            val constraint: Constraints = Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build()

                            val workRequest = PeriodicWorkRequestBuilder<ConverterWorker>(
                                30, //BuildConfig.REFRESH_INTERVAL,
                                TimeUnit.MINUTES)
                                .setConstraints(constraint)
                                .build()

                            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                                "converter_bg_work",
                                ExistingPeriodicWorkPolicy.KEEP,
                                workRequest
                            )

                        }
                    }
                )
                .build()
        }
    }
}