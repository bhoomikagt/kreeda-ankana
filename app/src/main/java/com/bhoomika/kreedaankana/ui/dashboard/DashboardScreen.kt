package com.bhoomika.kreedaankana.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bhoomika.kreedaankana.data.local.entity.Booking
import com.bhoomika.kreedaankana.viewmodel.BookingViewModel
import com.bhoomika.kreedaankana.viewmodel.ChallengeViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    bookingVM: BookingViewModel,
    onNavigateToBooking: () -> Unit,
    onNavigateToCreateTeam: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToInvites: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToChallengeBoard: () -> Unit,
    onNavigateToScoreWall: () -> Unit
) {

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {

            ModalDrawerSheet {

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "Kreeda Ankana",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium
                )

                HorizontalDivider()

                NavigationDrawerItem(
                    label = { Text("Profile") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToProfile()
                    }
                )

                NavigationDrawerItem(
                    label = { Text("Book Ground") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToBooking()
                    }
                )

                NavigationDrawerItem(
                    label = { Text("Create Team") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToCreateTeam()
                    }
                )

                NavigationDrawerItem(
                    label = { Text("Team Invites") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToInvites()
                    }
                )

                NavigationDrawerItem(
                    label = { Text("Ground Calendar") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToCalendar()
                    }
                )

                NavigationDrawerItem(
                    label = { Text("Challenge Board") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToChallengeBoard()
                    }
                )

                NavigationDrawerItem(
                    label = { Text("Score Wall") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                        }
                        onNavigateToScoreWall()
                    }
                )
            }
        }
    ) {

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Kreeda Ankana") },

                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = null
                            )
                        }
                    },

                    actions = {
                        IconButton(
                            onClick =
                                onNavigateToProfile
                        ) {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = null
                            )
                        }
                    }
                )
            },

            containerColor =
                Color(0xFFF6F7FB)

        ) { padding ->

            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),

                verticalArrangement =
                    Arrangement.spacedBy(20.dp)
            ) {

                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )

                Text(
                    "Welcome back",
                    color = Color.Gray
                )

                Text(
                    "Ready to play?",
                    style =
                        MaterialTheme
                            .typography
                            .headlineSmall
                )

                DashboardContent(
                    bookingVM
                )
            }
        }
    }
}

@Composable
fun DashboardContent(
    bookingVM: BookingViewModel
) {

    val challengeVM: ChallengeViewModel =
        viewModel()

    val bookings by
    bookingVM.bookings.collectAsState()

    val challengeBookingIds by
    challengeVM
        .challengeBookingIds
        .collectAsState()

    val today = LocalDate.now()
    val teamAdminState by
    challengeVM
        .teamAdminState
        .collectAsState()

    var selectedBooking by remember {
        mutableStateOf<Booking?>(null)
    }

    var description by remember {
        mutableStateOf("")
    }

    // ==========================
    // LOAD BOOKINGS + CHALLENGES
    // ==========================
    LaunchedEffect(Unit) {

        bookingVM.clearBookings()

        challengeVM.loadAllChallenges()

        (0..6).forEach {

            bookingVM.loadBookings(
                today.plusDays(
                    it.toLong()
                )
            )
        }
    }

    Column(
        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {

        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Icon(
                Icons.Default.DateRange,
                contentDescription = null
            )

            Spacer(
                modifier =
                    Modifier.width(8.dp)
            )

            Text(
                "Upcoming Bookings",
                style =
                    MaterialTheme
                        .typography
                        .titleMedium
            )
        }

        if (bookings.isEmpty()) {

            Text(
                "No bookings yet",
                color = Color.Gray
            )

        } else {

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),

                verticalArrangement =
                    Arrangement.spacedBy(16.dp)
            ) {

                val grouped =
                    bookings
                        .sortedWith(
                            compareBy(
                                { it.date },
                                { it.startTime }
                            )
                        )
                        .groupBy {
                            it.date
                        }

                grouped.forEach {
                        (date, dayBookings) ->

                    item {

                        Text(
                            text =
                                formatDateHeader(
                                    date
                                ),

                            style =
                                MaterialTheme
                                    .typography
                                    .titleMedium,

                            color =
                                Color(
                                    0xFF4F46E5
                                )
                        )
                    }

                    items(
                        dayBookings,
                        key = {
                            it.bookingId
                        }
                    ) { booking ->

                        val alreadyCreated =
                            booking.bookingId in
                        challengeBookingIds
                        LaunchedEffect(
                            booking.teamId
                        ) {

                            challengeVM.checkAdmin(
                                booking.teamId
                            )
                        }

                        val canCreateChallenge =
                            teamAdminState[
                                booking.teamId
                            ] == true

                        Card(
                            shape =
                                RoundedCornerShape(
                                    16.dp
                                ),

                            colors =
                                CardDefaults
                                    .cardColors(
                                        containerColor =
                                            Color.White
                                    ),

                            elevation =
                                CardDefaults
                                    .cardElevation(
                                        6.dp
                                    ),

                            modifier =
                                Modifier
                                    .fillMaxWidth()
                        ) {

                            Row(
                                modifier =
                                    Modifier
                                        .padding(
                                            14.dp
                                        )
                                        .fillMaxWidth(),

                                horizontalArrangement =
                                    Arrangement.SpaceBetween
                            ) {

                                Column {

                                    Text(
                                        booking.teamName,

                                        style =
                                            MaterialTheme
                                                .typography
                                                .titleSmall
                                    )

                                    Spacer(
                                        Modifier.height(
                                            4.dp
                                        )
                                    )

                                    Row(
                                        verticalAlignment =
                                            Alignment.CenterVertically
                                    ) {

                                        Icon(
                                            imageVector =
                                                Icons.Default.Place,

                                            contentDescription =
                                                null,

                                            tint =
                                                Color(
                                                    0xFF6C4AB6
                                                ),

                                            modifier =
                                                Modifier.size(
                                                    16.dp
                                                )
                                        )

                                        Spacer(
                                            Modifier.width(
                                                6.dp
                                            )
                                        )

                                        Text(
                                            booking.groundName,

                                            style =
                                                MaterialTheme
                                                    .typography
                                                    .bodySmall,

                                            color =
                                                Color.Gray
                                        )
                                    }

                                    Spacer(
                                        Modifier.height(
                                            4.dp
                                        )
                                    )

                                    Text(
                                        "${booking.startTime} - ${booking.endTime}",

                                        style =
                                            MaterialTheme
                                                .typography
                                                .bodySmall
                                    )

                                    Spacer(
                                        Modifier.height(
                                            10.dp
                                        )
                                    )

                                    // ==========================
                                    // CHALLENGE BUTTON LOGIC
                                    // ==========================
                                    if (canCreateChallenge) {

                                        if (
                                            alreadyCreated
                                        ) {

                                            Surface(
                                                color =
                                                    Color(
                                                        0xFFDCFCE7
                                                    ),

                                                shape =
                                                    RoundedCornerShape(
                                                        12.dp
                                                    )
                                            ) {

                                                Text(
                                                    text =
                                                        "✓ Challenge Created",

                                                    modifier =
                                                        Modifier.padding(
                                                            horizontal = 14.dp,
                                                            vertical = 10.dp
                                                        ),

                                                    color =
                                                        Color(
                                                            0xFF166534
                                                        )
                                                )
                                            }

                                        } else {

                                            Button(
                                                onClick = {
                                                    selectedBooking =
                                                        booking
                                                }
                                            ) {

                                                Text(
                                                    "Create Challenge"
                                                )
                                            }
                                        }
                                    }
                                }

                                Text(
                                    booking.date
                                        .dayOfWeek
                                        .name
                                        .take(3),

                                    style =
                                        MaterialTheme
                                            .typography
                                            .bodySmall,

                                    color =
                                        Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ==========================
    // CREATE CHALLENGE DIALOG
    // ==========================
    selectedBooking?.let {
            booking ->

        AlertDialog(

            onDismissRequest = {
                selectedBooking =
                    null
            },

            title = {
                Text(
                    "Create Challenge"
                )
            },

            text = {

                Column {

                    Text(
                        booking.teamName,

                        style =
                            MaterialTheme
                                .typography
                                .titleMedium
                    )

                    Text(
                        booking.groundName
                    )

                    Text(
                        "${booking.startTime} - ${booking.endTime}"
                    )

                    Spacer(
                        Modifier.height(
                            12.dp
                        )
                    )

                    OutlinedTextField(
                        value =
                            description,

                        onValueChange = {
                            description = it
                        },

                        label = {
                            Text(
                                "Challenge Description"
                            )
                        },

                        modifier =
                            Modifier.fillMaxWidth()
                    )
                }
            },

            confirmButton = {

                Button(
                    onClick = {

                        challengeVM
                            .createChallenge(
                                booking,
                                description
                            )

                        description = ""

                        selectedBooking =
                            null
                    }
                ) {
                    Text("Post")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        selectedBooking =
                            null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

fun formatDateHeader(
    date: LocalDate
): String {

    val today =
        LocalDate.now()

    val tomorrow =
        today.plusDays(1)

    return when (date) {

        today -> "Today"

        tomorrow ->
            "Tomorrow"

        else -> {
            "${date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }}, ${date.dayOfMonth}"
        }
    }
}