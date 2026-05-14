package com.bhoomika.kreedaankana.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bhoomika.kreedaankana.data.local.entity.Booking
import com.bhoomika.kreedaankana.data.model.Ground
import com.bhoomika.kreedaankana.data.model.Team
import com.bhoomika.kreedaankana.viewmodel.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    viewModel: BookingViewModel,
    teamVM: TeamViewModel,
    groundVM: GroundViewModel,
    onBack: () -> Unit,
    preselectedGroundId: String? = null,
    preselectedDate: LocalDate? = null,
    preselectedTime: LocalTime? = null
) {
    val scope = rememberCoroutineScope()
    val teams by teamVM.teams.collectAsState()
    val grounds by groundVM.grounds.collectAsState()
    val bookings by viewModel.bookings.collectAsState(initial = emptyList())

    val snackbarHostState = remember { SnackbarHostState() }
    val message by viewModel.bookingMessage.collectAsState(initial = null)

    var selectedTeam by remember { mutableStateOf<Team?>(null) }
    var selectedGround by remember { mutableStateOf<Ground?>(null) }
    var selectedDate by remember { mutableStateOf(preselectedDate ?: LocalDate.now()) }

    var teamExpanded by remember { mutableStateOf(false) }
    var groundExpanded by remember { mutableStateOf(false) }

    val selectedSlots = remember { mutableStateListOf<LocalTime>() }
    val lockedSlot = preselectedTime

    val isFromCalendar = preselectedGroundId != null

    // Load data
    LaunchedEffect(Unit) {
        teamVM.loadTeams()
        groundVM.loadGrounds()
    }

    LaunchedEffect(selectedDate) {
        selectedSlots.clear()
    }

    // Lock ground
    LaunchedEffect(grounds) {
        if (isFromCalendar) {
            selectedGround = grounds.find { it.id == preselectedGroundId }
        }
    }

    // Preselect slot
    LaunchedEffect(preselectedTime) {
        if (preselectedTime != null && selectedSlots.isEmpty()) {
            selectedSlots.add(preselectedTime)
        }
    }

    // Load bookings
    LaunchedEffect(selectedGround, selectedDate) {
        selectedGround?.let {
            viewModel.loadBookings(selectedDate)
        }
    }

    // Snackbar
    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    val filteredTeams = if (isFromCalendar && selectedGround != null) {
        teams.filter { selectedGround!!.sports.contains(it.teamSport) }
    } else teams

    val filteredGrounds = if (!isFromCalendar && selectedTeam != null) {
        grounds.filter { it.sports.contains(selectedTeam!!.teamSport) }
    } else grounds

    val slots = generateHourSlots()

    val availableDates = remember {
        List(7) { LocalDate.now().plusDays(it.toLong()) }
    }

// ensure preselected date is used if present
    LaunchedEffect(preselectedDate) {
        preselectedDate?.let {
            selectedDate = it
        }
    }

    val selectedRangeText = if (selectedSlots.isEmpty()) ""
    else {
        val start = selectedSlots.minOrNull()!!
        val end = selectedSlots.maxOrNull()!!.plusHours(1)
        "${formatTime(start)} - ${formatTime(end)}"
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },

        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Book Slot") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        },

        // ✅ FIXED BUTTON
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = {
                        if (selectedTeam == null) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Select a team")
                            }
                            return@Button
                        }

                        if (selectedGround == null) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Select a ground")
                            }
                            return@Button
                        }

                        if (selectedSlots.isEmpty()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Select time slots")
                            }
                            return@Button
                        }

                        val team = selectedTeam ?: return@Button
                        val ground = selectedGround ?: return@Button
                        val start = selectedSlots.minOrNull() ?: return@Button
                        val end = selectedSlots.maxOrNull()?.plusHours(1) ?: return@Button

                        viewModel.bookSlot(
                            Booking(
                                teamId = team.id,
                                teamName = team.teamName,
                                groundId = ground.id,
                                groundName = ground.name,
                                sport = team.teamSport,
                                userId = "",
                                date = selectedDate,
                                startTime = start,
                                endTime = end
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Confirm Booking")
                }
            }
        }

    ) { padding ->

        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
        ) {

            // TEAM
            ExposedDropdownMenuBox(
                expanded = teamExpanded,
                onExpandedChange = { teamExpanded = !teamExpanded }
            ) {
                OutlinedTextField(
                    value = selectedTeam?.teamName ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Team") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(teamExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()   // ✅ REQUIRED (THIS FIXES IT)
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = teamExpanded,
                    onDismissRequest = { teamExpanded = false }
                ) {
                    filteredTeams.forEach {
                        DropdownMenuItem(
                            text = { Text(it.teamName) },
                            onClick = {
                                selectedTeam = it
                                if (!isFromCalendar) selectedGround = null
                                teamExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // GROUND
            if (isFromCalendar) {
                OutlinedTextField(
                    value = selectedGround?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Ground") },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                ExposedDropdownMenuBox(
                    expanded = groundExpanded,
                    onExpandedChange = { groundExpanded = !groundExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedGround?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Ground") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(groundExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = groundExpanded,
                        onDismissRequest = { groundExpanded = false }
                    ) {
                        if (filteredGrounds.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No grounds available") },
                                onClick = { groundExpanded = false }
                            )
                        } else {
                            filteredGrounds.forEach { ground ->
                                DropdownMenuItem(
                                    text = { Text(ground.name) },
                                    onClick = {
                                        selectedGround = ground
                                        groundExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))

            Text(
                text = "Select Date",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                availableDates.forEach { date ->

                    val isSelected = date == selectedDate

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) Color(0xFF2563EB) else Color(0xFFF1F5F9)
                            )
                            .clickable {
                                selectedDate = date
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {

                            Text(
                                text = date.dayOfWeek.name.take(3),
                                color = if (isSelected) Color.White else Color.Gray,
                                style = MaterialTheme.typography.labelSmall
                            )

                            Text(
                                text = date.dayOfMonth.toString(),
                                color = if (isSelected) Color.White else Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            Spacer(Modifier.height(12.dp))

            Text(
                text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(4.dp))

            Text("Select Time Slots")

            if (selectedRangeText.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Selected: $selectedRangeText",
                    color = Color(0xFF2563EB),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(8.dp))

            // ✅ FIXED GRID (NO LazyGrid)
            slots.chunked(3).forEach { row ->

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {

                    row.forEach { start ->

                        val end = start.plusHours(1)

                        val isBooked = bookings.any {
                            it.date == selectedDate &&
                                    it.groundId == selectedGround?.id &&
                                    !(it.endTime <= start || it.startTime >= end)
                        }

                        val isSelected = selectedSlots.contains(start)
                        val isLocked = start == lockedSlot

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                                .clip(RoundedCornerShape(30.dp))
                                .background(
                                    when {
                                        isLocked -> Color(0xFFDBEAFE)
                                        isSelected -> Color(0xFFDCFCE7)
                                        else -> Color.White
                                    }
                                )
                                .border(
                                    2.dp,
                                    when {
                                        isBooked -> Color.Red
                                        isLocked -> Color(0xFF2563EB)
                                        isSelected -> Color(0xFF22C55E)
                                        else -> Color.LightGray
                                    },
                                    RoundedCornerShape(30.dp)
                                )
                                .clickable {

                                    if (isLocked || isBooked) return@clickable

                                    // FIRST CLICK
                                    if (selectedSlots.isEmpty()) {
                                        selectedSlots.add(start)
                                        return@clickable
                                    }

                                    val first = selectedSlots.first()

                                    // ❌ NO REVERSE SELECTION
                                    if (start < first) {
                                        selectedSlots.clear()
                                        selectedSlots.add(start)
                                        return@clickable
                                    }

                                    val currentMin = selectedSlots.minOrNull()!!
                                    val currentMax = selectedSlots.maxOrNull()!!

                                    // 🔥 CLICK INSIDE RANGE → RESET
                                    if (start in currentMin..currentMax) {
                                        selectedSlots.clear()
                                        selectedSlots.add(start)
                                        return@clickable
                                    }

                                    // ✅ FORWARD RANGE ONLY
                                    val range = slots.filter { it >= first && it <= start }

                                    // ❌ BLOCK CHECK
                                    val hasBlocked = range.any { slot ->
                                        val endTime = slot.plusHours(1)
                                        bookings.any {
                                            it.date == selectedDate &&
                                                    it.groundId == selectedGround?.id &&
                                                    !(it.endTime <= slot || it.startTime >= endTime)
                                        }
                                    }

                                    // 🔥 RESET if blocked
                                    if (hasBlocked) {
                                        selectedSlots.clear()
                                        selectedSlots.add(start)
                                        return@clickable
                                    }

                                    // ✅ VALID RANGE
                                    selectedSlots.clear()
                                    lockedSlot?.let { selectedSlots.add(it) }
                                    selectedSlots.addAll(range)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(formatTime(start))
                        }
                    }

                    repeat(3 - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.height(12.dp))


        }
    }
}

// ===============================
fun formatTime(time: LocalTime): String =
    time.format(DateTimeFormatter.ofPattern("h:mm a"))

fun generateHourSlots(): List<LocalTime> {
    val list = mutableListOf<LocalTime>()
    var t = LocalTime.of(6, 0)
    repeat(16) {
        list.add(t)
        t = t.plusHours(1)
    }
    return list
}