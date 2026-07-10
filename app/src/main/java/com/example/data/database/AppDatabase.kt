package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ChatEntity::class,
        MessageEntity::class,
        CalendarEventEntity::class,
        DocumentEntity::class,
        TaskEntity::class,
        AutomationRuleEntity::class,
        WorkflowLogEntity::class,
        UserAccountEntity::class,
        RegisteredUserEntity::class,
        WorkGroupEntity::class,
        WorkGroupMemberEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
    abstract fun calendarEventDao(): CalendarEventDao
    abstract fun documentDao(): DocumentDao
    abstract fun taskDao(): TaskDao
    abstract fun automationRuleDao(): AutomationRuleDao
    abstract fun workflowLogDao(): WorkflowLogDao
    abstract fun userAccountDao(): UserAccountDao
    abstract fun registeredUserDao(): RegisteredUserDao
    abstract fun workGroupDao(): WorkGroupDao
    abstract fun workGroupMemberDao(): WorkGroupMemberDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sync_office_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
