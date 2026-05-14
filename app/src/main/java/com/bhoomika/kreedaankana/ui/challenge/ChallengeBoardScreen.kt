package com.bhoomika.kreedaankana.ui.challenge

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
import com.bhoomika.kreedaankana.data.model.ChallengeReply
import com.bhoomika.kreedaankana.data.model.Team
import com.bhoomika.kreedaankana.viewmodel.ChallengeViewModel
import com.bhoomika.kreedaankana.viewmodel.TeamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeBoardScreen(
    viewModel: ChallengeViewModel,
    onBack: () -> Unit
) {

    var selectedTab by remember {
        mutableIntStateOf(0)
    }

    // View Replies dialog
    var showRepliesDialog by remember {
        mutableStateOf(false)
    }

    val openChallenges by
    viewModel.challenges.collectAsState()

    val myChallenges by
    viewModel.myChallenges.collectAsState()

    val teamVM: TeamViewModel =
        viewModel()

    val teams by
    teamVM.teams.collectAsState()

    var replies by remember {
        mutableStateOf<List<ChallengeReply>>(
            emptyList()
        )
    }

    val acceptedChallenges by
    viewModel.acceptedChallenges
        .collectAsState()

    var selectedChallenge by remember {
        mutableStateOf<Challenge?>(null)
    }

    // ==========================
// RESULT DIALOG STATE
// ==========================
    var showResultDialog by remember {
        mutableStateOf(false)
    }

    var resultChallenge by remember {
        mutableStateOf<Challenge?>(null)
    }

    var scoreA by remember {
        mutableStateOf("")
    }

    var scoreB by remember {
        mutableStateOf("")
    }

    var summary by remember {
        mutableStateOf("")
    }

    // Reply Dialog State
    var showReplyDialog by remember {
        mutableStateOf(false)
    }

    var selectedTeam by remember {
        mutableStateOf<Team?>(null)
    }

    var replyMessage by remember {
        mutableStateOf("")
    }

    var teamExpanded by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        viewModel.loadAllChallenges()
        teamVM.loadTeams()
    }

    Scaffold(

        topBar = {

            CenterAlignedTopAppBar(

                title = {
                    Text("Challenge Board")
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBack
                    ) {
                        Icon(
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
                        Text("Open")
                    }
                )

                Tab(
                    selected =
                        selectedTab == 1,

                    onClick = {
                        selectedTab = 1
                    },

                    text = {
                        Text("My Posted")
                    }
                )

                Tab(
                    selected =
                        selectedTab == 2,

                    onClick = {
                        selectedTab = 2
                    },

                    text = {
                        Text("Accepted")
                    }
                )
            }

            val list =
                when (selectedTab) {

                    0 -> openChallenges

                    1 -> myChallenges

                    else ->
                        acceptedChallenges
                }

            if (list.isEmpty()) {

                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment =
                        Alignment.Center
                ) {

                    Text(
                        text =
                            if (selectedTab == 0)
                                "No open challenges"
                            else
                                "No challenges created yet",

                        color = Color.Gray
                    )
                }

            } else {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),

                    verticalArrangement =
                        Arrangement.spacedBy(
                            14.dp
                        )
                ) {

                    items(
                        list,
                        key = {
                            it.id
                        }
                    ) { challenge ->

                        ChallengeCard(
                            challenge =
                                challenge,

                            isMine =
                                selectedTab == 1,

                            onRepliesClick = {

                                selectedChallenge =
                                    challenge

                                showRepliesDialog =
                                    true

                                viewModel
                                    .loadReplies(
                                        challenge.id
                                    ) {
                                        replies =
                                            it
                                    }
                            },

                            onReplyClick = {

                                selectedChallenge =
                                    challenge

                                showRepliesDialog =
                                    false

                                showReplyDialog =
                                    true
                            },

                            onPostResult = {

                                resultChallenge =
                                    challenge

                                showResultDialog =
                                    true
                            }
                        )
                    }
                }
            }
        }

        // ==========================
        // REPLIES DIALOG
        // ==========================
        selectedChallenge?.let { challenge ->

            if (showRepliesDialog) {

                AlertDialog(

                    onDismissRequest = {

                        showRepliesDialog = false
                        selectedChallenge = null
                    },

                    title = {
                        Text("Replies")
                    },

                    text = {

                        if (
                            replies.isEmpty()
                        ) {

                            Text(
                                "No replies yet"
                            )

                        } else {

                            Column(
                                verticalArrangement =
                                    Arrangement.spacedBy(
                                        10.dp
                                    )
                            ) {

                                replies.forEach {
                                        reply ->

                                    ElevatedCard {

                                        Row(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(
                                                        12.dp
                                                    ),

                                            horizontalArrangement =
                                                Arrangement.SpaceBetween
                                        ) {

                                            Column(
                                                modifier =
                                                    Modifier.weight(
                                                        1f
                                                    )
                                            ) {

                                                Text(
                                                    reply.teamName,

                                                    fontWeight =
                                                        FontWeight.Bold
                                                )

                                                Spacer(
                                                    Modifier.height(
                                                        4.dp
                                                    )
                                                )

                                                Text(
                                                    reply.message
                                                )
                                            }

                                            if (
                                                challenge.status
                                                != "ACCEPTED"
                                            ) {

                                                Button(
                                                    onClick = {

                                                        viewModel
                                                            .acceptReply(
                                                                challenge.id,
                                                                reply
                                                            )

                                                        selectedChallenge =
                                                            null
                                                    }
                                                ) {
                                                    Text(
                                                        "Accept"
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },

                    confirmButton = {

                        TextButton(
                            onClick = {

                                showRepliesDialog =
                                    false

                                selectedChallenge =
                                    null
                            }
                        ) {
                            Text("Close")
                        }
                    }
                )
            }
        }

        // ==========================
        // REPLY DIALOG
        // ==========================
        if (
            showReplyDialog &&
            selectedChallenge != null
        ) {

            AlertDialog(

                onDismissRequest = {

                    showReplyDialog =
                        false

                    selectedTeam =
                        null

                    replyMessage =
                        ""
                },

                title = {
                    Text("Reply to Challenge")
                },

                text = {

                    Column {

                        ExposedDropdownMenuBox(
                            expanded =
                                teamExpanded,

                            onExpandedChange = {
                                teamExpanded =
                                    !teamExpanded
                            }
                        ) {

                            OutlinedTextField(
                                value =
                                    selectedTeam?.teamName
                                        ?: "",

                                onValueChange = {},

                                readOnly = true,

                                label = {
                                    Text(
                                        "Select Team"
                                    )
                                },

                                modifier =
                                    Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                            )

                            ExposedDropdownMenu(

                                expanded =
                                    teamExpanded,

                                onDismissRequest = {
                                    teamExpanded =
                                        false
                                }
                            ) {

                                teams
                                    .filter {

                                        // hide challenge owner team
                                        it.id !=
                                                selectedChallenge
                                                    ?.hostTeamId
                                    }
                                    .forEach { team ->

                                        DropdownMenuItem(

                                            text = {
                                                Text(
                                                    team.teamName
                                                )
                                            },

                                            onClick = {

                                                selectedTeam =
                                                    team

                                                teamExpanded =
                                                    false
                                            }
                                        )
                                    }
                            }
                        }

                        Spacer(
                            Modifier.height(
                                12.dp
                            )
                        )

                        OutlinedTextField(

                            value =
                                replyMessage,

                            onValueChange = {
                                replyMessage =
                                    it
                            },

                            label = {
                                Text("Message")
                            },

                            modifier =
                                Modifier.fillMaxWidth()
                        )
                    }
                },

                confirmButton = {

                    Button(
                        onClick = {

                            val challenge =
                                selectedChallenge
                                    ?: return@Button

                            val team =
                                selectedTeam
                                    ?: return@Button

                            viewModel
                                .replyToChallenge(
                                    challenge = challenge,
                                    team = team,
                                    message = replyMessage
                                )

                            viewModel.loadReplies(
                                challenge.id
                            ) {
                                replies = it
                            }

                            viewModel.loadAllChallenges()

                            showReplyDialog = false
                            showRepliesDialog = false

                            replyMessage =
                                ""
                        }
                    ) {
                        Text("Send")
                    }
                },

                dismissButton = {

                    TextButton(
                        onClick = {

                            showReplyDialog =
                                false
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
    // ==========================
// POST RESULT DIALOG
// ==========================
    if (
        showResultDialog &&
        resultChallenge != null
    ) {

        AlertDialog(

            onDismissRequest = {

                showResultDialog =
                    false

                resultChallenge =
                    null

                scoreA = ""
                scoreB = ""
                summary = ""
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
                            resultChallenge
                                ?.hostTeamName
                                ?: "",

                        fontWeight =
                            FontWeight.Bold
                    )

                    Text("VS")

                    Text(
                        text =
                            resultChallenge
                                ?.acceptedTeamName
                                ?: "",

                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        Modifier.height(
                            16.dp
                        )
                    )

                    OutlinedTextField(

                        value =
                            scoreA,

                        onValueChange = {
                            scoreA = it
                        },

                        label = {

                            Text(
                                "${resultChallenge?.hostTeamName} Score"
                            )
                        },

                        modifier =
                            Modifier.fillMaxWidth()
                    )

                    Spacer(
                        Modifier.height(
                            12.dp
                        )
                    )

                    OutlinedTextField(

                        value =
                            scoreB,

                        onValueChange = {
                            scoreB = it
                        },

                        label = {

                            Text(
                                "${resultChallenge?.acceptedTeamName} Score"
                            )
                        },

                        modifier =
                            Modifier.fillMaxWidth()
                    )

                    Spacer(
                        Modifier.height(
                            12.dp
                        )
                    )

                    OutlinedTextField(

                        value =
                            summary,

                        onValueChange = {
                            summary = it
                        },

                        label = {
                            Text(
                                "Match Summary"
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

                        val challenge =
                            resultChallenge
                                ?: return@Button

                        viewModel
                            .postResult(
                                challenge =
                                    challenge,

                                scoreA =
                                    scoreA
                                        .toIntOrNull()
                                        ?: 0,

                                scoreB =
                                    scoreB
                                        .toIntOrNull()
                                        ?: 0,

                                summary =
                                    summary
                            )

                        showResultDialog =
                            false

                        resultChallenge =
                            null

                        scoreA = ""
                        scoreB = ""
                        summary = ""
                    }
                ) {

                    Text(
                        "Post Result"
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {

                        showResultDialog =
                            false
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
fun ChallengeCard(
    challenge: Challenge,
    isMine: Boolean,
    onRepliesClick: () -> Unit,
    onReplyClick: () -> Unit,
    onPostResult: () -> Unit
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
            modifier = Modifier
                .padding(18.dp)
        ) {

            Text(
                challenge.sport,

                style =
                    MaterialTheme
                        .typography
                        .titleMedium,

                color =
                    Color(0xFF2563EB)
            )

            Spacer(
                Modifier.height(6.dp)
            )

            Text(
                challenge.hostTeamName,

                style =
                    MaterialTheme
                        .typography
                        .titleLarge,

                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                Modifier.height(8.dp)
            )

            Text(
                challenge.description
            )

            Spacer(
                Modifier.height(12.dp)
            )

            Text(
                "📍 ${challenge.groundName}"
            )

            Text(
                "📅 ${challenge.date}"
            )

            Text(
                "🕒 ${challenge.startTime} - ${challenge.endTime}"
            )

            Spacer(
                Modifier.height(16.dp)
            )

            // ==========================
            // MY POSTED CHALLENGES
            // ==========================
            if (isMine) {

                when (
                    challenge.status
                ) {

                    "OPEN" -> {

                        Button(
                            onClick =
                                onRepliesClick
                        ) {
                            Text(
                                "View Replies"
                            )
                        }
                    }

                    "ACCEPTED" -> {

                        Column {

                            Surface(
                                color =
                                    Color(
                                        0xFFD1FAE5
                                    ),

                                shape =
                                    RoundedCornerShape(
                                        14.dp
                                    )
                            ) {

                                Text(
                                    text =
                                        "Accepted: ${challenge.acceptedTeamName}",

                                    modifier =
                                        Modifier.padding(
                                            12.dp
                                        ),

                                    color =
                                        Color(
                                            0xFF047857
                                        )
                                )
                            }

                            Spacer(
                                modifier =
                                    Modifier.height(
                                        10.dp
                                    )
                            )
                        }
                    }

                    "COMPLETED" -> {

                        Surface(
                            color =
                                Color(
                                    0xFFDBEAFE
                                ),

                            shape =
                                RoundedCornerShape(
                                    14.dp
                                )
                        ) {

                            Text(
                                text =
                                    "✓ Result Posted",

                                modifier =
                                    Modifier.padding(
                                        12.dp
                                    ),

                                color =
                                    Color(
                                        0xFF1D4ED8
                                    )
                            )
                        }
                    }
                }

            } else {

                when (
                    challenge.status
                ) {

                    "OPEN" -> {

                        Button(
                            onClick =
                                onReplyClick
                        ) {
                            Text(
                                "Reply"
                            )
                        }
                    }

                    "ACCEPTED" -> {

                        Surface(
                            color =
                                Color(
                                    0xFFD1FAE5
                                ),

                            shape =
                                RoundedCornerShape(
                                    14.dp
                                )
                        ) {

                            Text(
                                text =
                                    "✓ Match Confirmed",

                                modifier =
                                    Modifier.padding(
                                        12.dp
                                    ),

                                color =
                                    Color(
                                        0xFF047857
                                    )
                            )
                        }
                    }

                    "COMPLETED" -> {

                        Surface(
                            color =
                                Color(
                                    0xFFDBEAFE
                                ),

                            shape =
                                RoundedCornerShape(
                                    14.dp
                                )
                        ) {

                            Text(
                                text =
                                    "✓ Match Finished",

                                modifier =
                                    Modifier.padding(
                                        12.dp
                                    ),

                                color =
                                    Color(
                                        0xFF1D4ED8
                                    )
                            )
                        }
                    }
                }
            }


        }
    }
}