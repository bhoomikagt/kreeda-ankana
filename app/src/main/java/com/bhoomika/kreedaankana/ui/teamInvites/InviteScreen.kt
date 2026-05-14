package com.bhoomika.kreedaankana.ui.teamInvites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bhoomika.kreedaankana.viewmodel.InviteTeamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun inviteScreen(
    viewModel: InviteTeamViewModel,
    onBack: () -> Unit,
    onCreateClick: () -> Unit,
    onRespondClick: (String) -> Unit
) {

    val invites by viewModel.invites.collectAsState()
    val userTeams by viewModel.userTeamIds.collectAsState()
    val replyMap by viewModel.replyMap.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("All", "Sent", "Received")

    LaunchedEffect(Unit) {
        viewModel.loadinvites()
        viewModel.loadUserTeams()
    }

    // ✅ FIXED FILTERING (NO DUPLICATES)
    val filteredinvites = when (selectedTab) {

        // ALL
        0 -> invites

        // MY inviteS (HOST ONLY)
        1 -> invites.filter {
            userTeams.contains(it.hostTeamId)
        }

        // FOR ME (ONLY opponent AND NOT host)
        2 -> invites.filter {
            userTeams.contains(it.opponentTeamId) &&
                    !userTeams.contains(it.hostTeamId)
        }

        else -> invites
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Team Invites") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = onCreateClick) {
                        Icon(Icons.Default.Add, contentDescription = "Create Invite")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            if (filteredinvites.isEmpty()) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text("No invites")

                    Spacer(Modifier.height(8.dp))

                    Text(
                        when (selectedTab) {
                            1 -> "You haven’t created any invites"
                            2 -> "No invites for your teams"
                            else -> "Be the first to invite another team!"
                        },
                        color = Color.Gray
                    )
                }

            } else {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    items(filteredinvites, key = { it.id }) { invite ->

                        var expanded by remember { mutableStateOf(false) }
                        val replies = replyMap[invite.id] ?: emptyList()

                        val isHost = userTeams.contains(invite.hostTeamId)
                        val isOpponent = userTeams.contains(invite.opponentTeamId)

                        // ✅ ROLE PRIORITY FIX
                        val effectiveRole = when {
                            isHost -> "HOST"
                            isOpponent -> "OPPONENT"
                            else -> "VIEWER"
                        }

                        LaunchedEffect(expanded) {
                            if (expanded) {
                                viewModel.loadRepliesForinvite(invite.id)
                            }
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = !expanded },
                            shape = RoundedCornerShape(18.dp),
                            elevation = CardDefaults.cardElevation(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {

                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {

                                Text(
                                    invite.hostTeamName,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(
                                    "vs ${invite.opponentTeamName}",
                                    color = Color.Gray
                                )

                                Text(
                                    invite.sport,
                                    color = Color(0xFF6C4AB6)
                                )

                                if (invite.message.isNotBlank()) {
                                    Text(invite.message)
                                }

                                Text(
                                    "${invite.date} • ${invite.startTime} - ${invite.endTime}",
                                    color = Color.Gray
                                )

                                Text(
                                    if (invite.status == "open") "Open" else "Accepted",
                                    color = if (invite.status == "open")
                                        Color(0xFF16A34A)
                                    else Color(0xFFDC2626)
                                )

                                Text(
                                    if (expanded) "Hide replies ▲" else "View replies ▼",
                                    color = Color.Gray
                                )

                                // ✅ FIXED BUTTON LOGIC
                                when {

                                    // HOST (even if also opponent)
                                    selectedTab == 1 &&
                                            effectiveRole == "HOST" &&
                                            invite.status == "open" -> {

                                        Text(
                                            "Reply",
                                            color = Color(0xFF6C4AB6),
                                            modifier = Modifier.clickable {
                                                onRespondClick(
                                                    "${invite.id}|${effectiveRole}"
                                                )
                                            }
                                        )
                                    }

                                    // ONLY opponent (NOT host)
                                    selectedTab == 2 &&
                                            effectiveRole == "OPPONENT" &&
                                            !isHost &&
                                            invite.status == "open" -> {

                                        Text(
                                            "Respond",
                                            color = Color(0xFF6C4AB6),
                                            modifier = Modifier.clickable {
                                                onRespondClick(
                                                    "${invite.id}|${effectiveRole}"
                                                )
                                            }
                                        )
                                    }
                                }

                                // ✅ CHAT UI (UNCHANGED, CORRECT)
                                if (expanded) {

                                    Divider()

                                    if (replies.isEmpty()) {
                                        Text("No replies yet", color = Color.Gray)
                                    } else {

                                        replies.forEach { reply ->

                                            val isHostReply =
                                                reply.teamId == invite.hostTeamId

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = if (isHostReply)
                                                    Arrangement.Start
                                                else Arrangement.End
                                            ) {

                                                Card(
                                                    shape = RoundedCornerShape(16.dp),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = if (isHostReply)
                                                            Color(0xFFF1F5F9)
                                                        else
                                                            Color(0xFFDCFCE7)
                                                    ),
                                                    modifier = Modifier.widthIn(max = 260.dp)
                                                ) {

                                                    Column(
                                                        modifier = Modifier.padding(10.dp)
                                                    ) {

                                                        Text(
                                                            "${reply.teamName} • ${reply.userName}",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = Color.Gray
                                                        )

                                                        Spacer(Modifier.height(2.dp))

                                                        Text(reply.message)
                                                    }
                                                }
                                            }

                                            Spacer(Modifier.height(6.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}