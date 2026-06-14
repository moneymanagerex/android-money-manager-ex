package com.money.manager.ex.di

import android.content.Context
import androidx.room.Room
import com.money.manager.ex.data.local.MmexDatabase
import com.money.manager.ex.data.local.dao.AccountDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MmexDatabase {
        return Room.databaseBuilder(
            context,
            MmexDatabase::class.java,
            "mmex_v2.db"
        )
        // createFromFile() o createFromAsset() verrebbero usati qui per il database legacy
        .build()
    }

    @Provides
    fun provideAccountDao(database: MmexDatabase): AccountDao {
        return database.accountDao()
    }
}
