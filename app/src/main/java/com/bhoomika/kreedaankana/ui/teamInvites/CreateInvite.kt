package com.bhoomika.kreedaankana.ui.teamInvites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bhoomika.kreedaankana.data.local.entity.Booking
import com.bhoomika.kreedaankana.data.model.Team
import com.bhoomika.kreedaankana.viewmodel.BookingViewModel
import com.bhoomika.kreedaankana.viewmodel.InviteTeamViewModel
import com.bhoomika.kreedaankana.viewmodel.TeamViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInviteScreen(
    inviteVM: InviteTeamViewModel,
    teamVM: TeamViewModel,
    bookingVM: BookingViewModel,
    onBack: () -> Unit,
    onCreated: () -> Unit
) {

    val teams by teamVM.teams.collectAsState()
    val bookings by bookingVM.bookings.collectAsState()

    var hostTeam by remember { mutableStateOf<Team?>(null) }
    var opponentTeam by remember { mutableStateOf<Team?>(null) }

    var hostExpanded by remember { mutableStateOf(false) }
    var oppExpanded by remember { mutableStateOf(false) }

    var selectedBooking by remember { mutableStateOf<Booking?>(null) }

    var message by remember { mutableStateOf("") }

    // 🔥 Load teams
    LaunchedEffect(Unit) {
        teamVM.loadTeams()
    }

    // 🔥 Load bookings (next 7 days like dashboard)
    LaunchedEffect(Unit) {
        bookingVM.clearBookings()

        val today = LocalDate.now()
        (0..6).forEach {
            bookingVM.loadBookings(today.plusDays(it.toLong()))
        }
    }

    // 🔥 Filter opponent teams based on sport
    val opponentTeams = remember(hostTeam, teams) {
        if (hostTeam == null) emptyList()
        else teams.filter {
            it.teamSport == hostTeam!!.teamSport &&
                    it.id != hostTeam!!.id
        }
    }

    // 🔥 Filter bookings for selected host team
    val filteredBookings = remember(hostTeam, bookings) {
        if (hostTeam == null) emptyList()
        else bookings.filter { it.teamId == hostTeam!!.id }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Create Invite") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 🔽 HOST TEAM
            ExposedDropdownMenuBox(
                expanded = hostExpanded,
                onExpandedChange = { hostExpanded = !hostExpanded }
            ) {
                OutlinedTextField(
                    value = hostTeam?.teamName ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Your Team") },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryEditable)
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = hostExpanded,
                    onDismissRequest = { hostExpanded = false }
                ) {
                    teams.forEach {
                        DropdownMenuItem(
                            text = { Text(it.teamName) },
                            onClick = {
                                hostTeam = it
                                opponentTeam = null
                                selectedBooking = null
                                hostExpanded = false
                            }
                        )
                    }
                }
            }

            // 🔽 OPPONENT TEAM
            ExposedDropdownMenuBox(
                expanded = oppExpanded,
                onExpandedChange = { oppExpanded = !oppExpanded }
            ) {
                OutlinedTextField(
                    value = opponentTeam?.teamName ?: "",
                    onValueChange = {},
                    readOnly = true,
                    enabled = hostTeam != null,
                    label = { Text("Opponent Team") },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryEditable)
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = oppExpanded,
                    onDismissRequest = { oppExpanded = false }
                ) {
                    opponentTeams.forEach {
                        DropdownMenuItem(
                            text = { Text(it.teamName) },
                            onClick = {
                                opponentTeam = it
                                oppExpanded = false
                            }
                        )
                    }
                }
            }

            // 🔥 BOOKINGS LIST
            Text("Select Slot")

            if (filteredBookings.isEmpty()) {
                Text("No bookings found for this team", color = Color.Gray)
            } else {

                LazyColumn(
                    modifier = Modifier.height(250.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    items(filteredBookings) { booking ->

                        val isSelected =
                            selectedBooking?.bookingId == booking.bookingId

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedBooking = booking },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected)
                                    Color(0xFFEDE9FE) else Color.White
                            ),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {

                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {

                                Column {
                                    Text(booking.teamName)

                                    Text(
                                        booking.groundName,
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.bodySmall
                                    )

                                    Text(
                                        "${booking.startTime} - ${booking.endTime}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Text(
                                    booking.date.toString(),
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            // ✍️ MESSAGE
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Message (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            // 🚀 CREATE BUTTON
            Button(
                onClick = {
                    println("ButtonClicked")
                    if (hostTeam == null ||
                        opponentTeam == null ||
                        selectedBooking == null
                    ) return@Button

                    inviteVM.createInvite(
                        hostTeam = hostTeam!!,
                        opponentTeam = opponentTeam!!,
                        booking = selectedBooking!!,
                        message = message
                    )

                    onCreated()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Post Invite")
            }
        }
    }
}