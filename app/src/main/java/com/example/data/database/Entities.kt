package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,
    val name: String,
    val lastMessage: String,
    val isEncrypted: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val encryptedContent: String,
    val decryptedContent: String = "", // Local state after user decodes or auto-decrypted
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "calendar_events")
data class CalendarEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val timeZone: String,
    val attendees: String, // Comma-separated
    val category: String = "Meeting" // Meeting, Deadline, Personal
)

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val lastModified: Long = System.currentTimeMillis(),
    val modifiedBy: String = "You",
    val version: Int = 1,
    val aiSummary: String = ""
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val status: String, // Pending, Completed
    val priority: String, // High, Medium, Low
    val dueDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "automation_rules")
data class AutomationRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val triggerType: String, // e.g. "ON_DOC_EDIT", "ON_TASK_COMPLETE"
    val actionType: String, // e.g. "SEND_MESSAGE", "CREATE_TASK"
    val isActive: Boolean = true
)

@Entity(tableName = "workflow_logs")
data class WorkflowLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ruleName: String,
    val status: String, // Success, Triggered
    val timestamp: Long = System.currentTimeMillis(),
    val details: String
)

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey val email: String,
    val name: String,
    val role: String,
    val avatarInitials: String = "JD",
    val notificationsEnabled: Boolean = true,
    val isDarkMode: Boolean = true,
    val isEmailConnected: Boolean = false,
    val isCalendarConnected: Boolean = false,
    val isCrmConnected: Boolean = false,
    val isOnboarded: Boolean = false
)

@Entity(tableName = "registered_users")
data class RegisteredUserEntity(
    @PrimaryKey val email: String,
    val name: String,
    val role: String,
    val avatarInitials: String,
    val passwordHash: String = ""
)

@Entity(tableName = "work_groups")
data class WorkGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "work_group_members")
data class WorkGroupMemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val groupId: Int,
    val email: String,
    val name: String,
    val role: String,
    val avatarInitials: String,
    val isInvited: Boolean = false
)

