package com.bhoomika.kreedaankana.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items   // ⭐ IMPORTANT FIX
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bhoomika.kreedaankana.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {

    val vm: ProfileViewModel = viewModel()
    val user by vm.user.collectAsState()
    val teams by vm.teams.collectAsState()

    var deleteTeamId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        vm.loadProfile()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        },
        containerColor = Color(0xFFF5F7FA)
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            item {

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(Color(0xFF4CAF50), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user?.name?.firstOrNull()?.uppercase() ?: "U",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(user?.name ?: "User", style = MaterialTheme.typography.titleLarge)
                Text(user?.email ?: "", color = Color.Gray)

                Spacer(modifier = Modifier.height(24.dp))

                Text("MY TEAMS", modifier = Modifier.fillMaxWidth(), color = Color.Gray)

                Spacer(modifier = Modifier.height(8.dp))
            }

            items(teams) { team ->

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(team.name)

                    Text(
                        text = if (team.isAdmin) "Admin" else "Leave",
                        color = if (team.isAdmin) Color.Gray else Color.Red,
                        modifier = Modifier.clickable {
                            if (!team.isAdmin) {
                                deleteTeamId = team.id
                            }
                        }
                    )
                }

                Divider()
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        onLogout()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Logout")
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // 🔥 LEAVE TEAM CONFIRMATION (RESTORED)
    deleteTeamId?.let { id ->
        AlertDialog(
            onDismissRequest = { deleteTeamId = null },
            confirmButton = {
                TextButton(onClick = {
                    vm.leaveTeam(id) {}
                    deleteTeamId = null
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTeamId = null }) {
                    Text("Cancel")
                }
            },
            title = { Text("Leave Team") },
            text = { Text("Are you sure you want to leave this team?") }
        )
    }
}