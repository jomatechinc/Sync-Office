package com.example.data.repository

import com.example.data.database.*
import kotlinx.coroutines.flow.Flow

class SyncOfficeRepository(private val db: AppDatabase) {

    // Chat operations
    val allChats: Flow<List<ChatEntity>> = db.chatDao().getAllChats()
    suspend fun insertChat(chat: ChatEntity) = db.chatDao().insertChat(chat)
    suspend fun deleteChatById(id: String) = db.chatDao().deleteChatById(id)

    // Message operations
    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>> = db.messageDao().getMessagesForChat(chatId)
    suspend fun insertMessage(message: MessageEntity) = db.messageDao().insertMessage(message)

    // Calendar operations
    val allEvents: Flow<List<CalendarEventEntity>> = db.calendarEventDao().getAllEvents()
    suspend fun insertEvent(event: CalendarEventEntity) = db.calendarEventDao().insertEvent(event)
    suspend fun deleteEventById(id: Int) = db.calendarEventDao().deleteEventById(id)

    // Document operations
    val allDocuments: Flow<List<DocumentEntity>> = db.documentDao().getAllDocuments()
    suspend fun getDocumentById(id: Int): DocumentEntity? = db.documentDao().getDocumentById(id)
    suspend fun insertDocument(document: DocumentEntity) = db.documentDao().insertDocument(document)
    suspend fun deleteDocumentById(id: Int) = db.documentDao().deleteDocumentById(id)

    // Task operations
    val allTasks: Flow<List<TaskEntity>> = db.taskDao().getAllTasks()
    suspend fun insertTask(task: TaskEntity) = db.taskDao().insertTask(task)
    suspend fun deleteTaskById(id: Int) = db.taskDao().deleteTaskById(id)

    // Automation operations
    val allRules: Flow<List<AutomationRuleEntity>> = db.automationRuleDao().getAllRules()
    suspend fun insertRule(rule: AutomationRuleEntity) = db.automationRuleDao().insertRule(rule)
    suspend fun deleteRuleById(id: Int) = db.automationRuleDao().deleteRuleById(id)

    // Workflow logs
    val allLogs: Flow<List<WorkflowLogEntity>> = db.workflowLogDao().getAllLogs()
    suspend fun insertLog(log: WorkflowLogEntity) = db.workflowLogDao().insertLog(log)
    suspend fun clearLogs() = db.workflowLogDao().clearLogs()

    // User Account
    val activeAccount: Flow<UserAccountEntity?> = db.userAccountDao().getActiveAccount()
    suspend fun insertAccount(account: UserAccountEntity) = db.userAccountDao().insertAccount(account)
    suspend fun clearAccount() = db.userAccountDao().clearAccount()

    // Registered Users (Coworker lookup)
    val allRegisteredUsers: Flow<List<RegisteredUserEntity>> = db.registeredUserDao().getAllRegisteredUsers()
    suspend fun getUserByEmail(email: String): RegisteredUserEntity? = db.registeredUserDao().getUserByEmail(email)
    suspend fun insertRegisteredUser(user: RegisteredUserEntity) = db.registeredUserDao().insertRegisteredUser(user)

    // Work Groups
    val allGroups: Flow<List<WorkGroupEntity>> = db.workGroupDao().getAllGroups()
    suspend fun insertGroup(group: WorkGroupEntity): Long = db.workGroupDao().insertGroup(group)
    suspend fun deleteGroupById(id: Int) = db.workGroupDao().deleteGroupById(id)

    // Work Group Members
    fun getMembersForGroup(groupId: Int): Flow<List<WorkGroupMemberEntity>> = db.workGroupMemberDao().getMembersForGroup(groupId)
    suspend fun insertMember(member: WorkGroupMemberEntity) = db.workGroupMemberDao().insertMember(member)
    suspend fun removeMember(groupId: Int, email: String) = db.workGroupMemberDao().removeMember(groupId, email)
}
