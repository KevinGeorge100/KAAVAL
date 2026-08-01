package com.kaaval.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kaaval.app.data.dao.ContactDao
import com.kaaval.app.data.dao.EmergencyStateDao
import com.kaaval.app.data.dao.IncidentDao
import com.kaaval.app.data.dao.MedicalProfileDao
import com.kaaval.app.data.entity.ContactEntity
import com.kaaval.app.data.entity.EmergencyStateEntity
import com.kaaval.app.data.entity.IncidentEntity
import com.kaaval.app.data.entity.MedicalProfileEntity

@Database(
    entities = [ContactEntity::class, IncidentEntity::class, MedicalProfileEntity::class, EmergencyStateEntity::class],
    version = 2,
    exportSchema = false
)
abstract class KaavalDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun incidentDao(): IncidentDao
    abstract fun medicalProfileDao(): MedicalProfileDao
    abstract fun emergencyStateDao(): EmergencyStateDao

    companion object {
        @Volatile
        private var INSTANCE: KaavalDatabase? = null

        fun getDatabase(context: Context): KaavalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KaavalDatabase::class.java,
                    "kaaval_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
