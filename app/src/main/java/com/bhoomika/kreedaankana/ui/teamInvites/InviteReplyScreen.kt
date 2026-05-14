package com.bhoomika.kreedaankana.ui.teamInvites

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bhoomika.kreedaankana.viewmodel.InviteTeamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReplyScreen(
    inviteId: String,
    role: String, // ✅ USE THIS
    inviteVM: InviteTeamViewModel,
    onBack: () -> Unit
) {

    val replies by inviteVM.replies.collectAsState()
    val invites by inviteVM.invites.collectAsState()

    val invite = invites.find { it.id == inviteId }

    var message by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        inviteVM.loadReplies(inviteId)
        inviteVM.loadinvites()
    }

    // ✅ ROLE-BASED LOGIC (FIXED)
    val isHost = role == "HOST"
    val isOpponent = role == "OPPONENT"

    val isAccepted = invite?.status == "accepted"

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Respond") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {

            // 🔴 BLOCK STATES
            if (isAccepted) {
                Text(
                    "Invite already accepted",
                    color = MaterialTheme.colorScheme.error
                )
            } else {

                // ✍️ MESSAGE INPUT
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Reply Message") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                // ✅ BUTTONS BASED ON ROLE
                when {

                    // 🔵 HOST → ONLY REPLY
                    isHost -> {
                        Button(
                            onClick = {
                                inviteVM.addReplyAsHost(inviteId, message)
                                message = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = message.isNotBlank()
                        ) {
                            Text("Reply")
                        }
                    }

                    // 🟢 OPPONENT → REPLY + ACCEPT
                    isOpponent -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            OutlinedButton(
                                onClick = {
                                    inviteVM.addReplyAuto(inviteId, message)
                                    message = ""
                                },
                                modifier = Modifier.weight(1f),
                                enabled = message.isNotBlank()
                            ) {
                                Text("Reply")
                            }

                            Button(
                                onClick = {
                                    inviteVM.replyAndAccept(inviteId, message)
                                    message = ""
                                },
                                modifier = Modifier.weight(1f),
                                enabled = message.isNotBlank()
                            ) {
                                Text("Reply & Accept")
                            }
                        }
                    }

                    // 🔒 VIEW ONLY (fallback)
                    else -> {
                        Text(
                            "View only",
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Text("Replies", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(10.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(replies) { reply ->

                    Card(shape = RoundedCornerShape(12.dp)) {
                        Column(Modifier.padding(12.dp)) {

                            // ✅ TEAM + USER NAME
                            Text(
                                "${reply.teamName} • ${reply.userName}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray
                            )

                            Spacer(Modifier.height(4.dp))

                            Text(reply.message)
                        }
                    }
                }
            }
        }
    }
}