package com.billmind.pro.di

import android.content.Context
import androidx.room.Room
import com.billmind.pro.data.BillMindDao
import com.billmind.pro.data.BillMindDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BillMindDatabase {
        return Room.databaseBuilder(context, BillMindDatabase::class.java, "bill_mind_pro.db").build()
    }

    @Provides
    fun provideDao(database: BillMindDatabase): BillMindDao = database.dao()
}
