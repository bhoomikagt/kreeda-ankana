package com.bhoomika.kreedaankana

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*

import com.bhoomika.kreedaankana.data.local.db.AppDatabase
import com.bhoomika.kreedaankana.repository.BookingRepository
import com.bhoomika.kreedaankana.ui.auth.LoginScreen
import com.bhoomika.kreedaankana.ui.auth.SignUpScreen
import com.bhoomika.kreedaankana.ui.booking.BookingScreen
import com.bhoomika.kreedaankana.ui.dashboard.DashboardScreen
import com.bhoomika.kreedaankana.ui.team.CreateTeamScreen
import com.bhoomika.kreedaankana.ui.teamInvites.inviteScreen
import com.bhoomika.kreedaankana.ui.teamInvites.CreateInviteScreen
import com.bhoomika.kreedaankana.ui.teamInvites.ReplyScreen
import com.bhoomika.kreedaankana.ui.calendar.GroundCalendarScreen
import com.bhoomika.kreedaankana.ui.profile.ProfileScreen

import com.bhoomika.kreedaankana.viewmodel.*
import com.google.firebase.firestore.FirebaseFirestore
import com.bhoomika.kreedaankana.ui.challenge.ChallengeBoardScreen
import java.time.LocalDate
import java.time.LocalTime
import com.bhoomika.kreedaankana.ui.score.ScoreWallScreen
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppNavigation()
        }
    }
}

@Composable
fun AppNavigation() {

    val navController = rememberNavController()
    val authVM: AuthViewModel = viewModel()

    val startDestination = if (authVM.isLoggedIn()) "dashboard" else "login"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // 🔐 LOGIN
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToSignup = {
                    navController.navigate("signup")
                }
            )
        }

        // 🔐 SIGNUP
        composable("signup") {
            SignUpScreen(
                onSignupSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("signup") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // 🏠 DASHBOARD
        composable("dashboard") {

            val context = LocalContext.current
            val db = AppDatabase.getDatabase(context)

            val bookingVM: BookingViewModel = viewModel(
                factory = BookingViewModelFactory(
                    BookingRepository(
                        db.bookingDao(),
                        FirebaseFirestore.getInstance()
                    )
                )
            )

            DashboardScreen(
                bookingVM = bookingVM,
                onNavigateToBooking = { navController.navigate("booking") },
                onNavigateToCreateTeam = { navController.navigate("create_team") },
                onNavigateToProfile = { navController.navigate("profile") },
                onNavigateToInvites = { navController.navigate("invites") },
                onNavigateToCalendar = {navController.navigate("calendar")},
                onNavigateToChallengeBoard = {navController.navigate("challenge_board")},
                onNavigateToScoreWall = {
                    navController.navigate(
                        "score_wall"
                    )
                }
            )
        }

        // 📅 BOOKING (NORMAL ENTRY)
        composable("booking") {

            val context = LocalContext.current
            val db = AppDatabase.getDatabase(context)

            val repo = BookingRepository(
                db.bookingDao(),
                FirebaseFirestore.getInstance()
            )

            val bookingVM: BookingViewModel = viewModel(
                factory = BookingViewModelFactory(repo)
            )

            val teamVM: TeamViewModel = viewModel()
            val groundVM: GroundViewModel = viewModel()

            BookingScreen(
                viewModel = bookingVM,
                teamVM = teamVM,
                groundVM = groundVM,
                onBack = { navController.popBackStack() }
            )
        }

        // 📅 BOOKING (FROM CALENDAR WITH PRESELECTION)
        composable(
            route = "booking/{groundId}/{date}/{time}"
        ) { backStackEntry ->

            val context = LocalContext.current
            val db = AppDatabase.getDatabase(context)

            val repo = BookingRepository(
                db.bookingDao(),
                FirebaseFirestore.getInstance()
            )

            val bookingVM: BookingViewModel = viewModel(
                factory = BookingViewModelFactory(repo)
            )

            val teamVM: TeamViewModel = viewModel()
            val groundVM: GroundViewModel = viewModel()

            val groundId = backStackEntry.arguments?.getString("groundId")
            val dateStr = backStackEntry.arguments?.getString("date")
            val timeStr = backStackEntry.arguments?.getString("time")

            val date = dateStr?.let { LocalDate.parse(it) }
            val time = timeStr?.let { LocalTime.parse(it) }

            BookingScreen(
                viewModel = bookingVM,
                teamVM = teamVM,
                groundVM = groundVM,
                onBack = { navController.popBackStack() },

                // ✅ PRESELECTED VALUES
                preselectedGroundId = groundId,
                preselectedDate = date,
                preselectedTime = time
            )
        }

        // 👥 CREATE TEAM
        composable("create_team") {
            CreateTeamScreen(
                onDone = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        // 👤 PROFILE
        composable("profile") {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            )
        }

        // 📨 INVITES
        composable("invites") {

            val inviteVM: InviteTeamViewModel = viewModel()

            inviteScreen(
                viewModel = inviteVM,
                onBack = { navController.popBackStack() },
                onCreateClick = {
                    navController.navigate("create_invite")
                },
                onRespondClick = { param ->
                    val parts = param.split("|")

                    val inviteId = parts.getOrNull(0) ?: return@inviteScreen
                    val role = parts.getOrNull(1) ?: return@inviteScreen

                    navController.navigate("reply/$inviteId/$role")
                }
            )
        }

        // ➕ CREATE INVITE
        composable("create_invite") {

            val context = LocalContext.current
            val db = AppDatabase.getDatabase(context)

            val bookingVM: BookingViewModel = viewModel(
                factory = BookingViewModelFactory(
                    BookingRepository(
                        db.bookingDao(),
                        FirebaseFirestore.getInstance()
                    )
                )
            )

            val inviteVM: InviteTeamViewModel = viewModel()
            val teamVM: TeamViewModel = viewModel()

            CreateInviteScreen(
                inviteVM = inviteVM,
                teamVM = teamVM,
                bookingVM = bookingVM,
                onBack = { navController.popBackStack() },
                onCreated = { navController.popBackStack() }
            )
        }

        // 💬 REPLY
        composable("reply/{inviteId}/{role}") { backStackEntry ->

            val inviteId = backStackEntry.arguments?.getString("inviteId") ?: return@composable
            val role = backStackEntry.arguments?.getString("role") ?: return@composable

            val inviteVM: InviteTeamViewModel = viewModel()

            ReplyScreen(
                inviteId = inviteId,
                role = role,
                inviteVM = inviteVM,
                onBack = { navController.popBackStack() }
            )
        }

        // 📆 CALENDAR
        composable("calendar") {

            val context = LocalContext.current
            val db = AppDatabase.getDatabase(context)

            val repo = BookingRepository(
                db.bookingDao(),
                FirebaseFirestore.getInstance()
            )

            val calendarVM: CalendarViewModel = viewModel(
                factory = CalendarViewModelFactory(repo)
            )

            GroundCalendarScreen(
                vm = calendarVM,
                onBack = { navController.popBackStack() },

                // ✅ FIXED NAVIGATION
                onSlotClick = { groundId, date, time ->
                    navController.navigate("booking/$groundId/$date/$time")
                }
            )
        }

        composable("challenge_board") {

            val challengeVM:
                    ChallengeViewModel =
                viewModel()

            ChallengeBoardScreen(
                viewModel = challengeVM,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("score_wall") {

            ScoreWallScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }}

}