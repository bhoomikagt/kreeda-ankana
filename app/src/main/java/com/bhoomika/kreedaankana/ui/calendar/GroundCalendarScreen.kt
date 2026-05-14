package com.bhoomika.kreedaankana.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bhoomika.kreedaankana.viewmodel.CalendarViewModel
import com.bhoomika.kreedaankana.viewmodel.GroundViewModel
import com.bhoomika.kreedaankana.utils.getNext7Days
import java.time.*
import java.time.format.DateTimeFormatter
import com.bhoomika.kreedaankana.data.model.Ground
import com.bhoomika.kreedaankana.data.local.entity.Booking

// ===============================
// BLOCK MODEL
// ===============================
data class CalendarBlock(
    val start: LocalTime,
    val end: LocalTime,
    val booking: Booking?
)

// ===============================
// BLOCK BUILDER
// ===============================
fun buildCalendarBlocks(
    timeSlots: List<LocalTime>,
    bookings: List<Booking>,
    groundId: String?,
    date: LocalDate
): List<CalendarBlock> {

    val blocks = mutableListOf<CalendarBlock>()
    var i = 0

    while (i < timeSlots.size) {

        val start = timeSlots[i]

        val booking = bookings.find {
            it.groundId == groundId &&
                    it.date == date &&
                    start >= it.startTime && start < it.endTime
        }

        if (booking != null) {

            blocks.add(
                CalendarBlock(
                    booking.startTime,
                    booking.endTime,
                    booking
                )
            )

            val hours = Duration
                .between(booking.startTime, booking.endTime)
                .toHours()
                .toInt()

            i += hours
        } else {

            blocks.add(
                CalendarBlock(
                    start,
                    start.plusHours(1),
                    null
                )
            )

            i++
        }
    }

    return blocks
}

// ===============================
// MAIN SCREEN
// ===============================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroundCalendarScreen(
    vm: CalendarViewModel = viewModel(),
    groundVM: GroundViewModel = viewModel(),
    onBack: (() -> Unit)? = null,
    onSlotClick: (String, LocalDate, LocalTime) -> Unit
) {

    val grounds by groundVM.grounds.collectAsStateWithLifecycle()
    val bookings by vm.bookings.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()

    var selectedGround by remember { mutableStateOf<Ground?>(null) }
    val dates = remember { getNext7Days() }
    var selectedDate by remember { mutableStateOf(dates[0]) }

    // 🔥 Load grounds once
    LaunchedEffect(Unit) {
        groundVM.loadGrounds()
    }

    // 🔥 Listen when ground/date changes
    LaunchedEffect(selectedGround?.id, selectedDate) {
        selectedGround?.let {
            vm.setGround(it.id)
            vm.listenBookings(selectedDate, it.id)
        }
    }

    val timeSlots = remember {
        (6 until 22).map { LocalTime.of(it, 0) }
    }

    val blocks = remember(bookings, selectedGround?.id, selectedDate) {
        buildCalendarBlocks(
            timeSlots,
            bookings,
            selectedGround?.id,
            selectedDate
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Calendar") },
                navigationIcon = {
                    onBack?.let {
                        IconButton(onClick = it) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                        }
                    }
                }
            )
        }
    ) { padding ->

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            // 🔽 Ground selector
            GroundDropdown(grounds, selectedGround) {
                selectedGround = it
            }

            if (selectedGround != null) {

                // 🔽 Date selector
                DateSelector(dates, selectedDate) {
                    selectedDate = it
                }

                Spacer(Modifier.height(8.dp))

                // 🔥 LOADING FIX (NO FLICKER)
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {

                        items(blocks) { block ->

                            val isBooked = block.booking != null
                            val hours = Duration
                                .between(block.start, block.end)
                                .toHours()
                                .toInt()

                            val height = (56 * hours).dp

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                // 🕒 TIME RANGE
                                Text(
                                    "${formatTime(block.start)} - ${formatTime(block.end)}",
                                    modifier = Modifier.width(130.dp),
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Gray
                                )

                                // 📦 SLOT BOX
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(height)
                                        .clickable(enabled = !isBooked) {
                                            selectedGround?.let {
                                                onSlotClick(
                                                    it.id,
                                                    selectedDate,
                                                    block.start
                                                )
                                            }
                                        }
                                        .background(
                                            if (isBooked) Color(0xFFFFE4E6)
                                            else Color(0xFFE7FBEF),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (isBooked) Color.Red else Color(0xFF22C55E),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {

                                    if (isBooked) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {

                                            Text(
                                                block.booking!!.teamName,
                                                fontWeight = FontWeight.SemiBold
                                            )

                                            Spacer(Modifier.height(4.dp))

                                            Text(
                                                "${formatTime(block.start)} - ${formatTime(block.end)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        }
                                    } else {
                                        Text(
                                            "Available",
                                            color = Color(0xFF16A34A),
                                            fontWeight = FontWeight.Medium
                                        )
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

// ===============================
fun formatTime(time: LocalTime): String =
    time.format(DateTimeFormatter.ofPattern("h:mm a"))