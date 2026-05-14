package com.bhoomika.kreedaankana.ui.team

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bhoomika.kreedaankana.data.model.Member
import com.bhoomika.kreedaankana.viewmodel.TeamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTeamScreen(
    onDone: () -> Unit,
    onBack: () -> Unit
) {

    val vm: TeamViewModel = viewModel()
    val context = LocalContext.current

    var teamName by remember { mutableStateOf("") }
    var sport by remember { mutableStateOf("") }

    val members = remember { mutableStateListOf<Member>() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Create Team") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        },
        containerColor = Color(0xFFF6F7FB)
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // 🔥 TEAM DETAILS CARD
            item {
                PremiumCard(isPrimary = true) {

                    Text(
                        "Team Details",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(Modifier.height(12.dp))

                    PremiumTextField(
                        value = teamName,
                        onValueChange = { teamName = it },
                        label = "Team Name"
                    )

                    Spacer(Modifier.height(12.dp))

                    PremiumTextField(
                        value = sport,
                        onValueChange = { sport = it },
                        label = "Sport Type"
                    )
                }
            }

            // 🔥 MEMBERS HEADER
            item {
                Text(
                    "Members",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray
                )
                if (members.isEmpty()) {
                    Text(
                        "No members added",
                        color = Color.Gray,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }



            // 🔥 MEMBERS LIST
            itemsIndexed(members) { index, member ->

                PremiumCard {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            "Member ${index + 1}",
                            style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f)
                        )

                        // 🔥 REMOVE MEMBER
                        if (members.size >= 1) {
                            IconButton(
                                onClick = { members.removeAt(index) }
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    PremiumTextField(
                        value = member.name,
                        onValueChange = {
                            members[index] = member.copy(name = it)
                        },
                        label = "Name"
                    )

                    Spacer(Modifier.height(12.dp))

                    PremiumTextField(
                        value = member.email,
                        onValueChange = {
                            members[index] = member.copy(email = it)
                        },
                        label = "Email"
                    )
                }
            }

            // 🔥 ADD MEMBER
            item {
                Text(
                    "+ Add Member",
                    color = Color(0xFF4F46E5),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { members.add(Member()) }
                        .padding(8.dp)
                )
            }

            // 🔥 CREATE BUTTON
            item {
                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (teamName.isBlank() || sport.isBlank()) {
                            Toast.makeText(context, "Fill details", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        vm.createTeam(teamName, sport, members) {
                            if (it) {
                                Toast.makeText(context, "Team created", Toast.LENGTH_SHORT).show()
                                onDone()
                            } else {
                                Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Create Team")
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun PremiumCard(
    isPrimary: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(
            if (isPrimary) 8.dp else 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimary) Color(0xFFF8FAFF) else Color.White
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}
@Composable
fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF4F46E5),
            unfocusedBorderColor = Color.LightGray
        )
    )
}