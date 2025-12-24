package com.cortlandwalker.shortoftheweek.data.cache

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CacheModule {

    @Provides
    @Singleton
    fun provideCacheDb(@ApplicationContext context: Context): SotwCacheDb =
        Room.databaseBuilder(context, SotwCacheDb::class.java, "sotw_cache.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideFeedCacheDao(db: SotwCacheDb): FeedCacheDao = db.feedCacheDao()

    @Provides
    fun provideFilmDao(db: SotwCacheDb): FilmDao = db.filmDao()
}

