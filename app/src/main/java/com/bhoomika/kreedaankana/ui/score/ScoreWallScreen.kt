package com.bhoomika.kreedaankana.ui.score

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bhoomika.kreedaankana.data.model.Challenge
import com.bhoomika.kreedaankana.data.model.MatchResult
import com.bhoomika.kreedaankana.viewmodel.ScoreWallViewModel
import kotlinx.coroutines.tasks.await


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreWallScreen(
    onBack: () -> Unit
) {

    val viewModel: ScoreWallViewModel =
        viewModel()

    var selectedTab by remember {
        mutableIntStateOf(0)
    }

    val upcomingMatches by
    viewModel.upcomingMatches
        .collectAsState()

    val results by
    viewModel.results
        .collectAsState()

    var selectedMatch by remember {
        mutableStateOf<Challenge?>(null)
    }

    var hostScore by remember {
        mutableStateOf("")
    }

    var opponentScore by remember {
        mutableStateOf("")
    }

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Scaffold(

        topBar = {

            CenterAlignedTopAppBar(

                title = {
                    Text("Score Wall")
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBack
                    ) {

                        Icon(
                            imageVector =
                                Icons.AutoMirrored
                                    .Filled
                                    .ArrowBack,

                            contentDescription =
                                "Back"
                        )
                    }
                }
            )
        }

    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            TabRow(
                selectedTabIndex =
                    selectedTab
            ) {

                Tab(
                    selected =
                        selectedTab == 0,

                    onClick = {
                        selectedTab = 0
                    },

                    text = {
                        Text("Upcoming")
                    }
                )

                Tab(
                    selected =
                        selectedTab == 1,

                    onClick = {
                        selectedTab = 1
                    },

                    text = {
                        Text("Results")
                    }
                )
            }

            // ==========================
            // UPCOMING MATCHES
            // ==========================
            if (selectedTab == 0) {

                if (
                    upcomingMatches
                        .isEmpty()
                ) {

                    Box(
                        modifier =
                            Modifier.fillMaxSize(),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(
                            text =
                                "No matches to score",

                            color =
                                Color.Gray
                        )
                    }

                } else {

                    LazyColumn(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(16.dp),

                        verticalArrangement =
                            Arrangement.spacedBy(
                                14.dp
                            )
                    ) {

                        items(
                            upcomingMatches
                        ) { match ->

                            UpcomingMatchCard(
                                challenge =
                                    match,

                                onPostScore = {

                                    selectedMatch =
                                        match
                                }
                            )
                        }
                    }
                }

            } else {

                // ==========================
                // RESULTS TAB
                // ==========================
                if (
                    results.isEmpty()
                ) {

                    Box(
                        modifier =
                            Modifier.fillMaxSize(),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(
                            text =
                                "No results yet",

                            color =
                                Color.Gray
                        )
                    }

                } else {

                    LazyColumn(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(16.dp),

                        verticalArrangement =
                            Arrangement.spacedBy(
                                14.dp
                            )
                    ) {

                        items(results) {
                                result ->

                            ResultCard(
                                result =
                                    result
                            )
                        }
                    }
                }
            }
        }
    }

    // ==========================
    // POST SCORE DIALOG
    // ==========================
    selectedMatch?.let {
            match ->

        AlertDialog(

            onDismissRequest = {

                selectedMatch =
                    null
            },

            title = {
                Text(
                    "Post Match Result"
                )
            },

            text = {

                Column {

                    Text(
                        text =
                            match.hostTeamName,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Text("VS")

                    Text(
                        text =
                            match
                                .acceptedTeamName
                                ?: "",

                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                16.dp
                            )
                    )

                    OutlinedTextField(
                        value =
                            hostScore,

                        onValueChange = {
                            hostScore =
                                it
                        },

                        label = {

                            Text(
                                "${match.hostTeamName} Score"
                            )
                        },

                        modifier =
                            Modifier.fillMaxWidth()
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                12.dp
                            )
                    )

                    OutlinedTextField(
                        value =
                            opponentScore,

                        onValueChange = {
                            opponentScore =
                                it
                        },

                        label = {

                            Text(
                                "${match.acceptedTeamName} Score"
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

                        println("POST BUTTON CLICKED")

                        val host =
                            hostScore.toIntOrNull()

                        val opponent =
                            opponentScore.toIntOrNull()

                        if (
                            host == null ||
                            opponent == null
                        ) {
                            println("INVALID SCORE")
                            return@Button
                        }

                        println("CALLING VIEWMODEL")

                        viewModel.postScore(
                            challenge = match,
                            hostScore = host,
                            opponentScore = opponent
                        )

                        println("RESETTING UI")

                        hostScore = ""
                        opponentScore = ""

                        selectedMatch = null
                    }
                ) {

                    Text("Post")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        selectedMatch =
                            null
                    }
                ) {

                    Text(
                        "Cancel"
                    )
                }
            }
        )
    }
}

@Composable
fun UpcomingMatchCard(
    challenge: Challenge,
    onPostScore: () -> Unit
) {

    val currentUid =
        com.google.firebase.auth
            .FirebaseAuth
            .getInstance()
            .currentUser
            ?.uid ?: ""

    var isHostAdmin by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(
        challenge.hostTeamId
    ) {

        try {

            val snapshot =
                com.google.firebase.firestore
                    .FirebaseFirestore
                    .getInstance()
                    .collection(
                        "teams"
                    )
                    .document(
                        challenge.hostTeamId
                    )
                    .get()
                    .await()

            val members =
                snapshot.get(
                    "members"
                ) as?
                        List<Map<String, Any>>
                    ?: emptyList()

            isHostAdmin =
                members.any {

                    it["uid"] ==
                            currentUid &&
                            it["role"] ==
                            "admin"
                }

        } catch (
            e: Exception
        ) {

            e.printStackTrace()
        }
    }

    ElevatedCard(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(
                22.dp
            )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    18.dp
                )
        ) {

            Text(
                text =
                    challenge.sport,

                color =
                    Color(
                        0xFF2563EB
                    ),

                style =
                    MaterialTheme
                        .typography
                        .titleMedium
            )

            Spacer(
                modifier =
                    Modifier.height(
                        10.dp
                    )
            )

            Text(
                text =
                    challenge.hostTeamName,

                fontWeight =
                    FontWeight.Bold
            )

            Text("VS")

            Text(
                text =
                    challenge
                        .acceptedTeamName
                        ?: "",

                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(
                        10.dp
                    )
            )

            Text(
                text =
                    "📍 ${challenge.groundName}"
            )

            Text(
                text =
                    "📅 ${challenge.date}"
            )

            Text(
                text =
                    "🕒 ${challenge.startTime} - ${challenge.endTime}"
            )

            Spacer(
                modifier =
                    Modifier.height(
                        14.dp
                    )
            )

            if (isHostAdmin) {

                Button(
                    onClick =
                        onPostScore,

                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Text(
                        "Post Result"
                    )
                }

            } else {

                Surface(
                    color =
                        Color(
                            0xFFE5E7EB
                        ),

                    shape =
                        RoundedCornerShape(
                            12.dp
                        ),

                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Text(
                        text =
                            "Only Host Admin Can Post",

                        modifier =
                            Modifier.padding(
                                horizontal = 14.dp,
                                vertical = 12.dp
                            ),

                        color =
                            Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun ResultCard(
    result: MatchResult
) {

    ElevatedCard(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(
                22.dp
            )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    18.dp
                )
        ) {

            Text(
                text =
                    "🏆 ${result.winnerTeamName} Won",

                color =
                    Color(
                        0xFF15803D
                    ),

                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Text(
                    text =
                        result.hostTeamName
                )

                Text(
                    text =
                        result.hostScore
                            .toString(),

                    fontWeight =
                        FontWeight.Bold
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        8.dp
                    )
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Text(
                    text =
                        result.opponentTeamName
                )

                Text(
                    text =
                        result.opponentScore
                            .toString(),

                    fontWeight =
                        FontWeight.Bold
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )

            Text(
                text =
                    "📍 ${result.groundName}"
            )

            Text(
                text =
                    "📅 ${result.date}"
            )
        }
    }
}