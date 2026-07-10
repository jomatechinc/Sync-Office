package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chats ORDER BY lastUpdated DESC")
    fun getAllChats(): Flow<List<ChatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)

    @Query("DELETE FROM chats WHERE id = :id")
    suspend fun deleteChatById(id: String)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)
}

@Dao
interface CalendarEventDao {
    @Query("SELECT * FROM calendar_events ORDER BY startTime ASC")
    fun getAllEvents(): Flow<List<CalendarEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEventEntity)

    @Query("DELETE FROM calendar_events WHERE id = :id")
    suspend fun deleteEventById(id: Int)
}

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY lastModified DESC")
    fun getAllDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getDocumentById(id: Int): DocumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocumentById(id: Int)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY dueDate ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Int)
}

@Dao
interface AutomationRuleDao {
    @Query("SELECT * FROM automation_rules")
    fun getAllRules(): Flow<List<AutomationRuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: AutomationRuleEntity)

    @Query("DELETE FROM automation_rules WHERE id = :id")
    suspend fun deleteRuleById(id: Int)
}

@Dao
interface WorkflowLogDao {
    @Query("SELECT * FROM workflow_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<WorkflowLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: WorkflowLogEntity)

    @Query("DELETE FROM workflow_logs")
    suspend fun clearLogs()
}

@Dao
interface UserAccountDao {
    @Query("SELECT * FROM user_accounts LIMIT 1")
    fun getActiveAccount(): Flow<UserAccountEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: UserAccountEntity)

    @Query("DELETE FROM user_accounts")
    suspend fun clearAccount()
}

@Dao
interface RegisteredUserDao {
    @Query("SELECT * FROM registered_users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): RegisteredUserEntity?

    @Query("SELECT * FROM registered_users")
    fun getAllRegisteredUsers(): Flow<List<RegisteredUserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegisteredUser(user: RegisteredUserEntity)
}

@Dao
interface WorkGroupDao {
    @Query("SELECT * FROM work_groups ORDER BY createdAt DESC")
    fun getAllGroups(): Flow<List<WorkGroupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: WorkGroupEntity): Long

    @Query("DELETE FROM work_groups WHERE id = :id")
    suspend fun deleteGroupById(id: Int)
}

@Dao
interface WorkGroupMemberDao {
    @Query("SELECT * FROM work_group_members WHERE groupId = :groupId")
    fun getMembersForGroup(groupId: Int): Flow<List<WorkGroupMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: WorkGroupMemberEntity)

    @Query("DELETE FROM work_group_members WHERE groupId = :groupId AND email = :email")
    suspend fun removeMember(groupId: Int, email: String)
}

