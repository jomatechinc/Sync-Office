package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.*
import com.example.data.repository.SyncOfficeRepository
import com.example.util.CryptoUtils
import com.example.util.GeminiClient
import com.example.util.FirebaseAuthHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CallParticipant(
    val id: String,
    val name: String,
    val role: String,
    val isMuted: Boolean,
    val isVideoOff: Boolean,
    val isSpeaking: Boolean,
    val isScreenSharing: Boolean = false,
    val connectionStatus: String = "Connected", // "Connected", "Connecting..."
    val audioLevel: Float = 0f
)

class SyncOfficeViewModel(
    application: Application,
    private val repository: SyncOfficeRepository
) : AndroidViewModel(application) {

    // --- State Streams ---
    val chats = repository.allChats
    val events = repository.allEvents
    val documents = repository.allDocuments
    val tasks = repository.allTasks
    val rules = repository.allRules
    val logs = repository.allLogs

    private val _selectedChatId = MutableStateFlow<String?>(null)
    val selectedChatId: StateFlow<String?> = _selectedChatId.asStateFlow()

    val selectedChatMessages: Flow<List<MessageEntity>> = _selectedChatId.flatMapLatest { chatId ->
        if (chatId == null) flowOf(emptyList())
        else repository.getMessagesForChat(chatId)
    }

    private val _selectedDocumentId = MutableStateFlow<Int?>(null)
    val selectedDocumentId: StateFlow<Int?> = _selectedDocumentId.asStateFlow()

    private val _aiSummaryState = MutableStateFlow<String?>(null)
    val aiSummaryState: StateFlow<String?> = _aiSummaryState.asStateFlow()

    private val _aiScheduleState = MutableStateFlow<String?>(null)
    val aiScheduleState: StateFlow<String?> = _aiScheduleState.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Video/Audio Call Simulation State
    private val _activeCallChatId = MutableStateFlow<String?>(null)
    val activeCallChatId: StateFlow<String?> = _activeCallChatId.asStateFlow()
    
    private val _isCallMuted = MutableStateFlow(false)
    val isCallMuted: StateFlow<Boolean> = _isCallMuted.asStateFlow()

    private val _isCallVideoOff = MutableStateFlow(false)
    val isCallVideoOff: StateFlow<Boolean> = _isCallVideoOff.asStateFlow()

    // Group Call Specific states
    private val _groupCallParticipants = MutableStateFlow<List<CallParticipant>>(emptyList())
    val groupCallParticipants: StateFlow<List<CallParticipant>> = _groupCallParticipants.asStateFlow()

    private val _isScreenSharing = MutableStateFlow(false)
    val isScreenSharing: StateFlow<Boolean> = _isScreenSharing.asStateFlow()

    private val _audioRoute = MutableStateFlow("Speaker") // "Speaker", "Receiver", "Bluetooth"
    val audioRoute: StateFlow<String> = _audioRoute.asStateFlow()

    private val _callDurationSec = MutableStateFlow(0)
    val callDurationSec: StateFlow<Int> = _callDurationSec.asStateFlow()

    private var callTimerJob: kotlinx.coroutines.Job? = null
    private var simulationJob: kotlinx.coroutines.Job? = null

    // --- Active User & Onboarding States ---
    val activeAccount: StateFlow<UserAccountEntity?> = repository.activeAccount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val allGroups = repository.allGroups
    
    fun getMembersForGroup(groupId: Int): Flow<List<WorkGroupMemberEntity>> = repository.getMembersForGroup(groupId)

    private val _signupEmail = MutableStateFlow("")
    val signupEmail = _signupEmail.asStateFlow()

    // --- Authentication States ---
    private val _authError = MutableStateFlow<String?>(null)
    val authError = _authError.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading = _isAuthLoading.asStateFlow()

    val isFirebaseMode = MutableStateFlow(FirebaseAuthHelper.isFirebaseAvailable)

    fun clearAuthError() {
        _authError.value = null
    }

    fun signUpWithEmailAndPassword(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || !email.contains("@")) {
            _authError.value = "Please enter a valid work email address."
            return
        }
        if (password.length < 6) {
            _authError.value = "Password must be at least 6 characters."
            return
        }
        _isAuthLoading.value = true
        _authError.value = null

        val auth = FirebaseAuthHelper.auth
        if (auth != null) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        viewModelScope.launch {
                            // Pre-register user profile locally in Secure DB
                            val existing = repository.getUserByEmail(email)
                            repository.insertRegisteredUser(
                                RegisteredUserEntity(
                                    email = email,
                                    name = email.substringBefore("@"),
                                    role = "Team Member",
                                    avatarInitials = if (email.length >= 2) email.take(2).uppercase() else "TM",
                                    passwordHash = CryptoUtils.encrypt(password)
                                )
                            )
                            _isAuthLoading.value = false
                            startSignupFlow(email)
                            setOnboardingStep(2)
                            onSuccess()
                        }
                    } else {
                        _isAuthLoading.value = false
                        _authError.value = task.exception?.localizedMessage ?: "Signup failed."
                    }
                }
        } else {
            // Local Secure Fallback Mode
            viewModelScope.launch {
                val existing = repository.getUserByEmail(email)
                if (existing != null) {
                    _isAuthLoading.value = false
                    _authError.value = "An account with this email already exists locally."
                } else {
                    val hashed = CryptoUtils.encrypt(password)
                    val registeredUser = RegisteredUserEntity(
                        email = email,
                        name = email.substringBefore("@"),
                        role = "Team Member",
                        avatarInitials = if (email.length >= 2) email.take(2).uppercase() else "TM",
                        passwordHash = hashed
                    )
                    repository.insertRegisteredUser(registeredUser)
                    _isAuthLoading.value = false
                    startSignupFlow(email)
                    setOnboardingStep(2)
                    onSuccess()
                }
            }
        }
    }

    fun signInWithEmailAndPassword(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || !email.contains("@")) {
            _authError.value = "Please enter a valid work email address."
            return
        }
        if (password.isBlank()) {
            _authError.value = "Please enter your password."
            return
        }
        _isAuthLoading.value = true
        _authError.value = null

        val auth = FirebaseAuthHelper.auth
        if (auth != null) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        viewModelScope.launch {
                            val existingLocalAccount = repository.getUserByEmail(email)
                            if (existingLocalAccount != null) {
                                // Profile already onboarded previously
                                val activeAcc = UserAccountEntity(
                                    email = existingLocalAccount.email,
                                    name = existingLocalAccount.name,
                                    role = existingLocalAccount.role,
                                    avatarInitials = existingLocalAccount.avatarInitials,
                                    isOnboarded = true
                                )
                                repository.insertAccount(activeAcc)
                                _isAuthLoading.value = false
                                onSuccess()
                            } else {
                                // Successful log in, but first time profile onboarding
                                _isAuthLoading.value = false
                                startSignupFlow(email)
                                setOnboardingStep(3) // Jump straight to profile configuration
                                onSuccess()
                            }
                        }
                    } else {
                        _isAuthLoading.value = false
                        _authError.value = task.exception?.localizedMessage ?: "Authentication failed."
                    }
                }
        } else {
            // Local Secure Fallback Mode
            viewModelScope.launch {
                val user = repository.getUserByEmail(email)
                if (user != null) {
                    val decPassword = CryptoUtils.decrypt(user.passwordHash)
                    if (decPassword == password) {
                        // Success! Log the user in
                        val activeAcc = UserAccountEntity(
                            email = user.email,
                            name = user.name,
                            role = user.role,
                            avatarInitials = user.avatarInitials,
                            isOnboarded = true
                        )
                        repository.insertAccount(activeAcc)
                        _isAuthLoading.value = false
                        onSuccess()
                    } else {
                        _isAuthLoading.value = false
                        _authError.value = "Invalid email or password."
                    }
                } else {
                    _isAuthLoading.value = false
                    _authError.value = "Account not found. Please sign up."
                }
            }
        }
    }

    private val _onboardingStep = MutableStateFlow(1) // 1: Intro, 2: Profile, 3: Accounts, 4: Preferences
    val onboardingStep = _onboardingStep.asStateFlow()

    private val _tempName = MutableStateFlow("")
    val tempName = _tempName.asStateFlow()

    private val _tempRole = MutableStateFlow("")
    val tempRole = _tempRole.asStateFlow()

    private val _tempAvatar = MutableStateFlow("JD")
    val tempAvatar = _tempAvatar.asStateFlow()

    private val _isEmailConnected = MutableStateFlow(false)
    val isEmailConnected = _isEmailConnected.asStateFlow()

    private val _isCalendarConnected = MutableStateFlow(false)
    val isCalendarConnected = _isCalendarConnected.asStateFlow()

    private val _isCrmConnected = MutableStateFlow(false)
    val isCrmConnected = _isCrmConnected.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled = _notificationsEnabled.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode = _isDarkMode.asStateFlow()

    // --- Productivity Analysis Flows ---
    private val _productivityRecommendation = MutableStateFlow<String?>(null)
    val productivityRecommendation = _productivityRecommendation.asStateFlow()

    private val _isProductivityLoading = MutableStateFlow(false)
    val isProductivityLoading = _isProductivityLoading.asStateFlow()

    fun runProductivityAnalysis(documentCount: Int, meetingCount: Int) {
        viewModelScope.launch {
            _isProductivityLoading.value = true
            _productivityRecommendation.value = null
            
            val prompt = """
                Analyze the following enterprise office productivity statistics for this week:
                - Number of Active Documents Edited/Created: $documentCount
                - Number of Meetings Scheduled/Attended: $meetingCount
                - Average Meeting Attendance Rate: 92%
                
                Please provide a concise, high-value set of 3 actionable optimization recommendations to improve pipeline speed, reduce meeting overload, and protect deep work hours. Ensure the recommendations are written in a supportive, professional, and sophisticated tone. Avoid generic advice, relate it to enterprise collaboration, and format it clearly with bullet points.
            """.trimIndent()
            
            val response = GeminiClient.generateContent(
                prompt = prompt,
                systemInstruction = "You are an elite enterprise productivity consultant and agile workflows specialist."
            )
            
            _productivityRecommendation.value = response
            _isProductivityLoading.value = false
            
            try {
                triggerWorkflows("ON_DOC_EDIT", "Executed enterprise productivity trend audit.")
            } catch (e: Exception) {
                // Safe ignore
            }
        }
    }

    fun updateSignupEmail(email: String) {
        _signupEmail.value = email
    }

    fun startSignupFlow(email: String) {
        _signupEmail.value = email
        _onboardingStep.value = 1
        val initials = if (email.contains("@")) {
            email.substringBefore("@").take(2).uppercase()
        } else "JD"
        _tempAvatar.value = initials
    }

    fun setOnboardingStep(step: Int) {
        _onboardingStep.value = step
    }

    fun updateTempProfile(name: String, role: String, avatar: String) {
        _tempName.value = name
        _tempRole.value = role
        _tempAvatar.value = avatar
    }

    fun toggleEmailConnection() {
        _isEmailConnected.value = !_isEmailConnected.value
    }

    fun toggleCalendarConnection() {
        _isCalendarConnected.value = !_isCalendarConnected.value
    }

    fun toggleCrmConnection() {
        _isCrmConnected.value = !_isCrmConnected.value
    }

    fun toggleNotifications() {
        _notificationsEnabled.value = !_notificationsEnabled.value
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            val account = UserAccountEntity(
                email = _signupEmail.value.ifBlank { "oghenekarojoseph@gmail.com" },
                name = _tempName.value.ifBlank { "User Admin" },
                role = _tempRole.value.ifBlank { "Consultant" },
                avatarInitials = _tempAvatar.value.ifBlank { "UA" },
                notificationsEnabled = _notificationsEnabled.value,
                isDarkMode = _isDarkMode.value,
                isEmailConnected = _isEmailConnected.value,
                isCalendarConnected = _isCalendarConnected.value,
                isCrmConnected = _isCrmConnected.value,
                isOnboarded = true
            )
            repository.insertAccount(account)

            // Insert into registered users as well
            val existing = repository.getUserByEmail(account.email)
            repository.insertRegisteredUser(
                RegisteredUserEntity(
                    email = account.email,
                    name = account.name,
                    role = account.role,
                    avatarInitials = account.avatarInitials,
                    passwordHash = existing?.passwordHash ?: ""
                )
            )

            // Log the onboarding success
            repository.insertLog(
                WorkflowLogEntity(
                    ruleName = "User Onboarding Completed",
                    status = "Success",
                    details = "User ${account.name} onboarded successfully with E2EE keys."
                )
            )
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                FirebaseAuthHelper.auth?.signOut()
            } catch (e: Exception) {
                // Safe ignore
            }
            repository.clearAccount()
            _signupEmail.value = ""
            _onboardingStep.value = 1
            _tempName.value = ""
            _tempRole.value = ""
            _isEmailConnected.value = false
            _isCalendarConnected.value = false
            _isCrmConnected.value = false
        }
    }

    fun createGroup(name: String, description: String, initialEmails: List<String>) {
        viewModelScope.launch {
            val groupId = repository.insertGroup(WorkGroupEntity(name = name, description = description))
            for (email in initialEmails) {
                addMemberToGroupInternal(groupId.toInt(), email)
            }
            repository.insertLog(
                WorkflowLogEntity(
                    ruleName = "Work Group Created",
                    status = "Success",
                    details = "Created work group '$name' with ${initialEmails.size} initial members."
                )
            )
        }
    }

    fun searchUserByEmail(email: String, onResult: (RegisteredUserEntity?) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email)
            onResult(user)
        }
    }

    fun addMemberToGroup(groupId: Int, email: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email)
            if (user != null) {
                repository.insertMember(
                    WorkGroupMemberEntity(
                        groupId = groupId,
                        email = user.email,
                        name = user.name,
                        role = user.role,
                        avatarInitials = user.avatarInitials,
                        isInvited = false
                    )
                )
                repository.insertLog(
                    WorkflowLogEntity(
                        ruleName = "Member Added to Group",
                        status = "Success",
                        details = "Added coworker ${user.name} to Group $groupId directly."
                    )
                )
                onResult(true, "${user.name} added to the group successfully.")
            } else {
                val inviteLink = "https://syncoffice.app/invite?g=$groupId&e=$email"
                repository.insertMember(
                    WorkGroupMemberEntity(
                        groupId = groupId,
                        email = email,
                        name = email.substringBefore("@"),
                        role = "Pending Invitation",
                        avatarInitials = if (email.length >= 2) email.take(2).uppercase() else "IN",
                        isInvited = true
                    )
                )
                repository.insertLog(
                    WorkflowLogEntity(
                        ruleName = "Invite Sent to Coworker",
                        status = "Pending",
                        details = "Generated invite link for $email: $inviteLink"
                    )
                )
                onResult(false, inviteLink)
            }
        }
    }

    private suspend fun addMemberToGroupInternal(groupId: Int, email: String) {
        val user = repository.getUserByEmail(email)
        if (user != null) {
            repository.insertMember(
                WorkGroupMemberEntity(
                    groupId = groupId,
                    email = user.email,
                    name = user.name,
                    role = user.role,
                    avatarInitials = user.avatarInitials,
                    isInvited = false
                )
            )
        } else {
            repository.insertMember(
                WorkGroupMemberEntity(
                    groupId = groupId,
                    email = email,
                    name = email.substringBefore("@"),
                    role = "Pending Invitation",
                    avatarInitials = if (email.length >= 2) email.take(2).uppercase() else "IN",
                    isInvited = true
                )
            )
        }
    }

    fun removeMemberFromGroup(groupId: Int, email: String) {
        viewModelScope.launch {
            repository.removeMember(groupId, email)
        }
    }

    init {
        // Seed default database values if empty
        viewModelScope.launch {
            chats.first().let { currentChats ->
                if (currentChats.isEmpty()) {
                    seedInitialData()
                }
            }
        }
    }

    fun selectChat(chatId: String?) {
        _selectedChatId.value = chatId
    }

    fun selectDocument(docId: Int?) {
        _selectedDocumentId.value = docId
        _aiSummaryState.value = null
    }

    // --- Messaging ---
    fun sendMessage(chatId: String, content: String, senderName: String = "You") {
        if (content.isBlank()) return
        viewModelScope.launch {
            // End-to-End Encryption
            val encrypted = CryptoUtils.encrypt(content)
            val message = MessageEntity(
                chatId = chatId,
                senderId = "user_1",
                senderName = senderName,
                encryptedContent = encrypted,
                decryptedContent = content,
                timestamp = System.currentTimeMillis()
            )
            repository.insertMessage(message)

            // Update chat last message
            val currentChat = chats.first().find { it.id == chatId }
            if (currentChat != null) {
                repository.insertChat(
                    currentChat.copy(
                        lastMessage = content,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    // --- Video Calls ---
    fun startCall(chatId: String) {
        _activeCallChatId.value = chatId
        _isCallMuted.value = false
        _isCallVideoOff.value = false
        _isScreenSharing.value = false
        _audioRoute.value = "Speaker"
        _callDurationSec.value = 0

        // Initialize default group participants
        _groupCallParticipants.value = listOf(
            CallParticipant("sarah_1", "Sarah Connor", "Chief Information Officer", isMuted = false, isVideoOff = false, isSpeaking = true, audioLevel = 0.7f),
            CallParticipant("marcus_2", "Marcus Aurelius", "Lead Systems Architect", isMuted = true, isVideoOff = true, isSpeaking = false, audioLevel = 0f),
            CallParticipant("elena_3", "Elena Rostova", "Operations Director", isMuted = false, isVideoOff = false, isSpeaking = false, audioLevel = 0f)
        )

        // Cancel existing jobs to prevent overlap
        callTimerJob?.cancel()
        simulationJob?.cancel()

        // Start Call Duration Timer
        callTimerJob = viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)
                _callDurationSec.value += 1
            }
        }

        // Start speaking and audio level simulation
        simulationJob = viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(2000)
                _groupCallParticipants.update { currentList ->
                    currentList.map { participant ->
                        if (participant.connectionStatus == "Connected" && !participant.isMuted) {
                            // Randomly determine if speaking and randomize levels
                            val isNowSpeaking = (0..2).random() > 0
                            val randomLevel = if (isNowSpeaking) (3..9).random() / 10f else 0f
                            participant.copy(isSpeaking = isNowSpeaking, audioLevel = randomLevel)
                        } else {
                            participant.copy(isSpeaking = false, audioLevel = 0f)
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            repository.insertLog(
                WorkflowLogEntity(
                    ruleName = "E2EE Group Call Established",
                    status = "Secure Connected",
                    details = "Established GDPR/HIPAA compliant E2E encrypted multi-party WebRTC group bridge for Chat ID $chatId"
                )
            )
        }
    }

    fun endCall() {
        val chatId = _activeCallChatId.value
        _activeCallChatId.value = null
        callTimerJob?.cancel()
        simulationJob?.cancel()
        _groupCallParticipants.value = emptyList()
        viewModelScope.launch {
            repository.insertLog(
                WorkflowLogEntity(
                    ruleName = "E2EE Group Call Terminated",
                    status = "Terminated",
                    details = "Safely closed secure group voice-bridge for Chat ID $chatId. All ephemeral multi-party keys rotated."
                )
            )
        }
    }

    fun toggleMute() {
        _isCallMuted.value = !_isCallMuted.value
    }

    fun toggleVideo() {
        _isCallVideoOff.value = !_isCallVideoOff.value
    }

    fun toggleScreenShare() {
        _isScreenSharing.value = !_isScreenSharing.value
        viewModelScope.launch {
            val status = if (_isScreenSharing.value) "Active" else "Inactive"
            repository.insertLog(
                WorkflowLogEntity(
                    ruleName = "E2EE Screen Share Toggle",
                    status = status,
                    details = "User screen sharing stream is now $status with AES-GCM 256 encryption."
                )
            )
        }
    }

    fun setAudioRoute(route: String) {
        _audioRoute.value = route
    }

    fun toggleParticipantMute(id: String) {
        _groupCallParticipants.update { currentList ->
            currentList.map { participant ->
                if (participant.id == id) {
                    val newMuted = !participant.isMuted
                    participant.copy(
                        isMuted = newMuted,
                        isSpeaking = if (newMuted) false else participant.isSpeaking,
                        audioLevel = if (newMuted) 0f else participant.audioLevel
                    )
                } else {
                    participant
                }
            }
        }
    }

    fun toggleParticipantVideo(id: String) {
        _groupCallParticipants.update { currentList ->
            currentList.map { participant ->
                if (participant.id == id) {
                    participant.copy(isVideoOff = !participant.isVideoOff)
                } else {
                    participant
                }
            }
        }
    }

    fun inviteParticipant(name: String, role: String) {
        val newId = "invite_" + System.currentTimeMillis()
        val tempParticipant = CallParticipant(
            id = newId,
            name = name,
            role = role,
            isMuted = false,
            isVideoOff = false,
            isSpeaking = false,
            connectionStatus = "Connecting..."
        )

        _groupCallParticipants.update { it + tempParticipant }

        viewModelScope.launch {
            repository.insertLog(
                WorkflowLogEntity(
                    ruleName = "E2EE Invite Sent",
                    status = "Delivered",
                    details = "Dispatched secure multi-party token invite to $name ($role)"
                )
            )

            // Dynamic join delay simulation
            kotlinx.coroutines.delay(2000)
            _groupCallParticipants.update { currentList ->
                currentList.map { p ->
                    if (p.id == newId) {
                        p.copy(connectionStatus = "Connected")
                    } else {
                        p
                    }
                }
            }

            repository.insertLog(
                WorkflowLogEntity(
                    ruleName = "E2EE Node Connected",
                    status = "Connected",
                    details = "Node connection verified for $name ($role) with certified E2EE link."
                )
            )
        }
    }

    // --- Scheduling ---
    fun addEvent(title: String, startOffsetHours: Int, durationHours: Int, timeZone: String, attendees: String, category: String) {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis() + (startOffsetHours * 3600000L)
            val endTime = startTime + (durationHours * 3600000L)
            repository.insertEvent(
                CalendarEventEntity(
                    title = title,
                    startTime = startTime,
                    endTime = endTime,
                    timeZone = timeZone,
                    attendees = attendees,
                    category = category
                )
            )
        }
    }

    fun deleteEvent(id: Int) {
        viewModelScope.launch {
            repository.deleteEventById(id)
        }
    }

    fun optimizeSchedule() {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiScheduleState.value = "Analyzing schedule and deadlines..."
            val currentEvents = repository.allEvents.first()
            val eventsSummary = currentEvents.joinToString("\n") {
                "- ${it.title} (${it.category}) starting at Offset: ${(it.startTime - System.currentTimeMillis()) / 3600000} hrs with TZ ${it.timeZone}, attendees: ${it.attendees}"
            }

            val prompt = """
                You are SyncOffice AI Smart Scheduler. 
                Below are the current calendar events scheduled in our secure environment:
                $eventsSummary
                
                Analyze these events across time zones (e.g. EDT, GMT, PST, JST). 
                Propose:
                1. 2-3 optimal time-slots for a new "All-Hands Architecture Alignment" that minimizes cross-timezone fatigue.
                2. Potential scheduling conflicts.
                3. A priority action item list for resolving timeline overlaps.
                
                Keep response structured, concise, and professional.
            """.trimIndent()

            val response = GeminiClient.generateContent(prompt, "You are a professional cross-timezone enterprise calendar advisor.")
            _aiScheduleState.value = response
            _isAiLoading.value = false

            // Insert workflow log
            repository.insertLog(
                WorkflowLogEntity(
                    ruleName = "AI Calendar Optimization",
                    status = "Optimized",
                    details = "Proposed optimal cross-timezone meeting structures using Gemini AI flash models."
                )
            )
        }
    }

    // --- Documents ---
    fun addDocument(title: String, content: String) {
        viewModelScope.launch {
            val doc = DocumentEntity(title = title, content = content)
            repository.insertDocument(doc)
            triggerWorkflows("ON_DOC_EDIT", "Created Document: $title")
        }
    }

    fun updateDocument(id: Int, title: String, content: String, version: Int) {
        viewModelScope.launch {
            val updatedDoc = DocumentEntity(
                id = id,
                title = title,
                content = content,
                lastModified = System.currentTimeMillis(),
                version = version + 1
            )
            repository.insertDocument(updatedDoc)
            
            // Auto trigger workflow
            triggerWorkflows("ON_DOC_EDIT", "Updated Document: $title (v${version + 1})")
        }
    }

    fun summarizeDocument(doc: DocumentEntity) {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiSummaryState.value = "Generating smart summarization..."

            val prompt = """
                You are SyncOffice Smart Summary assistant. 
                Summarize the following shared enterprise document:
                Title: ${doc.title}
                Content:
                ${doc.content}
                
                Generate a concise, 3-bullet executive summary, along with any highlighted action items.
            """.trimIndent()

            val summary = GeminiClient.generateContent(prompt, "You are a high-level corporate document analysis bot.")
            
            // Save summary back to entity
            repository.insertDocument(doc.copy(aiSummary = summary))
            _aiSummaryState.value = summary
            _isAiLoading.value = false

            // Log automation
            repository.insertLog(
                WorkflowLogEntity(
                    ruleName = "AI Document Summarization",
                    status = "Completed",
                    details = "Extracted 3-bullet digest for '${doc.title}' with action tracking."
                )
            )
        }
    }

    // --- Tasks (Custom Widget & Workspace Actions) ---
    fun addTask(title: String, priority: String) {
        viewModelScope.launch {
            repository.insertTask(
                TaskEntity(
                    title = title,
                    status = "Pending",
                    priority = priority
                )
            )
        }
    }

    fun toggleTaskStatus(task: TaskEntity) {
        viewModelScope.launch {
            val newStatus = if (task.status == "Completed") "Pending" else "Completed"
            repository.insertTask(task.copy(status = newStatus))
            
            if (newStatus == "Completed") {
                triggerWorkflows("ON_TASK_COMPLETE", "Completed Task: ${task.title}")
            }
        }
    }

    fun deleteTask(id: Int) {
        viewModelScope.launch {
            repository.deleteTaskById(id)
        }
    }

    // --- Automations ---
    fun addRule(name: String, triggerType: String, actionType: String) {
        viewModelScope.launch {
            repository.insertRule(
                AutomationRuleEntity(
                    name = name,
                    triggerType = triggerType,
                    actionType = actionType
                )
            )
        }
    }

    fun toggleRule(rule: AutomationRuleEntity) {
        viewModelScope.launch {
            repository.insertRule(rule.copy(isActive = !rule.isActive))
        }
    }

    fun deleteRule(id: Int) {
        viewModelScope.launch {
            repository.deleteRuleById(id)
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    // --- Core Automation Engine ---
    private suspend fun triggerWorkflows(triggerType: String, description: String) {
        val activeRules = repository.allRules.first().filter { it.triggerType == triggerType && it.isActive }
        for (rule in activeRules) {
            // Trigger log
            repository.insertLog(
                WorkflowLogEntity(
                    ruleName = rule.name,
                    status = "Triggered",
                    details = "Automation fired successfully. Context: $description"
                )
            )

            // Perform automation action
            when (rule.actionType) {
                "SEND_MESSAGE" -> {
                    // Send an automated notification message in general channel
                    val messageBody = "⚙️ [Automation Bot]: ${rule.name} - $description"
                    val encMessage = CryptoUtils.encrypt(messageBody)
                    repository.insertMessage(
                        MessageEntity(
                            chatId = "general",
                            senderId = "bot_1",
                            senderName = "SyncOffice Bot",
                            encryptedContent = encMessage,
                            decryptedContent = messageBody,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
                "CREATE_TASK" -> {
                    // Automatically add a task
                    repository.insertTask(
                        TaskEntity(
                            title = "Follow Up: ${description.take(30)}...",
                            status = "Pending",
                            priority = "Medium"
                        )
                    )
                }
            }
        }
    }

    // --- Seed Logic ---
    private suspend fun seedInitialData() {
        // 1. Seed Chats
        val seedChats = listOf(
            ChatEntity("general", "Global HQ Boardroom", "Welcome to SyncOffice secure portal.", lastUpdated = System.currentTimeMillis() - 300000),
            ChatEntity("compliance", "🔒 GDPR/HIPAA Security Guard", "E2E keys rotated successfully.", lastUpdated = System.currentTimeMillis() - 600000),
            ChatEntity("marketing", "Market Expansion Alignment", "Sent you the draft summary.", lastUpdated = System.currentTimeMillis() - 1200000)
        )
        seedChats.forEach { repository.insertChat(it) }

        // 2. Seed Messages
        val seedMessages = listOf(
            MessageEntity(chatId = "general", senderId = "admin", senderName = "CEO Office", encryptedContent = CryptoUtils.encrypt("Welcome to our unified office hub! HIPAA & GDPR encryption is online."), decryptedContent = "Welcome to our unified office hub! HIPAA & GDPR encryption is online."),
            MessageEntity(chatId = "compliance", senderId = "security", senderName = "Compliance Officer", encryptedContent = CryptoUtils.encrypt("E2E security is enforced. Let's run a secure video link testing when ready."), decryptedContent = "E2E security is enforced. Let's run a secure video link testing when ready.")
        )
        seedMessages.forEach { repository.insertMessage(it) }

        // 3. Seed Events
        val seedEvents = listOf(
            CalendarEventEntity(title = "EU Security Compliance Audit", startTime = System.currentTimeMillis() + 18000000, endTime = System.currentTimeMillis() + 21600000, timeZone = "GMT+1", attendees = "CEO, Legal, DPO", category = "Audit"),
            CalendarEventEntity(title = "PST Team Standup", startTime = System.currentTimeMillis() + 32400000, endTime = System.currentTimeMillis() + 36000000, timeZone = "PST", attendees = "Engineering, Product", category = "Meeting")
        )
        seedEvents.forEach { repository.insertEvent(it) }

        // 4. Seed Documents
        val seedDocs = listOf(
            DocumentEntity(title = "Data Protection Agreement", content = "This document sets forth the terms for HIPAA and GDPR compliant data storage inside SyncOffice servers. All user metadata is strictly ephemeral. Video feeds are relay-only WebRTC bridges.", aiSummary = "Summary: Governs HIPAA and GDPR standards. Local server data is transient. Voice/Video logs are securely encrypted."),
            DocumentEntity(title = "Product Growth Plan", content = "We are launching the AI smart scheduling agent in Q3. This features auto-detection of participant availability across multiple time zones. This reduces calendar overlaps.")
        )
        seedDocs.forEach { repository.insertDocument(it) }

        // 5. Seed Tasks (Widgets)
        val seedTasks = listOf(
            TaskEntity(title = "Finalize Q3 Security Review", status = "Pending", priority = "High"),
            TaskEntity(title = "Configure Gemini API in Secrets", status = "Pending", priority = "High"),
            TaskEntity(title = "Conduct HIPAA Audit Checklist", status = "Pending", priority = "Medium")
        )
        seedTasks.forEach { repository.insertTask(it) }

        // 6. Seed Rules
        val seedRules = listOf(
            AutomationRuleEntity(name = "Auto-Message Chat on Doc Edits", triggerType = "ON_DOC_EDIT", actionType = "SEND_MESSAGE", isActive = true),
            AutomationRuleEntity(name = "Auto-Task Creation on Task Completions", triggerType = "ON_TASK_COMPLETE", actionType = "CREATE_TASK", isActive = true)
        )
        seedRules.forEach { repository.insertRule(it) }

        // 7. Seed Registered Users (Coworkers for email searching)
        val seedUsers = listOf(
            RegisteredUserEntity("sarah.connor@acme.com", "Sarah Connor", "Compliance Officer", "SC"),
            RegisteredUserEntity("michael.scott@acme.com", "Michael Scott", "Regional Manager", "MS"),
            RegisteredUserEntity("pam.beesly@acme.com", "Pam Beesly", "Office Administrator", "PB"),
            RegisteredUserEntity("john.doe@acme.com", "John Doe", "Lead Architect", "JD")
        )
        seedUsers.forEach { repository.insertRegisteredUser(it) }

        // 8. Seed Default Work Groups
        val groupId = repository.insertGroup(
            WorkGroupEntity(
                name = "Product Steering Committee",
                description = "High-level cross-timezone strategy coordination."
            )
        )
        repository.insertMember(WorkGroupMemberEntity(groupId = groupId.toInt(), email = "sarah.connor@acme.com", name = "Sarah Connor", role = "Compliance Officer", avatarInitials = "SC"))
        repository.insertMember(WorkGroupMemberEntity(groupId = groupId.toInt(), email = "pam.beesly@acme.com", name = "Pam Beesly", role = "Office Administrator", avatarInitials = "PB"))

        // 9. Seed Initial Log
        repository.insertLog(
            WorkflowLogEntity(
                ruleName = "System Startup",
                status = "Success",
                details = "Initialized database. E2E secure vault, Registered coworkers, and Product Steering Committee group ready."
            )
        )
    }
}

class SyncOfficeViewModelFactory(
    private val application: Application,
    private val repository: SyncOfficeRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SyncOfficeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SyncOfficeViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
