package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.database.*
import com.example.ui.theme.*
import com.example.util.CryptoUtils
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncOfficeApp(viewModel: SyncOfficeViewModel) {
    val activeAccount by viewModel.activeAccount.collectAsStateWithLifecycle()

    if (activeAccount == null || !activeAccount!!.isOnboarded) {
        OnboardingFlowScreen(viewModel = viewModel)
    } else {
        var currentTab by remember { mutableStateOf("dashboard") }
        val activeCallChatId by viewModel.activeCallChatId.collectAsStateWithLifecycle()
        val chatsList by viewModel.chats.collectAsStateWithLifecycle(initialValue = emptyList())
        val activeCallChat = chatsList.find { it.id == activeCallChatId }
        var showProfileDialog by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Secure Shield",
                                tint = CosmicPrimary
                            )
                            Text(
                                text = "SyncOffice Pro",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    },
                    actions = {
                        Row(
                            modifier = Modifier.padding(end = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                color = SecurityCompliant.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(SecurityCompliant)
                                    )
                                    Text(
                                        text = "HIPAA SECURE",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = SecurityCompliant,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            // Dynamic Monogram Avatar
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CosmicPrimary)
                                    .clickable { showProfileDialog = true }
                                    .testTag("profile_avatar_monogram"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = activeAccount?.avatarInitials ?: "JD",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = Color(0xFF1D192B),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = CosmicBackground,
                        titleContentColor = Color.White
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = CosmicSurface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == "dashboard",
                        onClick = { currentTab = "dashboard" },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Dashboard") },
                        modifier = Modifier.testTag("nav_dashboard"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF1D192B),
                            selectedTextColor = CosmicTextPrimary,
                            indicatorColor = CosmicSecondary,
                            unselectedIconColor = CosmicTextSecondary,
                            unselectedTextColor = CosmicTextSecondary
                        )
                    )
                    NavigationBarItem(
                        selected = currentTab == "chat",
                        onClick = { currentTab = "chat" },
                        icon = { Icon(Icons.Default.Chat, contentDescription = "Messaging") },
                        label = { Text("Chat") },
                        modifier = Modifier.testTag("nav_chat"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF1D192B),
                            selectedTextColor = CosmicTextPrimary,
                            indicatorColor = CosmicSecondary,
                            unselectedIconColor = CosmicTextSecondary,
                            unselectedTextColor = CosmicTextSecondary
                        )
                    )
                    NavigationBarItem(
                        selected = currentTab == "calendar",
                        onClick = { currentTab = "calendar" },
                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Scheduling") },
                        label = { Text("Calendar") },
                        modifier = Modifier.testTag("nav_calendar"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF1D192B),
                            selectedTextColor = CosmicTextPrimary,
                            indicatorColor = CosmicSecondary,
                            unselectedIconColor = CosmicTextSecondary,
                            unselectedTextColor = CosmicTextSecondary
                        )
                    )
                    NavigationBarItem(
                        selected = currentTab == "documents",
                        onClick = { currentTab = "documents" },
                        icon = { Icon(Icons.Default.Description, contentDescription = "Collaboration") },
                        label = { Text("Docs") },
                        modifier = Modifier.testTag("nav_docs"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF1D192B),
                            selectedTextColor = CosmicTextPrimary,
                            indicatorColor = CosmicSecondary,
                            unselectedIconColor = CosmicTextSecondary,
                            unselectedTextColor = CosmicTextSecondary
                        )
                    )
                    NavigationBarItem(
                        selected = currentTab == "groups",
                        onClick = { currentTab = "groups" },
                        icon = { Icon(Icons.Default.Group, contentDescription = "Work Groups") },
                        label = { Text("Groups") },
                        modifier = Modifier.testTag("nav_groups"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF1D192B),
                            selectedTextColor = CosmicTextPrimary,
                            indicatorColor = CosmicSecondary,
                            unselectedIconColor = CosmicTextSecondary,
                            unselectedTextColor = CosmicTextSecondary
                        )
                    )
                    NavigationBarItem(
                        selected = currentTab == "workflows",
                        onClick = { currentTab = "workflows" },
                        icon = { Icon(Icons.Default.Autorenew, contentDescription = "Automation") },
                        label = { Text("Workflows") },
                        modifier = Modifier.testTag("nav_workflows"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF1D192B),
                            selectedTextColor = CosmicTextPrimary,
                            indicatorColor = CosmicSecondary,
                            unselectedIconColor = CosmicTextSecondary,
                            unselectedTextColor = CosmicTextSecondary
                        )
                    )
                }
            },
            containerColor = CosmicBackground
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Main Tab View
                when (currentTab) {
                    "dashboard" -> DashboardScreen(viewModel = viewModel)
                    "chat" -> ChatScreen(viewModel = viewModel)
                    "calendar" -> CalendarScreen(viewModel = viewModel)
                    "documents" -> DocumentScreen(viewModel = viewModel)
                    "groups" -> WorkGroupScreen(viewModel = viewModel)
                    "workflows" -> WorkflowScreen(viewModel = viewModel)
                }

                // Secure Overlay Video Call Simulator
                if (activeCallChat != null) {
                    VideoCallOverlay(
                        chat = activeCallChat,
                        onEndCall = { viewModel.endCall() },
                        viewModel = viewModel
                    )
                }
            }
        }

        // Executive Security Profile dialog
        if (showProfileDialog) {
            AlertDialog(
                onDismissRequest = { showProfileDialog = false },
                containerColor = CosmicSurface,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = CosmicPrimary)
                        Text("Enterprise Profile", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(CosmicPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = activeAccount?.avatarInitials ?: "JD",
                                    color = Color(0xFF1D192B),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                            Column {
                                Text(activeAccount?.name ?: "User", style = MaterialTheme.typography.titleSmall, color = Color.White)
                                Text(activeAccount?.email ?: "", style = MaterialTheme.typography.bodySmall, color = CosmicTextSecondary)
                                Text(activeAccount?.role ?: "", style = MaterialTheme.typography.bodySmall, color = CosmicSecondary, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Divider(color = CosmicSurfaceVariant, modifier = Modifier.padding(vertical = 4.dp))

                        Text("INTEGRATED SECURE PLATFORMS:", style = MaterialTheme.typography.labelSmall, color = CosmicPrimary)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Email Client Sync", style = MaterialTheme.typography.bodySmall, color = CosmicTextPrimary)
                            Text(
                                text = if (activeAccount?.isEmailConnected == true) "CONNECTED" else "NOT CONNECTED",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (activeAccount?.isEmailConnected == true) SecurityCompliant else CosmicTextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Exchange Calendar Sync", style = MaterialTheme.typography.bodySmall, color = CosmicTextPrimary)
                            Text(
                                text = if (activeAccount?.isCalendarConnected == true) "CONNECTED" else "NOT CONNECTED",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (activeAccount?.isCalendarConnected == true) SecurityCompliant else CosmicTextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("CRM Platform Sync", style = MaterialTheme.typography.bodySmall, color = CosmicTextPrimary)
                            Text(
                                text = if (activeAccount?.isCrmConnected == true) "CONNECTED" else "NOT CONNECTED",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (activeAccount?.isCrmConnected == true) SecurityCompliant else CosmicTextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Divider(color = CosmicSurfaceVariant, modifier = Modifier.padding(vertical = 4.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Alert Notifications", style = MaterialTheme.typography.bodySmall, color = Color.White)
                                Text("Receive E2EE alerts", style = MaterialTheme.typography.labelSmall, color = CosmicTextSecondary)
                            }
                            Switch(
                                checked = activeAccount?.notificationsEnabled ?: true,
                                onCheckedChange = { /* Simulated toggle */ },
                                colors = SwitchDefaults.colors(checkedThumbColor = CosmicPrimary)
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Clean Eye-Safe Canvas", style = MaterialTheme.typography.bodySmall, color = Color.White)
                                Text("Enforce dark minimalism theme", style = MaterialTheme.typography.labelSmall, color = CosmicTextSecondary)
                            }
                            Switch(
                                checked = activeAccount?.isDarkMode ?: true,
                                onCheckedChange = { /* Simulated theme switch */ },
                                colors = SwitchDefaults.colors(checkedThumbColor = CosmicPrimary)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showProfileDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicPrimary, contentColor = Color(0xFF1D192B))
                    ) {
                        Text("Dismiss")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showProfileDialog = false
                            viewModel.signOut()
                        }
                    ) {
                        Text("Sign Out", color = Color.Red.copy(alpha = 0.8f))
                    }
                }
            )
        }
    }
}

// --- RECENT ACTIVITY MODELS & COMPONENTS ---
data class RecentActivity(
    val id: String,
    val type: ActivityType,
    val title: String,
    val description: String,
    val timestamp: Long
)

enum class ActivityType {
    DOCUMENT_EDIT,
    CHAT_MESSAGE,
    MEETING
}

@Composable
fun RecentActivityRow(activity: RecentActivity) {
    val timeStr = remember(activity.timestamp) {
        val sdf = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
        sdf.format(Date(activity.timestamp))
    }

    val (icon, tintColor) = when (activity.type) {
        ActivityType.DOCUMENT_EDIT -> Pair(Icons.Default.Description, CosmicPrimary)
        ActivityType.CHAT_MESSAGE -> Pair(Icons.Default.Chat, CosmicSecondary)
        ActivityType.MEETING -> Pair(Icons.Default.Event, SecurityCompliant)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CosmicSurfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(12.dp)
            .testTag("recent_activity_row_${activity.id}"),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(tintColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tintColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = activity.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = activity.description,
                style = MaterialTheme.typography.bodySmall,
                color = CosmicTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = timeStr,
            style = MaterialTheme.typography.labelSmall,
            color = CosmicTextSecondary.copy(alpha = 0.8f)
        )
    }
}

// --- TAB 1: DASHBOARD ---
@Composable
fun DashboardScreen(viewModel: SyncOfficeViewModel) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle(initialValue = emptyList())
    val events by viewModel.events.collectAsStateWithLifecycle(initialValue = emptyList())
    val documents by viewModel.documents.collectAsStateWithLifecycle(initialValue = emptyList())
    val chats by viewModel.chats.collectAsStateWithLifecycle(initialValue = emptyList())

    val recentActivities = remember(documents, chats, events) {
        val list = mutableListOf<RecentActivity>()

        // Add document edits
        documents.forEach { doc ->
            list.add(
                RecentActivity(
                    id = "doc_${doc.id}",
                    type = ActivityType.DOCUMENT_EDIT,
                    title = doc.title,
                    description = "Edited by ${doc.modifiedBy} (v${doc.version})",
                    timestamp = doc.lastModified
                )
            )
        }

        // Add chat messages (from chats lastMessage/lastUpdated)
        chats.forEach { chat ->
            if (chat.lastMessage.isNotEmpty()) {
                list.add(
                    RecentActivity(
                        id = "chat_${chat.id}",
                        type = ActivityType.CHAT_MESSAGE,
                        title = "Message in #${chat.name}",
                        description = chat.lastMessage,
                        timestamp = chat.lastUpdated
                    )
                )
            }
        }

        // Add scheduled meetings
        events.forEach { event ->
            list.add(
                RecentActivity(
                    id = "event_${event.id}",
                    type = ActivityType.MEETING,
                    title = event.title,
                    description = "${event.category} • ${event.timeZone}",
                    timestamp = event.startTime
                )
            )
        }

        // Sort by timestamp descending so the most recent is first
        list.sortByDescending { it.timestamp }
        list
    }

    var activeSubTab by remember { mutableStateOf("Overview") }

    Row(modifier = Modifier.fillMaxSize()) {
        // Elegant Sidebar Navigation
        Column(
            modifier = Modifier
                .width(160.dp)
                .fillMaxHeight()
                .background(CosmicSurface)
                .padding(vertical = 16.dp, horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "DASHBOARD",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = CosmicTextSecondary,
                    letterSpacing = 1.25.sp
                ),
                modifier = Modifier.padding(start = 12.dp, bottom = 12.dp)
            )

            val navItems = listOf(
                Triple("Overview", Icons.Default.Dashboard, "nav_sub_overview"),
                Triple("Operations", Icons.Default.TaskAlt, "nav_sub_operations"),
                Triple("Schedule", Icons.Default.AccessTime, "nav_sub_schedule"),
                Triple("Compliance", Icons.Default.Shield, "nav_sub_compliance"),
                Triple("Integrations", Icons.Default.CloudSync, "nav_sub_integrations"),
                Triple("Productivity", Icons.Default.TrendingUp, "nav_sub_productivity")
            )

            navItems.forEach { (tabName, icon, tag) ->
                val isSelected = activeSubTab == tabName
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) CosmicPrimary.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable { activeSubTab = tabName }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .testTag(tag),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = tabName,
                        tint = if (isSelected) CosmicPrimary else CosmicTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = tabName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else CosmicTextSecondary
                        )
                    )
                }
            }
        }

        // Border line separating sidebar and main content
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .background(CosmicSurfaceVariant)
        )

        // Main Content Panel with Widget Grid Layout
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(CosmicBackground)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (activeSubTab) {
                    "Overview" -> {
                        // WIDGET GRID - OVERVIEW TAB
                        item {
                            // AI Insight Hero Banner (Clean Minimalist banner style)
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CosmicTertiary),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CosmicPrimary, modifier = Modifier.size(18.dp))
                                            Text(
                                                text = "AI CO-PILOT RECOMMENDATION",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    color = CosmicPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 1.25.sp
                                                )
                                            )
                                        }
                                        Text(
                                            text = "Active Node",
                                            style = MaterialTheme.typography.labelSmall.copy(color = SecurityCompliant, fontWeight = FontWeight.Bold)
                                        )
                                    }
                                    Text(
                                        text = "Your workspace has ${tasks.filter { it.status == "Pending" }.size} pending tasks. Today has ${events.size} scheduled events. Run cross-timezone check to optimize pipeline workflows.",
                                        style = MaterialTheme.typography.bodyLarge.copy(color = CosmicTextPrimary, fontWeight = FontWeight.Light, lineHeight = 20.sp)
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.optimizeSchedule() },
                                            colors = ButtonDefaults.buttonColors(containerColor = CosmicPrimary, contentColor = Color(0xFF1D192B)),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Optimize Workflows", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                        }
                                        OutlinedButton(
                                            onClick = {},
                                            border = BorderStroke(1.dp, CosmicSurfaceVariant),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CosmicPrimary)
                                        ) {
                                            Text("Dismiss", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // Grid widgets
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Column 1
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Security Status Widget
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Icon(Icons.Default.Shield, contentDescription = null, tint = SecurityCompliant, modifier = Modifier.size(18.dp))
                                                    Text("Security Shield", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                                }
                                                Surface(
                                                    color = SecurityCompliant.copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text(
                                                        "SECURE",
                                                        fontSize = 8.sp,
                                                        color = SecurityCompliant,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "E2E AES-128 payload encryption active. Storage nodes are HIPAA & GDPR verified.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = CosmicTextSecondary
                                            )
                                            Button(
                                                onClick = { viewModel.optimizeSchedule() },
                                                colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceVariant, contentColor = Color.White),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth().height(32.dp),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text("Trigger Hash Re-Audit", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    // Quick Account Synchronizer Widget
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Icon(Icons.Default.CloudSync, contentDescription = null, tint = CosmicSecondary, modifier = Modifier.size(18.dp))
                                                Text("Enterprise Accounts", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                            }
                                            val isEmailConn by viewModel.isEmailConnected.collectAsStateWithLifecycle()
                                            val isCalConn by viewModel.isCalendarConnected.collectAsStateWithLifecycle()
                                            val isCrmConn by viewModel.isCrmConnected.collectAsStateWithLifecycle()

                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                AccountSyncStatusItem("GSuite Email", isEmailConn)
                                                AccountSyncStatusItem("Office Calendar", isCalConn)
                                                AccountSyncStatusItem("Salesforce CRM", isCrmConn)
                                            }
                                        }
                                    }
                                }

                                // Column 2
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Task Tracker Widget
                                    var newTaskTitle by remember { mutableStateOf("") }
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Icon(Icons.Default.TaskAlt, contentDescription = null, tint = CosmicPrimary, modifier = Modifier.size(18.dp))
                                                    Text("Tasks Tracker", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                                }
                                                Surface(
                                                    color = CosmicPrimary.copy(alpha = 0.15f),
                                                    shape = CircleShape
                                                ) {
                                                    Text(
                                                        "${tasks.filter { it.status == "Pending" }.size} Active",
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                        style = MaterialTheme.typography.labelSmall.copy(color = CosmicPrimary, fontWeight = FontWeight.Bold)
                                                    )
                                                }
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                OutlinedTextField(
                                                    value = newTaskTitle,
                                                    onValueChange = { newTaskTitle = it },
                                                    placeholder = { Text("Add task...", fontSize = 11.sp, color = CosmicTextSecondary) },
                                                    modifier = Modifier.weight(1f).height(44.dp).testTag("dashboard_quick_task_input"),
                                                    textStyle = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = CosmicPrimary,
                                                        unfocusedBorderColor = CosmicSurfaceVariant
                                                    ),
                                                    singleLine = true
                                                )
                                                IconButton(
                                                    onClick = {
                                                        if (newTaskTitle.isNotBlank()) {
                                                            viewModel.addTask(newTaskTitle, "Medium")
                                                            newTaskTitle = ""
                                                        }
                                                    },
                                                    modifier = Modifier.size(36.dp).background(CosmicPrimary, RoundedCornerShape(8.dp))
                                                ) {
                                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                                }
                                            }

                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                if (tasks.isEmpty()) {
                                                    Text("No tasks added.", style = MaterialTheme.typography.bodySmall, color = CosmicTextSecondary)
                                                } else {
                                                    tasks.take(3).forEach { task ->
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .background(CosmicSurfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                                Checkbox(
                                                                    checked = task.status == "Completed",
                                                                    onCheckedChange = { viewModel.toggleTaskStatus(task) },
                                                                    colors = CheckboxDefaults.colors(checkedColor = SecurityCompliant)
                                                                )
                                                                Text(
                                                                    task.title,
                                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                                        textDecoration = if (task.status == "Completed") androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                                                                        color = if (task.status == "Completed") CosmicTextSecondary else Color.White
                                                                    ),
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Today's Meetings Widget
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = CosmicSecondary, modifier = Modifier.size(18.dp))
                                                Text("Today's Agenda", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                            }

                                            if (events.isEmpty()) {
                                                Text("No events scheduled.", style = MaterialTheme.typography.bodySmall, color = CosmicTextSecondary)
                                            } else {
                                                events.take(2).forEach { event ->
                                                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                                                    val startTimeStr = timeFormat.format(Date(event.startTime))
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column {
                                                            Text(event.title, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                                            Text("$startTimeStr • ${event.timeZone}", style = MaterialTheme.typography.bodySmall, color = CosmicTextSecondary)
                                                        }
                                                        Surface(color = CosmicSurfaceVariant, shape = RoundedCornerShape(6.dp)) {
                                                            Text(event.category, fontSize = 8.sp, color = CosmicSecondary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Recent Activity Widget
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.History,
                                                contentDescription = "Recent Activity",
                                                tint = CosmicPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = "Recent Activity Log",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White
                                            )
                                        }
                                        Surface(
                                            color = CosmicPrimary.copy(alpha = 0.15f),
                                            shape = CircleShape
                                        ) {
                                            Text(
                                                text = "${recentActivities.size} items",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(color = CosmicPrimary, fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }

                                    Divider(color = CosmicSurfaceVariant)

                                    if (recentActivities.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(
                                                    imageVector = Icons.Default.History,
                                                    contentDescription = null,
                                                    tint = CosmicTextSecondary,
                                                    modifier = Modifier.size(36.dp)
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = "No recent activity recorded.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = CosmicTextSecondary
                                                )
                                            }
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 240.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .verticalScroll(rememberScrollState()),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                recentActivities.forEach { activity ->
                                                    RecentActivityRow(activity)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "Operations" -> {
                        // OPERATIONS SUB-DASHBOARD
                        item {
                            Text("Workspace Operations Center", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                            Text("Active pipeline tasks and automatic background automation runs.", style = MaterialTheme.typography.bodySmall, color = CosmicTextSecondary)
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // All Tasks Card
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.weight(1.2f)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text("Full Pipeline Backlog", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                        if (tasks.isEmpty()) {
                                            Text("No tasks active.", style = MaterialTheme.typography.bodySmall, color = CosmicTextSecondary)
                                        } else {
                                            tasks.forEach { task ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(CosmicSurfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                        .padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                        Checkbox(
                                                            checked = task.status == "Completed",
                                                            onCheckedChange = { viewModel.toggleTaskStatus(task) },
                                                            colors = CheckboxDefaults.colors(checkedColor = SecurityCompliant)
                                                        )
                                                        Text(task.title, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                                                    }
                                                    Surface(
                                                        color = when(task.priority) {
                                                            "High" -> Color(0xFFEF4444).copy(alpha = 0.15f)
                                                            "Medium" -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                                                            else -> Color(0xFF10B981).copy(alpha = 0.15f)
                                                        },
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(task.priority, fontSize = 9.sp, color = if(task.priority == "High") Color(0xFFEF4444) else if(task.priority == "Medium") Color(0xFFF59E0B) else Color(0xFF10B981), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Automation Terminal
                                val logs by viewModel.logs.collectAsStateWithLifecycle(initialValue = emptyList())
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text("Decentralized Event Terminal", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                                .padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            if (logs.isEmpty()) {
                                                Text("> No system logs captured yet.", style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), color = CosmicTextSecondary)
                                            } else {
                                                logs.take(5).forEach { log ->
                                                    Text(
                                                        text = "> [${log.status}] ${log.ruleName} - ${log.details}",
                                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 10.sp),
                                                        color = if (log.status == "Success") SecurityCompliant else AutomationGlow
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "Schedule" -> {
                        // SCHEDULER WIDGETS
                        item {
                            Text("Workspace Scheduler Matrix", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                            Text("Coordinate cross-timezone calendar meetings and agenda lists securely.", style = MaterialTheme.typography.bodySmall, color = CosmicTextSecondary)
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Meetings card
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text("Today's Agenda Detail", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                        if (events.isEmpty()) {
                                            Text("No meetings scheduled today.", style = MaterialTheme.typography.bodySmall, color = CosmicTextSecondary)
                                        } else {
                                            events.forEach { event ->
                                                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                                                val startTimeStr = timeFormat.format(Date(event.startTime))
                                                val endTimeStr = timeFormat.format(Date(event.endTime))
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(CosmicSurfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                                        .padding(12.dp),
                                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                        Text(event.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                                        Surface(color = CosmicSurfaceVariant, shape = RoundedCornerShape(6.dp)) {
                                                            Text(event.category, fontSize = 8.sp, color = CosmicSecondary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                        }
                                                    }
                                                    Text("Time: $startTimeStr - $endTimeStr (${event.timeZone})", style = MaterialTheme.typography.bodySmall, color = CosmicTextSecondary)
                                                    Text("Attendees: ${event.attendees}", style = MaterialTheme.typography.bodySmall, color = CosmicTextSecondary)
                                                }
                                            }
                                        }
                                    }
                                }

                                // Timezone conversion helper
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text("Global Timezone Matrix", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                        TimezoneRow("New York (EST)", "UTC-5")
                                        TimezoneRow("London (GMT)", "UTC+0")
                                        TimezoneRow("Tokyo (JST)", "UTC+9")
                                        TimezoneRow("Sydney (AEST)", "UTC+10")
                                    }
                                }
                            }
                        }
                    }

                    "Compliance" -> {
                        // COMPLIANCE TELEMETRY MATRIX
                        item {
                            Text("HIPAA & GDPR Secure Telemetry", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                            Text("Encryption verification and security compliance certificates.", style = MaterialTheme.typography.bodySmall, color = CosmicTextSecondary)
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Compliance Checklist
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text("HIPAA Verification Matrix", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                        ComplianceCheckrow("End-to-End Payload Encryption", true)
                                        ComplianceCheckrow("Decentralized Blockchain Log", true)
                                        ComplianceCheckrow("Auto-Sanitizing SQLite Cache", true)
                                        ComplianceCheckrow("Multi-Factor Account Handshake", true)
                                        ComplianceCheckrow("Dynamic Key Rollover Cycles", true)
                                    }
                                }

                                // Ledger node stats
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text("Decentralized Node Identity", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                                .padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text("Node ID: SO-NODE-PRO-47C", style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), color = CosmicPrimary)
                                            Text("Encryption: AES-256 E2EE", style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), color = Color.White)
                                            Text("Protocol: Secure TLS 1.3", style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), color = Color.White)
                                            Text("Ledger Hash: 0x7E3A9D...4B2F", style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), color = CosmicTextSecondary)
                                        }
                                        Button(
                                            onClick = {},
                                            colors = ButtonDefaults.buttonColors(containerColor = SecurityCompliant, contentColor = Color(0xFF1D192B)),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Export Audit Certificate", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "Integrations" -> {
                        // INTEGRATIONS BRIDGE HUB
                        item {
                            Text("Active Enterprise API Bridges", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                            Text("Synchronize your calendar, messaging networks, and CRM tools securely.", style = MaterialTheme.typography.bodySmall, color = CosmicTextSecondary)
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                val isEmailConn by viewModel.isEmailConnected.collectAsStateWithLifecycle()
                                val isCalConn by viewModel.isCalendarConnected.collectAsStateWithLifecycle()
                                val isCrmConn by viewModel.isCrmConnected.collectAsStateWithLifecycle()

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text("Bridges Active State", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                        ConnectionBridgeRowCompact("GSuite & Exchange Email", isEmailConn) { viewModel.toggleEmailConnection() }
                                        ConnectionBridgeRowCompact("Corporate Calendar Sync", isCalConn) { viewModel.toggleCalendarConnection() }
                                        ConnectionBridgeRowCompact("HubSpot & Salesforce CRM", isCrmConn) { viewModel.toggleCrmConnection() }
                                    }
                                }

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text("Local API Gateway Stats", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Node Ping Latency", style = MaterialTheme.typography.bodySmall, color = CosmicTextSecondary)
                                            Text("4 ms", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = SecurityCompliant)
                                        }
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Active Sync Buffer", style = MaterialTheme.typography.bodySmall, color = CosmicTextSecondary)
                                            Text("0 pending items", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                        }
                                        Button(
                                            onClick = {},
                                            colors = ButtonDefaults.buttonColors(containerColor = CosmicPrimary, contentColor = Color(0xFF1D192B)),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Test Local Gateway", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "Productivity" -> {
                        item {
                            ProductivitySubScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccountSyncStatusItem(title: String, isConnected: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(if (isConnected) SecurityCompliant else CosmicTextSecondary))
            Text(title, style = MaterialTheme.typography.bodySmall, color = CosmicTextSecondary)
        }
        Text(
            text = if (isConnected) "CONNECTED" else "DISCONNECTED",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (isConnected) SecurityCompliant else CosmicTextSecondary
        )
    }
}

@Composable
fun TimezoneRow(zoneName: String, utcDiff: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CosmicSurfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(zoneName, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
        Text(utcDiff, style = MaterialTheme.typography.bodySmall, color = CosmicSecondary)
    }
}

@Composable
fun ComplianceCheckrow(checkName: String, verified: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(checkName, style = MaterialTheme.typography.bodySmall, color = Color.White)
        Icon(
            imageVector = if (verified) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = null,
            tint = if (verified) SecurityCompliant else Color.Red,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun ConnectionBridgeRowCompact(title: String, isConnected: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CosmicSurfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.bodySmall, color = Color.White)
            Text(
                text = if (isConnected) "CONNECTED" else "NOT CONNECTED",
                fontSize = 8.sp,
                color = if (isConnected) SecurityCompliant else CosmicTextSecondary,
                fontWeight = FontWeight.Bold
            )
        }
        IconButton(onClick = onToggle, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = if (isConnected) Icons.Default.LinkOff else Icons.Default.Link,
                contentDescription = null,
                tint = if (isConnected) Color.Red.copy(alpha = 0.7f) else CosmicPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// --- TAB 2: SECURE MESSAGING (CHATS) ---
@Composable
fun ChatScreen(viewModel: SyncOfficeViewModel) {
    val chats by viewModel.chats.collectAsStateWithLifecycle(initialValue = emptyList())
    val selectedChatId by viewModel.selectedChatId.collectAsStateWithLifecycle()
    val activeChat = chats.find { it.id == selectedChatId }

    if (activeChat == null) {
        // Chat List Screen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Secure Communication Hub",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Channels are end-to-end encrypted locally via AES-128. Video bridges are GDPR peer-to-peer compliant.",
                style = MaterialTheme.typography.bodySmall.copy(color = CosmicTextSecondary)
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(chats) { chat ->
                    Card(
                        onClick = { viewModel.selectChat(chat.id) },
                        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("chat_item_${chat.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = chat.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    if (chat.isEncrypted) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Encrypted",
                                            tint = SecurityCompliant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = chat.lastMessage,
                                    style = MaterialTheme.typography.bodyMedium.copy(color = CosmicTextSecondary),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Open Chat",
                                tint = CosmicSecondary
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Active Chat Conversation Screen
        val messages by viewModel.selectedChatMessages.collectAsStateWithLifecycle(initialValue = emptyList())
        var textInput by remember { mutableStateOf("") }
        var showEncrypted by remember { mutableStateOf(false) } // THE TOGGLE DEMO!

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Chat Header
            Surface(
                color = CosmicSurface,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(onClick = { viewModel.selectChat(null) }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Column {
                            Text(
                                text = activeChat.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(SecurityCompliant)
                                )
                                Text(
                                    text = "E2E AES Locked",
                                    style = MaterialTheme.typography.labelSmall.copy(color = SecurityCompliant)
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // WIRE FORMAT TOGGLE BUTTON
                        Button(
                            onClick = { showEncrypted = !showEncrypted },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (showEncrypted) AutomationGlow.copy(alpha = 0.2f) else CosmicSurfaceVariant,
                                contentColor = if (showEncrypted) AutomationGlow else Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (showEncrypted) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Wire Format",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (showEncrypted) "Wire Cipher" else "Decrypted", fontSize = 11.sp)
                        }

                        IconButton(
                            onClick = { viewModel.startCall(activeChat.id) },
                            modifier = Modifier.background(CosmicPrimary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.VideoCall, contentDescription = "Start Secure Video", tint = CosmicPrimary)
                        }
                    }
                }
            }

            // Message History Thread
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { message ->
                    val isMe = message.senderId == "user_1"
                    val contentToShow = if (showEncrypted) {
                        message.encryptedContent
                    } else {
                        message.decryptedContent.ifEmpty { CryptoUtils.decrypt(message.encryptedContent) }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                    ) {
                        Text(
                            text = message.senderName,
                            style = MaterialTheme.typography.labelSmall.copy(color = CosmicTextSecondary)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Surface(
                            color = if (isMe) CosmicPrimary else CosmicSurfaceVariant,
                            shape = RoundedCornerShape(
                                topStart = 12.dp,
                                topEnd = 12.dp,
                                bottomStart = if (isMe) 12.dp else 0.dp,
                                bottomEnd = if (isMe) 0.dp else 12.dp
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = contentToShow,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (isMe) Color.Black else Color.White,
                                        fontFamily = if (showEncrypted) FontFamily.Monospace else FontFamily.Default,
                                        fontSize = if (showEncrypted) 12.sp else 14.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Encrypted Badge",
                                        tint = if (isMe) Color.Black.copy(alpha = 0.6f) else SecurityCompliant,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Text(
                                        text = if (showEncrypted) "AES Payload" else "Verified Decrypted",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isMe) Color.Black.copy(alpha = 0.6f) else CosmicTextSecondary,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Input Row
            Surface(
                color = CosmicSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Enter E2E encrypted message...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicPrimary,
                            unfocusedBorderColor = CosmicSurfaceVariant
                        )
                    )

                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                viewModel.sendMessage(activeChat.id, textInput)
                                textInput = ""
                            }
                        },
                        modifier = Modifier
                            .background(CosmicPrimary, CircleShape)
                            .size(48.dp)
                            .testTag("send_message_button")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.Black)
                    }
                }
            }
        }
    }
}

// --- SECURE VIDEO CALL MODE OVERLAY ---

@Composable
fun AudioEqualizerWave(isSpeaking: Boolean, modifier: Modifier = Modifier) {
    var waveHeight1 by remember { mutableStateOf(4.dp) }
    var waveHeight2 by remember { mutableStateOf(12.dp) }
    var waveHeight3 by remember { mutableStateOf(6.dp) }

    if (isSpeaking) {
        LaunchedEffect(Unit) {
            while (true) {
                delay(150)
                waveHeight1 = (4..16).random().dp
                waveHeight2 = (4..16).random().dp
                waveHeight3 = (4..16).random().dp
            }
        }
    } else {
        waveHeight1 = 2.dp
        waveHeight2 = 2.dp
        waveHeight3 = 2.dp
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Box(modifier = Modifier.width(3.dp).height(waveHeight1).background(SecurityCompliant))
        Box(modifier = Modifier.width(3.dp).height(waveHeight2).background(SecurityCompliant))
        Box(modifier = Modifier.width(3.dp).height(waveHeight3).background(SecurityCompliant))
    }
}

@Composable
fun ScreenShareFeed() {
    var tick by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(800)
            tick = (tick + 1) % 10
        }
    }
    
    val terminalLines = listOf(
        "sys_agent_bridge: establishing websocket on secure node...",
        "GDPR compliance shield verified: AES-256-GCM key rotation success.",
        "HIPAA data protection lock: ENABLED.",
        "video_codec_h265: hardware acceleration active (4K 60fps).",
        "peer_connection: ICE candidates gathered, state = COMPLETED.",
        "security_integrity_check: local environment matches ISO-27001.",
        "monitoring_engine: latency < 12ms, packets lost = 0.00%.",
        "webrtc_media_stream: screen sharing buffer allocated (1080p).",
        "audio_pipeline: echo cancellation and noise reduction = 100%.",
        "sync_office_link: active group call broadcast online."
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .border(1.dp, CosmicPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Red))
                Text("LIVE SCREEN STREAMING", color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            }
            Text("AES-256 LINK", color = SecurityCompliant, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(10) { index ->
                val lineIndex = (tick + index) % terminalLines.size
                Text(
                    text = "> ${terminalLines[lineIndex]}",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = if (index == 9) CosmicPrimary else CosmicTextSecondary.copy(alpha = (index + 1) / 10f)
                )
            }
        }
    }
}

@Composable
fun ParticipantCard(
    participant: CallParticipant,
    onToggleMute: () -> Unit,
    onToggleVideo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val borderGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val isSpeaking = participant.isSpeaking && !participant.isMuted && participant.connectionStatus == "Connected"

    Card(
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(16.dp),
        border = if (isSpeaking) {
            BorderStroke(2.dp, SecurityCompliant.copy(alpha = borderGlowAlpha))
        } else {
            BorderStroke(1.dp, CosmicSurfaceVariant)
        },
        modifier = modifier
            .aspectRatio(1.2f)
            .fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (participant.connectionStatus == "Connecting...") {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = CosmicPrimary, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Securing Node...",
                        style = MaterialTheme.typography.labelSmall,
                        color = CosmicTextSecondary
                    )
                }
            } else if (participant.isVideoOff) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(CosmicSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = participant.name.split(" ").map { it.take(1) }.joinToString("").take(2).uppercase(),
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Camera Paused",
                        style = MaterialTheme.typography.labelSmall,
                        color = CosmicTextSecondary
                    )
                }
            } else {
                var wavePhase by remember { mutableStateOf(0f) }
                LaunchedEffect(Unit) {
                    while (true) {
                        delay(50)
                        wavePhase += 0.05f
                    }
                }
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val path = androidx.compose.ui.graphics.Path()
                    path.moveTo(0f, height / 2f)
                    for (x in 0..width.toInt() step 10) {
                        val y = height / 2f + Math.sin((x * 0.015f + wavePhase).toDouble()).toFloat() * 15f
                        path.lineTo(x.toFloat(), y)
                    }
                    path.lineTo(width, height)
                    path.lineTo(0f, height)
                    path.close()

                    drawPath(
                        path = path,
                        brush = Brush.verticalGradient(
                            colors = listOf(CosmicPrimary.copy(alpha = 0.15f), Color.Transparent),
                            startY = height / 2f,
                            endY = height
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.InterpreterMode,
                        contentDescription = null,
                        tint = CosmicPrimary.copy(alpha = 0.4f),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            if (participant.id != "user_me" && participant.connectionStatus == "Connected") {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onToggleMute,
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (participant.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Mute Remote",
                            tint = if (participant.isMuted) Color.Red else Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleVideo,
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (participant.isVideoOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                            contentDescription = "Video Remote",
                            tint = if (participant.isVideoOff) Color.Red else Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isSpeaking) {
                    AudioEqualizerWave(isSpeaking = true, modifier = Modifier.size(12.dp))
                } else if (participant.isMuted) {
                    Icon(Icons.Default.MicOff, contentDescription = "Muted", tint = Color.Red, modifier = Modifier.size(12.dp))
                }

                Column {
                    Text(
                        text = participant.name,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                    )
                    Text(
                        text = participant.role,
                        fontSize = 8.sp,
                        color = CosmicTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun VideoCallOverlay(
    chat: ChatEntity,
    onEndCall: () -> Unit,
    viewModel: SyncOfficeViewModel
) {
    val isMuted by viewModel.isCallMuted.collectAsStateWithLifecycle()
    val isVideoOff by viewModel.isCallVideoOff.collectAsStateWithLifecycle()
    val groupParticipants by viewModel.groupCallParticipants.collectAsStateWithLifecycle()
    val isScreenSharing by viewModel.isScreenSharing.collectAsStateWithLifecycle()
    val audioRoute by viewModel.audioRoute.collectAsStateWithLifecycle()
    val callDurationSec by viewModel.callDurationSec.collectAsStateWithLifecycle()

    var showInviteDialog by remember { mutableStateOf(false) }
    var inviteName by remember { mutableStateOf("") }
    var inviteRole by remember { mutableStateOf("") }
    var showAudioMenu by remember { mutableStateOf(false) }

    val allFeeds = remember(groupParticipants, isVideoOff, isMuted) {
        val userFeed = CallParticipant(
            id = "user_me",
            name = "You",
            role = "Enterprise Host",
            isMuted = isMuted,
            isVideoOff = isVideoOff,
            isSpeaking = !isMuted,
            audioLevel = if (isMuted) 0f else 0.5f,
            connectionStatus = "Connected"
        )
        listOf(userFeed) + groupParticipants
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .clickable(enabled = false) {}
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Info Row
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    color = SecurityCompliant.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = "Certified", tint = SecurityCompliant, modifier = Modifier.size(16.dp))
                        Text(
                            text = "SECURE E2EE WORKSPACE VOICE-BRIDGE",
                            style = MaterialTheme.typography.labelSmall.copy(color = SecurityCompliant, fontWeight = FontWeight.Bold)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = chat.name,
                    style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
                )
                
                val minutes = callDurationSec / 60
                val seconds = callDurationSec % 60
                val durationText = String.format("%02d:%02d", minutes, seconds)
                Text(
                    text = "Active Group Session: $durationText • Audio: $audioRoute",
                    style = MaterialTheme.typography.bodyMedium.copy(color = CosmicSecondary)
                )
            }

            // Feeds simulation block
            if (isScreenSharing) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1.3f)) {
                        ScreenShareFeed()
                    }
                    
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.7f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(allFeeds) { participant ->
                            ParticipantCard(
                                participant = participant,
                                onToggleMute = { viewModel.toggleParticipantMute(participant.id) },
                                onToggleVideo = { viewModel.toggleParticipantVideo(participant.id) },
                                modifier = Modifier.width(140.dp)
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val chunkedFeeds = allFeeds.chunked(2)
                    items(chunkedFeeds) { rowParticipants ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowParticipants.forEach { participant ->
                                ParticipantCard(
                                    participant = participant,
                                    onToggleMute = { viewModel.toggleParticipantMute(participant.id) },
                                    onToggleVideo = { viewModel.toggleParticipantVideo(participant.id) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowParticipants.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Control Actions Panel
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.toggleMute() },
                        modifier = Modifier
                            .size(52.dp)
                            .background(if (isMuted) Color.Red else CosmicSurfaceVariant, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Mute Microphone",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = { viewModel.toggleVideo() },
                        modifier = Modifier
                            .size(52.dp)
                            .background(if (isVideoOff) Color.Red else CosmicSurfaceVariant, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isVideoOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                            contentDescription = "Toggle Video Feed",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = { viewModel.toggleScreenShare() },
                        modifier = Modifier
                            .size(52.dp)
                            .background(if (isScreenSharing) CosmicPrimary else CosmicSurfaceVariant, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Toggle Screen Share",
                            tint = if (isScreenSharing) Color.Black else Color.White
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showAudioMenu = true },
                            modifier = Modifier
                                .size(52.dp)
                                .background(CosmicSurfaceVariant, CircleShape)
                        ) {
                            Icon(
                                imageVector = when (audioRoute) {
                                    "Speaker" -> Icons.Default.VolumeUp
                                    "Bluetooth" -> Icons.Default.SettingsInputComponent
                                    else -> Icons.Default.Phone
                                },
                                contentDescription = "Audio Routing",
                                tint = Color.White
                            )
                        }

                        DropdownMenu(
                            expanded = showAudioMenu,
                            onDismissRequest = { showAudioMenu = false },
                            containerColor = CosmicSurface
                        ) {
                            DropdownMenuItem(
                                text = { Text("Speakerphone", color = Color.White) },
                                leadingIcon = { Icon(Icons.Default.VolumeUp, contentDescription = null, tint = CosmicPrimary) },
                                onClick = {
                                    viewModel.setAudioRoute("Speaker")
                                    showAudioMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Earpiece Receiver", color = Color.White) },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = CosmicPrimary) },
                                onClick = {
                                    viewModel.setAudioRoute("Receiver")
                                    showAudioMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Bluetooth Headset", color = Color.White) },
                                leadingIcon = { Icon(Icons.Default.SettingsInputComponent, contentDescription = null, tint = CosmicPrimary) },
                                onClick = {
                                    viewModel.setAudioRoute("Bluetooth")
                                    showAudioMenu = false
                                }
                            )
                        }
                    }

                    IconButton(
                        onClick = { showInviteDialog = true },
                        modifier = Modifier
                            .size(52.dp)
                            .background(CosmicSurfaceVariant, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Invite Participant",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = onEndCall,
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color.Red, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "Hang Up Secure Session",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Secure Relay Bridge • Signal Token: AES_GCM_256_HASH_VALID",
                style = MaterialTheme.typography.labelSmall.copy(color = CosmicTextSecondary)
            )
        }
    }

    if (showInviteDialog) {
        AlertDialog(
            onDismissRequest = { showInviteDialog = false },
            containerColor = CosmicSurface,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Group, contentDescription = null, tint = CosmicPrimary)
                    Text("Securely Invite Node", color = Color.White, style = MaterialTheme.typography.titleMedium)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Add a verified team member to this end-to-end encrypted voice & video conference bridge.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CosmicTextSecondary
                    )

                    OutlinedTextField(
                        value = inviteName,
                        onValueChange = { inviteName = it },
                        label = { Text("Full Name", color = CosmicTextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicPrimary,
                            unfocusedBorderColor = CosmicSurfaceVariant,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = inviteRole,
                        onValueChange = { inviteRole = it },
                        label = { Text("Enterprise Role / Position", color = CosmicTextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicPrimary,
                            unfocusedBorderColor = CosmicSurfaceVariant,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Suggested Colleagues", style = MaterialTheme.typography.labelSmall, color = CosmicPrimary)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val recommendations = listOf(
                            "Jane Doe" to "Compliance Auditor",
                            "Bob Smith" to "DevOps Lead",
                            "Alice Johnson" to "Venture Partner",
                            "SyncOffice Bot" to "E2E Secure Recorder"
                        )
                        recommendations.forEach { (name, role) ->
                            Surface(
                                modifier = Modifier.clickable {
                                    inviteName = name
                                    inviteRole = role
                                },
                                color = CosmicSurfaceVariant,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inviteName.isNotBlank() && inviteRole.isNotBlank()) {
                            viewModel.inviteParticipant(inviteName, inviteRole)
                            inviteName = ""
                            inviteRole = ""
                            showInviteDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicPrimary)
                ) {
                    Text("Send E2E Token", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showInviteDialog = false }) {
                    Text("Cancel", color = CosmicTextSecondary)
                }
            }
        )
    }
}

// --- TAB 3: SMART SCHEDULING (CALENDAR) ---
@Composable
fun CalendarScreen(viewModel: SyncOfficeViewModel) {
    val events by viewModel.events.collectAsStateWithLifecycle(initialValue = emptyList())
    val aiScheduleState by viewModel.aiScheduleState.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTimeZone by remember { mutableStateOf("Local") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Smart AI Calendar",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = "Timezone-aware scheduling & AI optimization",
                    style = MaterialTheme.typography.bodySmall.copy(color = CosmicTextSecondary)
                )
            }

            IconButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.background(CosmicPrimary, RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Meeting", tint = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Timezone selector bar
        Card(
            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Simulate Timezone:", style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Local", "EST", "GMT", "JST").forEach { tz ->
                        Button(
                            onClick = { selectedTimeZone = tz },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedTimeZone == tz) CosmicPrimary else CosmicSurfaceVariant,
                                contentColor = if (selectedTimeZone == tz) Color.Black else Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(tz, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // AI Scheduler Assistant Trigger
        Card(
            colors = CardDefaults.cardColors(containerColor = CosmicSurfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "AI Assistant", tint = CosmicPrimary)
                    Text("AI Schedule Optimizer", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Suggest optimal cross-timezone meeting windows avoiding conflicts and project deadline overlaps.",
                    style = MaterialTheme.typography.bodySmall.copy(color = CosmicTextSecondary)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.optimizeSchedule() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_optimize_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicPrimary, contentColor = Color.Black)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "Optimize", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Optimize Calendar slots with Gemini")
                }

                if (aiScheduleState != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CosmicSurface, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        if (isAiLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.Center),
                                color = CosmicPrimary
                            )
                        } else {
                            Text(
                                text = aiScheduleState!!,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 16.sp
                                ),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Schedule Event List
        Text(
            text = "Scheduled Items:",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(events) { event ->
                // Adjust timezone display based on simulator
                val hourShift = when (selectedTimeZone) {
                    "EST" -> -5
                    "GMT" -> 0
                    "JST" -> 9
                    else -> 2 // Local simulation
                }

                val calendar = Calendar.getInstance()
                calendar.timeInMillis = event.startTime
                calendar.add(Calendar.HOUR_OF_DAY, hourShift)
                val formatter = SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault())
                val displayedTime = formatter.format(calendar.time)

                Card(
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = event.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Adjusted to $selectedTimeZone: $displayedTime",
                                style = MaterialTheme.typography.bodySmall.copy(color = CosmicSecondary)
                            )
                            Text(
                                text = "Attendees: ${event.attendees}",
                                style = MaterialTheme.typography.bodySmall.copy(color = CosmicTextSecondary)
                            )
                        }

                        IconButton(onClick = { viewModel.deleteEvent(event.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Event", tint = Color.Red.copy(alpha = 0.8f))
                        }
                    }
                }
            }
        }
    }

    // Add Meeting Dialog Simulator
    if (showAddDialog) {
        var meetTitle by remember { mutableStateOf("") }
        var meetAttendees by remember { mutableStateOf("") }
        var meetCategory by remember { mutableStateOf("Meeting") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Schedule New Secure Session") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = meetTitle,
                        onValueChange = { meetTitle = it },
                        label = { Text("Meeting Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = meetAttendees,
                        onValueChange = { meetAttendees = it },
                        label = { Text("Attendees (comma-separated)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = meetCategory,
                        onValueChange = { meetCategory = it },
                        label = { Text("Category (Meeting, Audit, Personal)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (meetTitle.isNotBlank()) {
                            viewModel.addEvent(
                                title = meetTitle,
                                startOffsetHours = 24, // Scheduled for tomorrow
                                durationHours = 1,
                                timeZone = selectedTimeZone,
                                attendees = meetAttendees,
                                category = meetCategory
                            )
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicPrimary, contentColor = Color.Black)
                ) {
                    Text("Schedule")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// --- TAB 4: DOCUMENT COLLABORATION ---
@Composable
fun DocumentScreen(viewModel: SyncOfficeViewModel) {
    val documents by viewModel.documents.collectAsStateWithLifecycle(initialValue = emptyList())
    val selectedDocId by viewModel.selectedDocumentId.collectAsStateWithLifecycle()
    val activeDoc = documents.find { it.id == selectedDocId }

    val aiSummary by viewModel.aiSummaryState.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()

    if (activeDoc == null) {
        // Document List View
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Document Collaboration",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "Real-time look editing, version tracking & AI digest",
                        style = MaterialTheme.typography.bodySmall.copy(color = CosmicTextSecondary)
                    )
                }

                Button(
                    onClick = { viewModel.addDocument("Untitled Proposal", "Write enterprise briefs here...") },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicPrimary, contentColor = Color.Black)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Doc")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Doc")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(documents) { doc ->
                    Card(
                        onClick = { viewModel.selectDocument(doc.id) },
                        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("doc_item_${doc.id}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Description, contentDescription = "Doc", tint = CosmicSecondary)
                                    Text(
                                        text = doc.title,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                Surface(
                                    color = CosmicSecondary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "v${doc.version}",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(color = CosmicSecondary, fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = doc.content,
                                style = MaterialTheme.typography.bodyMedium.copy(color = CosmicTextSecondary),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Modified by ${doc.modifiedBy}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = CosmicTextSecondary)
                                )
                                Text(
                                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(doc.lastModified)),
                                    style = MaterialTheme.typography.bodySmall.copy(color = CosmicTextSecondary)
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Document Editor view
        var editorTitle by remember(activeDoc) { mutableStateOf(activeDoc.title) }
        var editorContent by remember(activeDoc) { mutableStateOf(activeDoc.content) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.selectDocument(null) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.summarizeDocument(activeDoc) },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicTertiary, contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Summarize", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("AI Summarize")
                    }

                    Button(
                        onClick = {
                            viewModel.updateDocument(activeDoc.id, editorTitle, editorContent, activeDoc.version)
                            viewModel.selectDocument(null)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicPrimary, contentColor = Color.Black)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save & Sync")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI summary box if available
            val shownSummary = aiSummary ?: activeDoc.aiSummary
            if (shownSummary.isNotEmpty() || isAiLoading) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CosmicSurfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = CosmicPrimary, modifier = Modifier.size(16.dp))
                            Text("Gemini AI Executive Summary", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        if (isAiLoading && aiSummary == "Generating smart summarization...") {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = CosmicPrimary)
                        } else {
                            Text(text = shownSummary, style = MaterialTheme.typography.bodySmall.copy(color = Color.White))
                        }
                    }
                }
            }

            // Editor Fields
            OutlinedTextField(
                value = editorTitle,
                onValueChange = { editorTitle = it },
                textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("doc_title_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CosmicPrimary,
                    unfocusedBorderColor = CosmicSurfaceVariant
                )
            )

            OutlinedTextField(
                value = editorContent,
                onValueChange = { editorContent = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("doc_content_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CosmicPrimary,
                    unfocusedBorderColor = CosmicSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Lock status: Fully Encrypted Node", fontSize = 11.sp, color = SecurityCompliant)
                Text("Version ${activeDoc.version} • Syncing state: Saved Local", fontSize = 11.sp, color = CosmicTextSecondary)
            }
        }
    }
}

// --- TAB 5: WORKFLOWS & AUTOMATION ---
@Composable
fun WorkflowScreen(viewModel: SyncOfficeViewModel) {
    val rules by viewModel.rules.collectAsStateWithLifecycle(initialValue = emptyList())
    val logs by viewModel.logs.collectAsStateWithLifecycle(initialValue = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Workflow Automation",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        Text(
            text = "Automate approvals, notifications, and logging based on project events.",
            style = MaterialTheme.typography.bodySmall.copy(color = CosmicTextSecondary)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Trigger rules list
        Text(
            text = "Active Trigger Rules:",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(0.4f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(rules) { rule ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.SettingsInputComponent, contentDescription = "Rule", tint = AutomationGlow, modifier = Modifier.size(16.dp))
                                Text(rule.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "When: ${rule.triggerType} -> Then: ${rule.actionType}",
                                style = MaterialTheme.typography.bodySmall.copy(color = CosmicTextSecondary)
                            )
                        }

                        Switch(
                            checked = rule.isActive,
                            onCheckedChange = { viewModel.toggleRule(rule) },
                            colors = SwitchDefaults.colors(checkedThumbColor = CosmicPrimary)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Execution logs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Live Execution Logs:",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            TextButton(onClick = { viewModel.clearAllLogs() }) {
                Text("Clear logs", color = Color.Red.copy(alpha = 0.8f))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxWidth()
                .background(CosmicSurface, RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (logs.isEmpty()) {
                item {
                    Text(
                        "No execution logs found. Modify a doc or complete a task to trigger automations!",
                        style = MaterialTheme.typography.bodySmall.copy(color = CosmicTextSecondary)
                    )
                }
            } else {
                items(logs) { log ->
                    val timeString = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(
                                    imageVector = if (log.status == "Success") Icons.Default.CheckCircle else Icons.Default.Bolt,
                                    contentDescription = log.status,
                                    tint = if (log.status == "Success") SecurityCompliant else AutomationGlow,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = log.ruleName,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                                )
                            }
                            Text(
                                text = timeString,
                                style = MaterialTheme.typography.labelSmall.copy(color = CosmicTextSecondary)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = log.details,
                            style = MaterialTheme.typography.bodySmall.copy(color = CosmicTextSecondary)
                        )
                        Divider(
                            modifier = Modifier.padding(vertical = 6.dp),
                            color = CosmicSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// --- SECURE USER ONBOARDING FLOW WIZARD ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingFlowScreen(viewModel: SyncOfficeViewModel) {
    val step by viewModel.onboardingStep.collectAsStateWithLifecycle()
    val email by viewModel.signupEmail.collectAsStateWithLifecycle()
    val name by viewModel.tempName.collectAsStateWithLifecycle()
    val role by viewModel.tempRole.collectAsStateWithLifecycle()
    val avatar by viewModel.tempAvatar.collectAsStateWithLifecycle()
    
    val isEmailConn by viewModel.isEmailConnected.collectAsStateWithLifecycle()
    val isCalConn by viewModel.isCalendarConnected.collectAsStateWithLifecycle()
    val isCrmConn by viewModel.isCrmConnected.collectAsStateWithLifecycle()
    
    val notifEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()

    val authError by viewModel.authError.collectAsStateWithLifecycle()
    val isAuthLoading by viewModel.isAuthLoading.collectAsStateWithLifecycle()
    val isFirebaseMode by viewModel.isFirebaseMode.collectAsStateWithLifecycle()

    var isSignUpMode by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .background(CosmicSurface, RoundedCornerShape(24.dp))
                .border(1.dp, CosmicSurfaceVariant, RoundedCornerShape(24.dp))
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (step == 1 && email.isBlank()) {
                // SECURE FIREBASE / LOCAL AUTHENTICATION SCREEN
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = CosmicPrimary,
                    modifier = Modifier.size(56.dp)
                )
                
                Text(
                    text = if (isSignUpMode) "Create SyncOffice Account" else "Welcome to SyncOffice",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = if (isSignUpMode) 
                        "Register a secure E2E encrypted account to collaborate across your team."
                    else 
                        "Log in to your secure enterprise dashboard and connect with your workspaces.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = CosmicTextSecondary),
                    textAlign = TextAlign.Center
                )

                // Auth Mode indicator badge
                Surface(
                    color = if (isFirebaseMode) SecurityCompliant.copy(alpha = 0.15f) else CosmicSecondary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isFirebaseMode) SecurityCompliant else CosmicSecondary)
                        )
                        Text(
                            text = if (isFirebaseMode) "FIREBASE AUTH SECURED" else "LOCAL SECURE FALLBACK",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isFirebaseMode) SecurityCompliant else CosmicSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = {
                        emailInput = it
                        viewModel.clearAuthError()
                    },
                    label = { Text("Work Email Address") },
                    placeholder = { Text("e.g. sarah.connor@acme.com") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_email_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CosmicPrimary,
                        unfocusedBorderColor = CosmicSurfaceVariant,
                        focusedLabelColor = CosmicPrimary,
                        unfocusedLabelColor = CosmicTextSecondary,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        viewModel.clearAuthError()
                    },
                    label = { Text("Password") },
                    placeholder = { Text("At least 6 characters") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_password_input"),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        val description = if (passwordVisible) "Hide password" else "Show password"
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = description, tint = CosmicTextSecondary)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CosmicPrimary,
                        unfocusedBorderColor = CosmicSurfaceVariant,
                        focusedLabelColor = CosmicPrimary,
                        unfocusedLabelColor = CosmicTextSecondary,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                if (authError != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Red.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .border(1.dp, Color.Red.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = Color.Red.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = authError!!,
                            color = Color.Red.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        if (isSignUpMode) {
                            viewModel.signUpWithEmailAndPassword(emailInput, passwordInput) {
                                // Successfully registered!
                            }
                        } else {
                            viewModel.signInWithEmailAndPassword(emailInput, passwordInput) {
                                // Successfully logged in!
                            }
                        }
                    },
                    enabled = !isAuthLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("auth_submit_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CosmicPrimary,
                        contentColor = Color(0xFF1D192B)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isAuthLoading) {
                        CircularProgressIndicator(
                            color = Color(0xFF1D192B),
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (isSignUpMode) "Register & Start Setup" else "Secure Log In",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                TextButton(
                    onClick = {
                        isSignUpMode = !isSignUpMode
                        viewModel.clearAuthError()
                    },
                    modifier = Modifier.testTag("auth_toggle_button")
                ) {
                    Text(
                        text = if (isSignUpMode) "Already have an account? Log In" else "New to SyncOffice? Sign Up",
                        color = CosmicPrimary,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            } else {
                // STEP WIZARD INDICATOR
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(4) { idx ->
                        val active = idx + 1 <= step - 1
                        val isCurrent = idx + 1 == step - 1
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (isCurrent) CosmicPrimary 
                                    else if (active) SecurityCompliant 
                                    else CosmicSurfaceVariant
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                when (step) {
                    2 -> {
                        // STEP 1: CAPABILITIES INTRODUCTION
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = CosmicSecondary,
                            modifier = Modifier.size(48.dp)
                        )

                        Text(
                            text = "Core Workspace Capabilities",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )

                        Text(
                            text = "Unified workspace with integrated intelligence and end-to-end security compliance.",
                            style = MaterialTheme.typography.bodySmall.copy(color = CosmicTextSecondary),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Features List
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CapabilityRow(
                                icon = Icons.Default.Lock,
                                title = "E2E Encrypted Messaging",
                                desc = "GDPR & HIPAA compliant communication with video bridges."
                            )
                            CapabilityRow(
                                icon = Icons.Default.CalendarMonth,
                                title = "Smart Scheduling",
                                desc = "Cross-timezone calendar optimizer driven by workflow AI."
                            )
                            CapabilityRow(
                                icon = Icons.Default.Description,
                                title = "Document Collaboration",
                                desc = "Instant real-time edits with automatic AI-powered summaries."
                            )
                            CapabilityRow(
                                icon = Icons.Default.Autorenew,
                                title = "Task Automation",
                                desc = "Trigger background reporting and reminders effortlessly."
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.setOnboardingStep(3) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("step_capabilities_next"),
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicPrimary, contentColor = Color(0xFF1D192B)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Setup Security Profile", fontWeight = FontWeight.Bold)
                        }
                    }
                    3 -> {
                        // STEP 2: PROFILE SETUP
                        Text(
                            text = "Configure Security Profile",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )

                        Text(
                            text = "Initialize your user signature for the decentralized compliance log.",
                            style = MaterialTheme.typography.bodySmall.copy(color = CosmicTextSecondary),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Executive Live Monogram Preview
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(CosmicPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = avatar.ifBlank { "JD" },
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = Color(0xFF1D192B),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        var profileName by remember { mutableStateOf(name) }
                        var profileRole by remember { mutableStateOf(role) }
                        var profileAvatar by remember { mutableStateOf(avatar) }

                        OutlinedTextField(
                            value = profileName,
                            onValueChange = {
                                profileName = it
                                val init = if (it.trim().contains(" ")) {
                                    val parts = it.trim().split("\\s+".toRegex())
                                    (parts[0].take(1) + parts[1].take(1)).uppercase()
                                } else {
                                    it.trim().take(2).uppercase()
                                }
                                profileAvatar = init.ifBlank { "JD" }
                                viewModel.updateTempProfile(profileName, profileRole, profileAvatar)
                            },
                            label = { Text("Full Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_name_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CosmicPrimary,
                                unfocusedBorderColor = CosmicSurfaceVariant,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        OutlinedTextField(
                            value = profileRole,
                            onValueChange = {
                                profileRole = it
                                viewModel.updateTempProfile(profileName, profileRole, profileAvatar)
                            },
                            label = { Text("Corporate Role / Title") },
                            placeholder = { Text("e.g. Lead Analyst, Compliance Officer") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_role_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CosmicPrimary,
                                unfocusedBorderColor = CosmicSurfaceVariant,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (profileName.isNotBlank() && profileRole.isNotBlank()) {
                                    viewModel.setOnboardingStep(4)
                                } else {
                                    if (profileName.isBlank()) profileName = "Corporate User"
                                    if (profileRole.isBlank()) profileRole = "Associate"
                                    viewModel.updateTempProfile(profileName, profileRole, profileAvatar)
                                    viewModel.setOnboardingStep(4)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("step_profile_next"),
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicPrimary, contentColor = Color(0xFF1D192B)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Connect Enterprise Accounts", fontWeight = FontWeight.Bold)
                        }
                    }
                    4 -> {
                        // STEP 3: EXTERNAL ACCOUNTS CONNECTION
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = CosmicSecondary,
                            modifier = Modifier.size(48.dp)
                        )

                        Text(
                            text = "Connect External Accounts",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )

                        Text(
                            text = "Establish secure communication bridges with CRM, calendar, and email tools.",
                            style = MaterialTheme.typography.bodySmall.copy(color = CosmicTextSecondary),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ConnectionBridgeRow(
                                title = "GSuite & Office 365 Email",
                                isConnected = isEmailConn,
                                onConnectToggle = { viewModel.toggleEmailConnection() }
                            )
                            ConnectionBridgeRow(
                                title = "Exchange Calendar Sync",
                                isConnected = isCalConn,
                                onConnectToggle = { viewModel.toggleCalendarConnection() }
                            )
                            ConnectionBridgeRow(
                                title = "Salesforce & HubSpot CRM",
                                isConnected = isCrmConn,
                                onConnectToggle = { viewModel.toggleCrmConnection() }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.setOnboardingStep(5) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("step_accounts_next"),
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicPrimary, contentColor = Color(0xFF1D192B)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Configure Preferences", fontWeight = FontWeight.Bold)
                        }
                    }
                    5 -> {
                        // STEP 4: INITIAL PREFERENCES & FINAL HANDSHAKE
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SecurityCompliant,
                            modifier = Modifier.size(52.dp)
                        )

                        Text(
                            text = "Initialize Preferences",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )

                        Text(
                            text = "Tailor your workspace canvas and instant notification relays.",
                            style = MaterialTheme.typography.bodySmall.copy(color = CosmicTextSecondary),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CosmicSurfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("E2E Alert Notifications", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                                    Text("Instant security digests & chat alerts", style = MaterialTheme.typography.bodySmall, color = CosmicTextSecondary)
                                }
                                Switch(
                                    checked = notifEnabled,
                                    onCheckedChange = { viewModel.toggleNotifications() },
                                    colors = SwitchDefaults.colors(checkedThumbColor = CosmicPrimary)
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CosmicSurfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Clean Eye-Safe Canvas", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                                    Text("Enforce high contrast dark theme", style = MaterialTheme.typography.bodySmall, color = CosmicTextSecondary)
                                }
                                Switch(
                                    checked = isDark,
                                    onCheckedChange = { viewModel.toggleDarkMode() },
                                    colors = SwitchDefaults.colors(checkedThumbColor = CosmicPrimary)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.completeOnboarding() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("step_complete_handshake"),
                            colors = ButtonDefaults.buttonColors(containerColor = SecurityCompliant, contentColor = Color(0xFF1D192B)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Complete Security Handshake", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CapabilityRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CosmicSurfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = CosmicPrimary, modifier = Modifier.size(24.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
            Text(desc, style = MaterialTheme.typography.bodySmall, color = CosmicTextSecondary)
        }
    }
}

@Composable
fun ConnectionBridgeRow(title: String, isConnected: Boolean, onConnectToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CosmicSurfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = Color.White)
            Text(
                text = if (isConnected) "CONNECTED • SECURED" else "NOT SYNCHRONIZED",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (isConnected) SecurityCompliant else CosmicTextSecondary
            )
        }
        Button(
            onClick = onConnectToggle,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isConnected) CosmicSurfaceVariant else CosmicSecondary,
                contentColor = if (isConnected) Color.White else Color(0xFF1D192B)
            ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(if (isConnected) "Disconnect" else "Synchronize", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// --- WORK GROUPS DASHBOARD SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkGroupScreen(viewModel: SyncOfficeViewModel) {
    val groups by viewModel.allGroups.collectAsStateWithLifecycle(initialValue = emptyList())
    var selectedGroupId by remember { mutableStateOf<Int?>(null) }
    
    var showCreateDialog by remember { mutableStateOf(false) }

    val activeGroup = groups.find { it.id == selectedGroupId } ?: groups.firstOrNull()
    if (activeGroup != null && selectedGroupId == null) {
        selectedGroupId = activeGroup.id
    }

    val membersFlow = remember(selectedGroupId) {
        selectedGroupId?.let { viewModel.getMembersForGroup(it) } ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }
    val members by membersFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Column: List of Workgroups
        Column(
            modifier = Modifier
                .weight(1.0f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Work Groups",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                
                IconButton(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier
                        .background(CosmicPrimary, CircleShape)
                        .size(36.dp)
                        .testTag("create_group_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Group", tint = Color(0xFF1D192B), modifier = Modifier.size(20.dp))
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (groups.isEmpty()) {
                    item {
                        Text("No active work groups. Click + to create one.", color = CosmicTextSecondary)
                    }
                } else {
                    items(groups) { group ->
                        val isSelected = group.id == selectedGroupId
                        Card(
                            onClick = { selectedGroupId = group.id },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) CosmicSurfaceVariant else CosmicSurface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("group_card_${group.id}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = group.name,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) CosmicPrimary else Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = group.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CosmicTextSecondary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Right Column: Selected Group Details & Coworker Adding Form
        Column(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight()
                .background(CosmicSurface, RoundedCornerShape(16.dp))
                .border(1.dp, CosmicSurfaceVariant, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (activeGroup != null) {
                Text(
                    text = activeGroup.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = activeGroup.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = CosmicTextSecondary
                )

                Divider(color = CosmicSurfaceVariant)

                // ADD MEMBER SEARCH FORM
                Text(
                    text = "ADD COWORKER BY EMAIL",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = CosmicPrimary
                )

                var searchEmailInput by remember { mutableStateOf("") }
                var registryResultMsg by remember { mutableStateOf<String?>(null) }
                var registryIsFound by remember { mutableStateOf<Boolean?>(null) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchEmailInput,
                        onValueChange = {
                            searchEmailInput = it
                            registryResultMsg = null
                            registryIsFound = null
                        },
                        placeholder = { Text("coworker@acme.com") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("group_email_search_input"),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicPrimary,
                            unfocusedBorderColor = CosmicSurfaceVariant,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Button(
                        onClick = {
                            if (searchEmailInput.contains("@")) {
                                viewModel.searchUserByEmail(searchEmailInput.trim()) { registered ->
                                    if (registered != null) {
                                        registryIsFound = true
                                        registryResultMsg = "Registry Hit: ${registered.name} (${registered.role})"
                                    } else {
                                        registryIsFound = false
                                        registryResultMsg = "Registry Miss: User not found."
                                    }
                                }
                            } else {
                                registryResultMsg = "Enter valid email"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color(0xFF1D192B)),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        modifier = Modifier.testTag("search_registry_button")
                    ) {
                        Text("Verify", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (registryResultMsg != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CosmicSurfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = registryResultMsg!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (registryIsFound == true) SecurityCompliant else CosmicSecondary
                        )

                        if (registryIsFound == true) {
                            Button(
                                onClick = {
                                    viewModel.addMemberToGroup(activeGroup.id, searchEmailInput.trim()) { success, info ->
                                        if (success) {
                                            searchEmailInput = ""
                                            registryResultMsg = "Added directly successfully!"
                                            registryIsFound = null
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp)
                                    .testTag("add_to_group_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = SecurityCompliant, contentColor = Color(0xFF1D192B)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Add Direct to Group", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else if (registryIsFound == false) {
                            val generatedLink = "https://syncoffice.app/invite?g=${activeGroup.id}&e=${searchEmailInput.trim()}"
                            
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Send this invite link to your coworker:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CosmicTextSecondary
                                )
                                SelectionContainer {
                                    Text(
                                        text = generatedLink,
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                        color = CosmicPrimary,
                                        modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(4.dp)).padding(6.dp)
                                    )
                                }
                                Button(
                                    onClick = {
                                        viewModel.addMemberToGroup(activeGroup.id, searchEmailInput.trim()) { _, _ ->
                                            searchEmailInput = ""
                                            registryResultMsg = "Invitation tracked! Coworker pending."
                                            registryIsFound = null
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(34.dp)
                                        .testTag("copy_invite_link_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = CosmicPrimary, contentColor = Color(0xFF1D192B)),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Copy & Log Invitation", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Divider(color = CosmicSurfaceVariant)

                // MEMBERS LIST
                Text(
                    text = "MEMBERS (${members.size})",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = CosmicTextSecondary
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (members.isEmpty()) {
                        item {
                            Text("No members in group yet.", style = MaterialTheme.typography.bodySmall, color = CosmicTextSecondary)
                        }
                    } else {
                        items(members) { member ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CosmicSurfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(if (member.isInvited) CosmicSurfaceVariant else CosmicSecondary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = member.avatarInitials,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (member.isInvited) CosmicTextSecondary else Color(0xFF1D192B)
                                        )
                                    }
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(member.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                            if (member.isInvited) {
                                                Surface(
                                                    color = CosmicSecondary.copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        "INVITED",
                                                        fontSize = 8.sp,
                                                        color = CosmicSecondary,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(member.role, style = MaterialTheme.typography.bodySmall, color = CosmicTextSecondary)
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.removeMemberFromGroup(activeGroup.id, member.email) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Select or create a work group.", color = CosmicTextSecondary)
                }
            }
        }
    }

    // CREATE GROUP DIALOG
    if (showCreateDialog) {
        var groupNameInput by remember { mutableStateOf("") }
        var groupDescInput by remember { mutableStateOf("") }
        
        // List of initial members added in the dialog (Pair: email to isInvited)
        var addedMembersList by remember { mutableStateOf(listOf<Pair<String, Boolean>>()) }
        
        // Search states inside dialog
        var searchEmailInput by remember { mutableStateOf("") }
        var registryResultMsg by remember { mutableStateOf<String?>(null) }
        var registryIsFound by remember { mutableStateOf<Boolean?>(null) }
        var searchedUser by remember { mutableStateOf<RegisteredUserEntity?>(null) }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = CosmicSurface,
            title = { Text("Create Work Group", color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = groupNameInput,
                        onValueChange = { groupNameInput = it },
                        label = { Text("Group Name") },
                        modifier = Modifier.fillMaxWidth().testTag("create_group_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicPrimary,
                            unfocusedBorderColor = CosmicSurfaceVariant,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = groupDescInput,
                        onValueChange = { groupDescInput = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth().testTag("create_group_desc_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CosmicPrimary,
                            unfocusedBorderColor = CosmicSurfaceVariant,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Divider(color = CosmicSurfaceVariant)

                    // Added members list display
                    Text(
                        text = "ADDED INITIAL MEMBERS (${addedMembersList.size})",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = CosmicTextSecondary)
                    )

                    if (addedMembersList.isEmpty()) {
                        Text(
                            text = "No coworkers added yet. Search below to add registered members or invite guests.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CosmicTextSecondary
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            addedMembersList.forEach { (email, isInvited) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(CosmicSurfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(if (isInvited) CosmicSurfaceVariant else CosmicPrimary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = email.take(2).uppercase(),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (isInvited) CosmicTextSecondary else Color(0xFF1D192B)
                                            )
                                        }
                                        Text(
                                            text = email,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = if (isInvited) SecurityWarning.copy(alpha = 0.15f) else SecurityCompliant.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = if (isInvited) "GUEST" else "MEMBER",
                                                fontSize = 8.sp,
                                                color = if (isInvited) SecurityWarning else SecurityCompliant,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                addedMembersList = addedMembersList.filter { it.first != email }
                                            },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remove",
                                                tint = Color.Red.copy(alpha = 0.6f),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Divider(color = CosmicSurfaceVariant)

                    // Coworker Finder Component
                    Text(
                        text = "COWORKER FINDER",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = CosmicPrimary)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchEmailInput,
                            onValueChange = { searchEmailInput = it },
                            label = { Text("Coworker Email") },
                            placeholder = { Text("coworker@acme.com") },
                            modifier = Modifier.weight(1f).testTag("create_group_emails_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CosmicPrimary,
                                unfocusedBorderColor = CosmicSurfaceVariant,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                val trimmed = searchEmailInput.trim()
                                if (trimmed.isNotBlank() && trimmed.contains("@")) {
                                    viewModel.searchUserByEmail(trimmed) { user ->
                                        if (user != null) {
                                            searchedUser = user
                                            registryIsFound = true
                                            registryResultMsg = "Registry Hit: ${user.name} (${user.role})"
                                        } else {
                                            searchedUser = null
                                            registryIsFound = false
                                            registryResultMsg = "Registry Miss: User not found."
                                        }
                                    }
                                } else {
                                    registryResultMsg = "Please enter a valid email address."
                                    registryIsFound = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicSecondary, contentColor = Color(0xFF1D192B)),
                            modifier = Modifier.height(56.dp).testTag("create_group_search_button")
                        ) {
                            Text("Find", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Display Search Result Message & Actions
                    if (registryResultMsg != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CosmicSurfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = registryResultMsg!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (registryIsFound == true) SecurityCompliant else CosmicSecondary
                            )

                            if (registryIsFound == true && searchedUser != null) {
                                Button(
                                    onClick = {
                                        val email = searchedUser!!.email
                                        if (!addedMembersList.any { it.first == email }) {
                                            addedMembersList = addedMembersList + Pair(email, false)
                                        }
                                        searchEmailInput = ""
                                        registryResultMsg = "Added to group creation list!"
                                        registryIsFound = null
                                        searchedUser = null
                                    },
                                    modifier = Modifier.fillMaxWidth().height(36.dp).testTag("add_searched_member_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = SecurityCompliant, contentColor = Color(0xFF1D192B)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Add directly to Group list", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else if (registryIsFound == false) {
                                val generatedLink = "https://syncoffice.app/invite?g=temp&e=${searchEmailInput.trim()}"
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "Generate and copy a unique invite link:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CosmicTextSecondary
                                    )
                                    SelectionContainer {
                                        Text(
                                            text = generatedLink,
                                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                            color = CosmicPrimary,
                                            modifier = Modifier
                                                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                                .padding(6.dp)
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            val email = searchEmailInput.trim()
                                            if (email.isNotBlank() && !addedMembersList.any { it.first == email }) {
                                                addedMembersList = addedMembersList + Pair(email, true)
                                            }
                                            searchEmailInput = ""
                                            registryResultMsg = "Invitation tracked as pending guest!"
                                            registryIsFound = null
                                        },
                                        modifier = Modifier.fillMaxWidth().height(36.dp).testTag("add_invited_guest_button"),
                                        colors = ButtonDefaults.buttonColors(containerColor = CosmicPrimary, contentColor = Color(0xFF1D192B)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Log Invitation & Add Guest", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (groupNameInput.isNotBlank()) {
                            val emails = addedMembersList.map { it.first }
                            viewModel.createGroup(groupNameInput, groupDescInput, emails)
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicPrimary, contentColor = Color(0xFF1D192B)),
                    modifier = Modifier.testTag("create_group_confirm_button")
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = CosmicTextSecondary)
                }
            }
        )
    }
}

// --- TAB 5: PRODUCTIVITY DASHBOARD ---

data class DayProductivity(
    val dayLabel: String,
    val edits: Int,
    val meetings: Int,
    val attendanceRate: Int
)

@Composable
fun ProductivitySubScreen(viewModel: SyncOfficeViewModel) {
    val documents by viewModel.documents.collectAsStateWithLifecycle(initialValue = emptyList())
    val events by viewModel.events.collectAsStateWithLifecycle(initialValue = emptyList())
    val productivityRecommendation by viewModel.productivityRecommendation.collectAsStateWithLifecycle()
    val isProductivityLoading by viewModel.isProductivityLoading.collectAsStateWithLifecycle()

    val totalDocVersions = remember(documents) { documents.sumOf { it.version } }
    val totalMeetings = remember(events) { events.size }

    val mon = DayProductivity("Mon", 4, 2, 85)
    val tue = DayProductivity("Tue", 7, 3, 90)
    val wed = DayProductivity("Wed", 12, 1, 100)
    val thu = DayProductivity("Thu", 9, 4, 88)
    val fri = DayProductivity("Fri", maxOf(5, totalDocVersions), maxOf(2, totalMeetings), 95)
    val sat = DayProductivity("Sat", 2, 0, 0)
    val sun = DayProductivity("Sun", 1, 0, 0)

    val trendData = listOf(mon, tue, wed, thu, fri, sat, sun)
    val maxVal = remember(trendData) {
        val maxEdits = trendData.maxOf { it.edits }
        val maxMeetings = trendData.maxOf { it.meetings }
        maxOf(maxEdits, maxMeetings, 15).toFloat()
    }

    var selectedDayIndex by remember { mutableStateOf(4) } // Default to Friday
    val selectedDay = trendData[selectedDayIndex]

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("productivity_dashboard_container"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // HEADER SUMMARY WITH TITLE & METRIC CHIPS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Workspace Productivity Dashboard",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = "E2E secure analytics tracking document collaboration and team coordinate rates.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CosmicTextSecondary
                )
            }

            Surface(
                color = SecurityCompliant.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = "Active Speed",
                        tint = SecurityCompliant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "PIPELINE ACTIVE (+14%)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SecurityCompliant,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        // TOP 3 KPI HIGHLIGHT CARDS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 1: Document Collaboration Edits
            Card(
                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Doc Edits", style = MaterialTheme.typography.labelSmall, color = CosmicTextSecondary)
                        Icon(Icons.Default.Article, contentDescription = null, tint = CosmicPrimary, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = "${trendData.sumOf { it.edits }} total",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "Last 7 days versions",
                        style = MaterialTheme.typography.labelSmall,
                        color = CosmicTextSecondary
                    )
                }
            }

            // Card 2: Meetings & Events
            Card(
                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Meetings", style = MaterialTheme.typography.labelSmall, color = CosmicTextSecondary)
                        Icon(Icons.Default.Groups, contentDescription = null, tint = CosmicSecondary, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = "${trendData.sumOf { it.meetings }} syncs",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "Average attendance 92%",
                        style = MaterialTheme.typography.labelSmall,
                        color = CosmicTextSecondary
                    )
                }
            }

            // Card 3: Combined Score
            Card(
                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sprint Health", style = MaterialTheme.typography.labelSmall, color = CosmicTextSecondary)
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = SecurityCompliant, modifier = Modifier.size(16.dp))
                    }
                    val sprintScore = minOf(99, 70 + (totalDocVersions * 2) + (totalMeetings * 3))
                    Text(
                        text = "$sprintScore / 100",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = SecurityCompliant
                    )
                    Text(
                        text = "Status: OPTIMAL NODE",
                        style = MaterialTheme.typography.labelSmall,
                        color = SecurityCompliant
                    )
                }
            }
        }

        // DOUBLE-LINE GRADIENT TREND CHART (CANVAS)
        Card(
            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Weekly Activity Distribution",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Tap on any day column to focus details",
                            style = MaterialTheme.typography.labelSmall,
                            color = CosmicTextSecondary
                        )
                    }

                    // Legend Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(CosmicPrimary))
                            Text("Document Edits", style = MaterialTheme.typography.labelSmall, color = CosmicTextSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SecurityCompliant))
                            Text("Meetings", style = MaterialTheme.typography.labelSmall, color = CosmicTextSecondary)
                        }
                    }
                }

                // Interactive Chart Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 20.dp)
                    ) {
                        val width = size.width
                        val height = size.height
                        val paddingLeft = 40f
                        val paddingRight = 40f
                        val paddingTop = 20f
                        val paddingBottom = 20f

                        val chartWidth = width - paddingLeft - paddingRight
                        val chartHeight = height - paddingTop - paddingBottom

                        // Draw Grid Lines (Horizontal background separators)
                        val gridCount = 3
                        for (i in 0..gridCount) {
                            val y = paddingTop + (chartHeight / gridCount) * i
                            drawLine(
                                color = CosmicSurfaceVariant.copy(alpha = 0.4f),
                                start = androidx.compose.ui.geometry.Offset(paddingLeft, y),
                                end = androidx.compose.ui.geometry.Offset(width - paddingRight, y),
                                strokeWidth = 1f
                            )
                        }

                        // Coordinates logic
                        val xCoords = FloatArray(7) { idx ->
                            paddingLeft + (chartWidth / 6f) * idx
                        }

                        // Path calculation for Edits (Lavender Curve)
                        val editsPath = androidx.compose.ui.graphics.Path()
                        val meetingsPath = androidx.compose.ui.graphics.Path()

                        trendData.forEachIndexed { idx, day ->
                            val x = xCoords[idx]
                            val yEdits = height - paddingBottom - (day.edits.toFloat() / maxVal) * chartHeight
                            val yMeetings = height - paddingBottom - (day.meetings.toFloat() / maxVal) * chartHeight

                            if (idx == 0) {
                                editsPath.moveTo(x, yEdits)
                                meetingsPath.moveTo(x, yMeetings)
                            } else {
                                editsPath.lineTo(x, yEdits)
                                meetingsPath.lineTo(x, yMeetings)
                            }
                        }

                        // Draw Edits Gradient Fill Area Under the Curve
                        val editsFillPath = androidx.compose.ui.graphics.Path().apply {
                            addPath(editsPath)
                            lineTo(xCoords[6], height - paddingBottom)
                            lineTo(xCoords[0], height - paddingBottom)
                            close()
                        }
                        drawPath(
                            path = editsFillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(CosmicPrimary.copy(alpha = 0.25f), Color.Transparent),
                                startY = paddingTop,
                                endY = height - paddingBottom
                            )
                        )

                        // Draw Meetings Gradient Fill Area
                        val meetingsFillPath = androidx.compose.ui.graphics.Path().apply {
                            addPath(meetingsPath)
                            lineTo(xCoords[6], height - paddingBottom)
                            lineTo(xCoords[0], height - paddingBottom)
                            close()
                        }
                        drawPath(
                            path = meetingsFillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(SecurityCompliant.copy(alpha = 0.20f), Color.Transparent),
                                startY = paddingTop,
                                endY = height - paddingBottom
                            )
                        )

                        // Draw Lines themselves with high-polish strokes
                        drawPath(
                            path = editsPath,
                            color = CosmicPrimary,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                        )
                        drawPath(
                            path = meetingsPath,
                            color = SecurityCompliant,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                        )

                        // Draw Node Circles & highlight selected node
                        trendData.forEachIndexed { idx, day ->
                            val x = xCoords[idx]
                            val yEdits = height - paddingBottom - (day.edits.toFloat() / maxVal) * chartHeight
                            val yMeetings = height - paddingBottom - (day.meetings.toFloat() / maxVal) * chartHeight

                            // Draw support column line for selected day
                            if (idx == selectedDayIndex) {
                                drawLine(
                                    color = CosmicPrimary.copy(alpha = 0.3f),
                                    start = androidx.compose.ui.geometry.Offset(x, paddingTop),
                                    end = androidx.compose.ui.geometry.Offset(x, height - paddingBottom),
                                    strokeWidth = 2f,
                                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                )
                            }

                            // Edits Node Circle
                            drawCircle(
                                color = if (idx == selectedDayIndex) Color.White else CosmicPrimary,
                                radius = if (idx == selectedDayIndex) { if (idx == selectedDayIndex) 7f else 4f } else 4f,
                                center = androidx.compose.ui.geometry.Offset(x, yEdits)
                            )
                            if (idx == selectedDayIndex) {
                                drawCircle(
                                    color = CosmicPrimary,
                                    radius = 12f,
                                    center = androidx.compose.ui.geometry.Offset(x, yEdits),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                                )
                            }

                            // Meetings Node Circle
                            drawCircle(
                                color = if (idx == selectedDayIndex) Color.White else SecurityCompliant,
                                radius = if (idx == selectedDayIndex) { if (idx == selectedDayIndex) 7f else 4f } else 4f,
                                center = androidx.compose.ui.geometry.Offset(x, yMeetings)
                            )
                            if (idx == selectedDayIndex) {
                                drawCircle(
                                    color = SecurityCompliant,
                                    radius = 12f,
                                    center = androidx.compose.ui.geometry.Offset(x, yMeetings),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                                )
                            }
                        }
                    }

                    // Invisible Clickable Overlay Columns for perfect interactive response
                    Row(modifier = Modifier.fillMaxSize()) {
                        Spacer(modifier = Modifier.width(20.dp))
                        trendData.forEachIndexed { idx, day ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable { selectedDayIndex = idx }
                            )
                        }
                        Spacer(modifier = Modifier.width(20.dp))
                    }
                }

                // X-Axis Day Labels
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    trendData.forEachIndexed { idx, day ->
                        Text(
                            text = day.dayLabel,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (idx == selectedDayIndex) FontWeight.Bold else FontWeight.Normal,
                                color = if (idx == selectedDayIndex) Color.White else CosmicTextSecondary
                            ),
                            modifier = Modifier.clickable { selectedDayIndex = idx }
                        )
                    }
                }
            }
        }

        // FOCUS DETAILED TOOLTIP BOX (REVEALS DYNAMIC DETAILS ON TAP)
        Card(
            colors = CardDefaults.cardColors(containerColor = CosmicSurfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CosmicPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(selectedDay.dayLabel, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = CosmicPrimary))
                    }

                    Column {
                        Text(
                            text = "Daily Sprint Snapshot • ${selectedDay.dayLabel}day",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = "Edits: ${selectedDay.edits}",
                                style = MaterialTheme.typography.bodySmall,
                                color = CosmicPrimary
                            )
                            Text(
                                text = "Meetings: ${selectedDay.meetings}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SecurityCompliant
                            )
                            if (selectedDay.meetings > 0) {
                                Text(
                                    text = "Attendance: ${selectedDay.attendanceRate}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CosmicSecondary
                                )
                            }
                        }
                    }
                }

                Surface(
                    color = if (selectedDay.edits >= 8) SecurityCompliant.copy(alpha = 0.15f) else CosmicSurfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (selectedDay.edits >= 8) "HIGH ACTIVITY" else "STANDARD WAVE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (selectedDay.edits >= 8) SecurityCompliant else CosmicTextSecondary
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // CO-PILOT AGILITY PRODUCTIVITY ADVISOR PANEL (GEMINI INTEGRATION)
        Card(
            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, CosmicPrimary.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Co-pilot Advisor",
                            tint = CosmicPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "AI Agile Workflow Advisor",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    if (isProductivityLoading) {
                        CircularProgressIndicator(
                            color = CosmicPrimary,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }

                Text(
                    text = "Request your smart co-pilot model to check collaboration patterns, optimize active sprints, and provide structural workspace pipeline insights.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CosmicTextSecondary
                )

                if (productivityRecommendation != null) {
                    Divider(color = CosmicSurfaceVariant, modifier = Modifier.padding(vertical = 4.dp))
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "GEMINI ADVISORY GENERATED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CosmicPrimary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        
                        Text(
                            text = productivityRecommendation!!,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = CosmicTextPrimary,
                                lineHeight = 20.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        viewModel.runProductivityAnalysis(
                            documentCount = trendData.sumOf { it.edits },
                            meetingCount = trendData.sumOf { it.meetings }
                        )
                    },
                    enabled = !isProductivityLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CosmicPrimary,
                        contentColor = Color(0xFF1D192B)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isProductivityLoading) "Consulting Gemini core..." else "Execute Agile Agility Audit",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

