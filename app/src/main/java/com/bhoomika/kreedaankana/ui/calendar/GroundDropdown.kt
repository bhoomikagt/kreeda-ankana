package com.bhoomika.kreedaankana.ui.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.bhoomika.kreedaankana.data.model.Ground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroundDropdown(
    grounds: List<Ground>,
    selected: Ground?,
    onSelect: (Ground) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = selected?.name ?: "Select Ground",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            grounds.forEach { ground ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(ground.name)

                            // 🔥 SHOW SPORTS (extra info)
                            if (ground.sports.isNotEmpty()) {
                                Text(
                                    ground.sports.joinToString(", "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    },
                    onClick = {
                        onSelect(ground)
                        expanded = false
                    }
                )
            }
        }
    }
}