package com.bhoomika.kreedaankana.ui.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@Composable
fun DateSelector(
    dates: List<LocalDate>,
    selectedDate: LocalDate,
    onSelect: (LocalDate) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {

        items(dates) { date ->

            val isSelected = date == selectedDate

            Card(
                modifier = Modifier.clickable { onSelect(date) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFF4F46E5) else Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(date.dayOfWeek.name.take(3))
                    Text(date.dayOfMonth.toString())
                }
            }
        }
    }
}