package com.bhoomika.kreedaankana.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    showBack: Boolean = false,
    onBack: (() -> Unit)? = null
) {
    CenterAlignedTopAppBar(

        title = {
            Text(title)
        },

        navigationIcon = {
            if (showBack) {
                IconButton(onClick = { onBack?.invoke() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        }
    )
}