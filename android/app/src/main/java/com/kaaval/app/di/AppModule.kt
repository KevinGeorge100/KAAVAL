package com.kaaval.app.di

import android.content.Context
import com.kaaval.app.data.KaavalDatabase
import com.kaaval.app.data.KaavalRepository

/**
 * Dependency Injection container / service provider placeholder for KAAVAL application.
 * Prepares the application for Hilt/Koin or manual dependency injection.
 */
object AppModule {

    @Volatile
    private var repository: KaavalRepository? = null

    fun provideRepository(context: Context): KaavalRepository {
        return repository ?: synchronized(this) {
            val db = KaavalDatabase.getDatabase(context)
            val instance = KaavalRepository(db)
            repository = instance
            instance
        }
    }
}
