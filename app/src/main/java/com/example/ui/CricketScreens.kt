package com.example.ui

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

// --- Beautiful fallback canvas logo drawer ---
@Composable
fun CustomSportsLogo(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(EmeraldSecondary, EmeraldTertiary)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Subtle design marks
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = GoldAccent.copy(alpha = 0.15f),
                radius = size.minDimension / 2.3f,
                style = Stroke(width = 2.dp.toPx())
            )
            // Cricket seam line
            drawLine(
                color = RealWhite.copy(alpha = 0.3f),
                start = Offset(0f, size.height / 2f),
                end = Offset(size.width, size.height / 2f),
                strokeWidth = 3.dp.toPx()
            )
        }
        val letters = if (title.length >= 2) title.take(2).uppercase() else title.uppercase()
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = letters,
                color = RealWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle.take(4).uppercase(),
                    color = GoldAccent,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// --- SCREEN 1: Dashboard Home (Leaderboards, Recent Matches, Formats, and Live Feeds) ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: CricketViewModel,
    onNavigateScoring: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tournamentsState by viewModel.tournaments.collectAsStateWithLifecycle()
    val teamsState by viewModel.teams.collectAsStateWithLifecycle()
    val playersState by viewModel.players.collectAsStateWithLifecycle()
    // 1. Tournament Form State
    var tournamentName by remember { mutableStateOf("") }
    var tournamentFormat by remember { mutableStateOf("T20") }

    // 2. Team Form State
    var teamName by remember { mutableStateOf("") }
    var teamSelectedTournament by remember { mutableStateOf<Tournament?>(null) }
    var teamTournamentDropdownExpanded by remember { mutableStateOf(false) }

    // 3. Player Form State
    var playerInputName by remember { mutableStateOf("") }
    var playerInputRole by remember { mutableStateOf("Batsman") }
    var playerInputBasePrice by remember { mutableStateOf("100000") }

    // 4. Match Form State
    var matchTeam1 by remember { mutableStateOf<Team?>(null) }
    var matchTeam2 by remember { mutableStateOf<Team?>(null) }
    var matchOversInput by remember { mutableStateOf("5") }
    var matchFormatSelected by remember { mutableStateOf("T20") }
    var team1DropdownExpanded by remember { mutableStateOf(false) }
    var team2DropdownExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("dashboard_column"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Welcome Hero Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                border = BorderStroke(1.dp, OutlinedContainerBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "WELCOME TO TK SCORE",
                        color = StadiumGrass,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your Clean Slate Cricket Management Console",
                        color = RealWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Design custom players, configure teams and tournaments, and launch live cricket matches with precise offline scoring trackers.",
                        color = MutedText,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = OutlinedContainerBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Dynamic Stats Badge Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Tournaments Stat Badge
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkBackground),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🏆", fontSize = 18.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${tournamentsState.size} Tourneys", color = RealWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Teams Stat Badge
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkBackground),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("👥", fontSize = 18.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${teamsState.size} Teams", color = RealWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Players Stat Badge
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkBackground),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🏏", fontSize = 18.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${playersState.size} Players", color = RealWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // --- OPTION CARD 1: Create a New Tournament ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                border = BorderStroke(1.dp, OutlinedContainerBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🏆", fontSize = 20.sp)
                        Text("CREATE NEW TOURNAMENT", color = GoldAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedTextField(
                        value = tournamentName,
                        onValueChange = { tournamentName = it },
                        label = { Text("Tournament Title") },
                        placeholder = { Text("e.g. World Cup T20") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = RealWhite,
                            unfocusedTextColor = RealWhite,
                            focusedBorderColor = StadiumGrass,
                            unfocusedBorderColor = OutlinedContainerBorder,
                            focusedLabelColor = StadiumGrass,
                            unfocusedLabelColor = MutedText
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column {
                        Text("Tournament Format", color = MutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("T20", "ODI", "Test").forEach { fmt ->
                                val active = tournamentFormat == fmt
                                FilledTonalButton(
                                    onClick = { tournamentFormat = fmt },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = if (active) StadiumGrass else OutlinedContainerBorder,
                                        contentColor = if (active) DarkBackground else RealWhite
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(fmt, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (tournamentName.isNotBlank()) {
                                viewModel.createTournament(tournamentName.trim(), tournamentFormat)
                                Toast.makeText(context, "Tournament '$tournamentName' registered!", Toast.LENGTH_SHORT).show()
                                tournamentName = ""
                            } else {
                                Toast.makeText(context, "Please enter a tournament name", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StadiumGrass, contentColor = DarkBackground),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("SAVE TOURNAMENT", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
        // --- OPTION CARD 2: Create a New Team ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                border = BorderStroke(1.dp, OutlinedContainerBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("👥", fontSize = 20.sp)
                        Text("CREATE NEW TEAM", color = StadiumGrass, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedTextField(
                        value = teamName,
                        onValueChange = { teamName = it },
                        label = { Text("Team Name / Club Title") },
                        placeholder = { Text("e.g. Lahore Kings") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = RealWhite,
                            unfocusedTextColor = RealWhite,
                            focusedBorderColor = StadiumGrass,
                            unfocusedBorderColor = OutlinedContainerBorder,
                            focusedLabelColor = StadiumGrass,
                            unfocusedLabelColor = MutedText
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Select Tournament Dropdown
                    Column {
                        Text("Assign to Tournament", color = MutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { teamTournamentDropdownExpanded = true },
                                colors = ButtonDefaults.buttonColors(containerColor = OutlinedContainerBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = teamSelectedTournament?.name ?: "Standalone / No Tournament",
                                    color = RealWhite,
                                    fontSize = 13.sp
                                )
                            }
                            DropdownMenu(
                                expanded = teamTournamentDropdownExpanded,
                                onDismissRequest = { teamTournamentDropdownExpanded = false },
                                modifier = Modifier.background(CardSurfaceDark)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Standalone / No Tournament", color = RealWhite) },
                                    onClick = {
                                        teamSelectedTournament = null
                                        teamTournamentDropdownExpanded = false
                                    }
                                )
                                tournamentsState.forEach { t ->
                                    DropdownMenuItem(
                                        text = { Text(t.name, color = RealWhite) },
                                        onClick = {
                                            teamSelectedTournament = t
                                            teamTournamentDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (teamName.isNotBlank()) {
                                viewModel.createTeam(teamName.trim(), teamSelectedTournament?.id)
                                Toast.makeText(context, "Team '$teamName' registered successfully!", Toast.LENGTH_SHORT).show()
                                teamName = ""
                            } else {
                                Toast.makeText(context, "Please enter a team name", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StadiumGrass, contentColor = DarkBackground),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("SAVE NEW TEAM", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

        // --- OPTION CARD 3: Create a New Player ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                border = BorderStroke(1.dp, OutlinedContainerBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("🏏", fontSize = 20.sp)
                        Text("CREATE NEW PLAYER", color = RealWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedTextField(
                        value = playerInputName,
                        onValueChange = { playerInputName = it },
                        label = { Text("Athlete Full Name") },
                        placeholder = { Text("e.g. John Doe") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = RealWhite,
                            unfocusedTextColor = RealWhite,
                            focusedBorderColor = StadiumGrass,
                            unfocusedBorderColor = OutlinedContainerBorder,
                            focusedLabelColor = StadiumGrass,
                            unfocusedLabelColor = MutedText
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column {
                        Text("Athlete Specialty Role", color = MutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        val roles = listOf("Batsman", "Bowler", "All-Rounder", "Wicketkeeper")
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            roles.forEach { r ->
                                val active = playerInputRole == r
                                FilledTonalButton(
                                    onClick = { playerInputRole = r },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = if (active) StadiumGrass else OutlinedContainerBorder,
                                        contentColor = if (active) DarkBackground else RealWhite
                                    ),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                ) {
                                    Text(r, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = playerInputBasePrice,
                        onValueChange = { playerInputBasePrice = it },
                        label = { Text("Base Price (₹ / Credits)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = RealWhite,
                            unfocusedTextColor = RealWhite,
                            focusedBorderColor = StadiumGrass,
                            unfocusedBorderColor = OutlinedContainerBorder,
                            focusedLabelColor = StadiumGrass,
                            unfocusedLabelColor = MutedText
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (playerInputName.isNotBlank()) {
                                val price = playerInputBasePrice.toDoubleOrNull() ?: 100000.0
                                viewModel.createPlayer(playerInputName.trim(), playerInputRole, price)
                                Toast.makeText(context, "Athlete '$playerInputName' registered!", Toast.LENGTH_SHORT).show()
                                playerInputName = ""
                            } else {
                                Toast.makeText(context, "Please enter an athlete name", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StadiumGrass, contentColor = DarkBackground),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("SAVE NEW PLAYER", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

        // --- OPTION CARD 4: Quick Start Match ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                border = BorderStroke(1.dp, OutlinedContainerBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("⚡", fontSize = 20.sp)
                        Text("QUICK START MATCH", color = ScoreOrange, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = "Initialize an active local game between your custom made teams.",
                        color = MutedText,
                        fontSize = 11.sp
                    )

                    // Choose Team A
                    Column {
                        Text("Batting / Bowling First (Team A)", color = MutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { team1DropdownExpanded = true },
                                colors = ButtonDefaults.buttonColors(containerColor = OutlinedContainerBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(matchTeam1?.name ?: "Select Team A", color = RealWhite)
                            }
                            DropdownMenu(
                                expanded = team1DropdownExpanded,
                                onDismissRequest = { team1DropdownExpanded = false },
                                modifier = Modifier.background(CardSurfaceDark)
                            ) {
                                if (teamsState.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("No custom teams created yet", color = MutedText) },
                                        onClick = { team1DropdownExpanded = false }
                                    )
                                } else {
                                    teamsState.forEach { t ->
                                        DropdownMenuItem(
                                            text = { Text(t.name, color = RealWhite) },
                                            onClick = {
                                                matchTeam1 = t
                                                team1DropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Choose Team B
                    Column {
                        Text("Batting / Bowling Second (Team B)", color = MutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { team2DropdownExpanded = true },
                                colors = ButtonDefaults.buttonColors(containerColor = OutlinedContainerBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(matchTeam2?.name ?: "Select Team B", color = RealWhite)
                            }
                            DropdownMenu(
                                expanded = team2DropdownExpanded,
                                onDismissRequest = { team2DropdownExpanded = false },
                                modifier = Modifier.background(CardSurfaceDark)
                            ) {
                                if (teamsState.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("No custom teams created yet", color = MutedText) },
                                        onClick = { team2DropdownExpanded = false }
                                    )
                                } else {
                                    teamsState.forEach { t ->
                                        if (t.id != matchTeam1?.id) {
                                            DropdownMenuItem(
                                                text = { Text(t.name, color = RealWhite) },
                                                onClick = {
                                                    matchTeam2 = t
                                                    team2DropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Overs limit
                    OutlinedTextField(
                        value = matchOversInput,
                        onValueChange = { matchOversInput = it },
                        label = { Text("Match Overs Limit (e.g. 5, 20)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = RealWhite,
                            unfocusedTextColor = RealWhite,
                            focusedBorderColor = StadiumGrass,
                            unfocusedBorderColor = OutlinedContainerBorder,
                            focusedLabelColor = StadiumGrass,
                            unfocusedLabelColor = MutedText
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Format Limit
                    Column {
                        Text("Match Game Format", color = MutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("T20", "ODI", "Test").forEach { fmt ->
                                val active = matchFormatSelected == fmt
                                FilledTonalButton(
                                    onClick = { matchFormatSelected = fmt },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = if (active) StadiumGrass else OutlinedContainerBorder,
                                        contentColor = if (active) DarkBackground else RealWhite
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(fmt, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val t1 = matchTeam1
                            val t2 = matchTeam2
                            if (t1 != null && t2 != null && t1.id != t2.id) {
                                val overs = matchOversInput.toIntOrNull() ?: 5
                                viewModel.createMatch(
                                    tournamentId = null,
                                    team1 = t1,
                                    team2 = t2,
                                    overs = overs,
                                    format = matchFormatSelected
                                )
                                Toast.makeText(context, "Match Launched! Navigating to scorer...", Toast.LENGTH_LONG).show()
                                onNavigateScoring()
                            } else {
                                Toast.makeText(context, "Please select two unique teams first!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ScoreOrange, contentColor = RealWhite),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("START LIVE ROUND SCORING", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }






    }
}

// --- SCREEN 2: Live Advanced Cricket Scoring Canvas ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScoringScreen(
    viewModel: CricketViewModel,
    modifier: Modifier = Modifier
) {
    val currMatch by viewModel.currentMatch.collectAsStateWithLifecycle()
    val ballsState by viewModel.matchBalls.collectAsStateWithLifecycle()
    val playersState by viewModel.players.collectAsStateWithLifecycle()
    val teamsState by viewModel.teams.collectAsStateWithLifecycle()

    val strikerOnState by viewModel.strikerId.collectAsStateWithLifecycle()
    val nonStrikerOnState by viewModel.nonStrikerId.collectAsStateWithLifecycle()
    val bowlerOnState by viewModel.bowlerId.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // State placeholders for match initialization
    var setupMode by remember { mutableStateOf(currMatch == null) }
    var inputOversMax by remember { mutableStateOf("5") }
    var inputFormat by remember { mutableStateOf("T20") }
    var selectedTeam1 by remember { mutableStateOf<Team?>(null) }
    var selectedTeam2 by remember { mutableStateOf<Team?>(null) }

    var extraTypeSelected by remember { mutableStateOf<String?>(null) }
    var wicketTypeSelected by remember { mutableStateOf<String?>(null) }

    if (setupMode || currMatch == null) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "INITIATE MATCH CONSOLE",
                    color = GoldAccent,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                    border = BorderStroke(1.dp, OutlinedContainerBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Select Competing Rosters", color = PureWhite, fontWeight = FontWeight.Bold)

                        // Selector Team 1
                        Column {
                            Text("Team A (Batting/Bowling First)", color = MutedText, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            var t1Exp by remember { mutableStateOf(false) }
                            Box {
                                Button(
                                    onClick = { t1Exp = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = OutlinedContainerBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(selectedTeam1?.name ?: "Select Team A")
                                }
                                DropdownMenu(expanded = t1Exp, onDismissRequest = { t1Exp = false }) {
                                    teamsState.forEach { t ->
                                        DropdownMenuItem(
                                            text = { Text(t.name) },
                                            onClick = {
                                                selectedTeam1 = t
                                                t1Exp = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Selector Team 2
                        Column {
                            Text("Team B (Batting/Bowling Second)", color = MutedText, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            var t2Exp by remember { mutableStateOf(false) }
                            Box {
                                Button(
                                    onClick = { t2Exp = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = OutlinedContainerBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(selectedTeam2?.name ?: "Select Team B")
                                }
                                DropdownMenu(expanded = t2Exp, onDismissRequest = { t2Exp = false }) {
                                    teamsState.forEach { t ->
                                        DropdownMenuItem(
                                            text = { Text(t.name) },
                                            onClick = {
                                                selectedTeam2 = t
                                                t2Exp = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Overs Limit
                        OutlinedTextField(
                            value = inputOversMax,
                            onValueChange = { inputOversMax = it },
                            label = { Text("Match Overs Limit (e.g. 5, 20)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = PureWhite,
                                unfocusedTextColor = PureWhite,
                                focusedBorderColor = StadiumGrass,
                                unfocusedBorderColor = OutlinedContainerBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Format Limit
                        Column {
                            Text("Cricket Format", color = MutedText, fontSize = 11.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("T20", "ODI", "Test").forEach { fmt ->
                                    val active = inputFormat == fmt
                                    FilledTonalButton(
                                        onClick = { inputFormat = fmt },
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = if (active) StadiumGrass else OutlinedContainerBorder,
                                            contentColor = if (active) DarkBackground else PureWhite
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(fmt)
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                val t1 = selectedTeam1
                                val t2 = selectedTeam2
                                if (t1 != null && t2 != null && t1.id != t2.id) {
                                    viewModel.createMatch(
                                        tournamentId = null,
                                        team1 = t1,
                                        team2 = t2,
                                        overs = inputOversMax.toIntOrNull() ?: 5,
                                        format = inputFormat
                                    )
                                    setupMode = false
                                } else {
                                    Toast.makeText(context, "Please select two unique teams", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StadiumGrass, contentColor = DarkBackground),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("FLIP COIN & BEGIN MATCH", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    } else {
        val m = currMatch!!
        // Filter players belonging to teams
        val team1Players = playersState.filter { it.currentTeamId == m.team1Id }
        val team2Players = playersState.filter { it.currentTeamId == m.team2Id }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("scoring_panel"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Innings Header
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                    border = BorderStroke(1.dp, OutlinedContainerBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "LIVE MATCH : OVER ${if (m.currentInnings == 1) "1st" else "2nd"} INNINGS",
                            color = StadiumGrass,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(horizontalAlignment = Alignment.Start) {
                                Text(text = m.team1Name.take(16), color = PureWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text(text = "${m.team1Runs}/${m.team1Wickets} in ${m.team1Overs} overs", color = MutedText, fontSize = 12.sp)
                            }
                            Text(text = "VS", color = GoldAccent, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = m.team2Name.take(16), color = PureWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text(text = if (m.team2Overs > 0.0 || m.currentInnings == 2) "${m.team2Runs}/${m.team2Wickets} in ${m.team2Overs} overs" else "Yet to Bat", color = MutedText, fontSize = 12.sp)
                            }
                        }

                        if (m.status == "Completed") {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "RESULT: ${m.winMargin}",
                                color = GoldAccent,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                        } else if (m.currentInnings == 2) {
                            Spacer(modifier = Modifier.height(12.dp))
                            val needed = (m.team1Runs + 1) - m.team2Runs
                            Text(
                                "NEED $needed RUNS TO WIN NOW",
                                color = ScoreOrange,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (m.status != "Completed") {
                // Selector Row for Batter 1, Batter 2, Bowler
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                        border = BorderStroke(1.dp, OutlinedContainerBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Current Players on Pitch", color = PureWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                            val battingPool = if (m.currentInnings == 1) team1Players else team2Players
                            val bowlingPool = if (m.currentInnings == 1) team2Players else team1Players

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                // Striker select
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Striker", color = MutedText, fontSize = 11.sp)
                                    var strExp by remember { mutableStateOf(false) }
                                    Box {
                                        Button(
                                            onClick = { strExp = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = OutlinedContainerBorder),
                                            contentPadding = PaddingValues(horizontal = 4.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            val currentStriker = battingPool.firstOrNull { it.id == strikerOnState }
                                            Text(currentStriker?.name?.take(10) ?: "Select", fontSize = 11.sp, maxLines = 1)
                                        }
                                        DropdownMenu(expanded = strExp, onDismissRequest = { strExp = false }) {
                                            battingPool.forEach { p ->
                                                DropdownMenuItem(
                                                    text = { Text(p.name, fontSize = 12.sp) },
                                                    onClick = {
                                                        viewModel.selectStriker(p.id)
                                                        strExp = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                // Non Striker select
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Non-Striker", color = MutedText, fontSize = 11.sp)
                                    var nstrExp by remember { mutableStateOf(false) }
                                    Box {
                                        Button(
                                            onClick = { nstrExp = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = OutlinedContainerBorder),
                                            contentPadding = PaddingValues(horizontal = 4.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            val currentNonS = battingPool.firstOrNull { it.id == nonStrikerOnState }
                                            Text(currentNonS?.name?.take(10) ?: "Select", fontSize = 11.sp, maxLines = 1)
                                        }
                                        DropdownMenu(expanded = nstrExp, onDismissRequest = { nstrExp = false }) {
                                            battingPool.forEach { p ->
                                                DropdownMenuItem(
                                                    text = { Text(p.name, fontSize = 12.sp) },
                                                    onClick = {
                                                        viewModel.selectNonStriker(p.id)
                                                        nstrExp = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                // Bowler select
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Active Bowler", color = MutedText, fontSize = 11.sp)
                                    var bowlExp by remember { mutableStateOf(false) }
                                    Box {
                                        Button(
                                            onClick = { bowlExp = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = OutlinedContainerBorder),
                                            contentPadding = PaddingValues(horizontal = 4.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            val currentB = bowlingPool.firstOrNull { it.id == bowlerOnState }
                                            Text(currentB?.name?.take(10) ?: "Select", fontSize = 11.sp, maxLines = 1)
                                        }
                                        DropdownMenu(expanded = bowlExp, onDismissRequest = { bowlExp = false }) {
                                            bowlingPool.forEach { p ->
                                                DropdownMenuItem(
                                                    text = { Text(p.name, fontSize = 12.sp) },
                                                    onClick = {
                                                        viewModel.selectBowler(p.id)
                                                        bowlExp = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Scoring Controls pad
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                        border = BorderStroke(1.dp, OutlinedContainerBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Ball Scorer", color = GoldAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                            // Runs grid
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                listOf(0, 1, 2, 3, 4, 6).forEach { score ->
                                    FilledTonalButton(
                                        onClick = {
                                            viewModel.scoreBall(
                                                runs = score,
                                                isExtra = extraTypeSelected != null,
                                                extraType = extraTypeSelected,
                                                isWicket = wicketTypeSelected != null,
                                                wicketType = wicketTypeSelected
                                            )
                                            // Reset
                                            extraTypeSelected = null
                                            wicketTypeSelected = null
                                        },
                                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = OutlinedContainerBorder, contentColor = PureWhite),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("btn_score_$score")
                                    ) {
                                        Text("$score", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }
                            }

                            // Extras selection Row
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                listOf("Wide", "No-Ball", "Bye", "Leg-Bye").forEach { ext ->
                                    val active = extraTypeSelected == ext
                                    OutlinedButton(
                                        onClick = {
                                            extraTypeSelected = if (active) null else ext
                                        },
                                        border = BorderStroke(1.dp, if (active) StadiumGrass else OutlinedContainerBorder),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = if (active) StadiumGrass else MutedText
                                        ),
                                        contentPadding = PaddingValues(horizontal = 4.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(ext, fontSize = 11.sp)
                                    }
                                }
                            }

                            // Wickets trigger Row
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                listOf("Bowled", "Caught", "LBW", "Run Out").forEach { wkt ->
                                    val active = wicketTypeSelected == wkt
                                    OutlinedButton(
                                        onClick = {
                                            wicketTypeSelected = if (active) null else wkt
                                        },
                                        border = BorderStroke(1.dp, if (active) WicketRed else OutlinedContainerBorder),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = if (active) WicketRed else MutedText
                                        ),
                                        contentPadding = PaddingValues(horizontal = 4.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(wkt, fontSize = 11.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Balls / Commentary Feed
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("BALL BY BALL ACTION FEED", color = PureWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Button(
                        onClick = { viewModel.exportMatchPdf(context, m) },
                        colors = ButtonDefaults.buttonColors(containerColor = StadiumGrass, contentColor = DarkBackground)
                    ) {
                        Text("Export Match PDF", fontSize = 11.sp)
                    }
                }
            }

            if (ballsState.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                        border = BorderStroke(1.dp, OutlinedContainerBorder)
                    ) {
                        Text(
                            text = "No ball logs registered for this match yet. Select pitch batters and bowlers to begin.",
                            color = MutedText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        )
                    }
                }
            } else {
                items(ballsState.reversed().take(20)) { ball ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                        border = BorderStroke(1.dp, if (ball.isWicket) WicketRed else OutlinedContainerBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (ball.isWicket) WicketRed else StadiumGrass.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${ball.overNumber}.${ball.ballNumber}",
                                    color = if (ball.isWicket) RealWhite else StadiumGrass,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(modifier = Modifier.weight(1.0f)) {
                                Text(text = ball.commentary, color = PureWhite, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Batsman: ${ball.batsmanName}", color = GoldAccent, fontSize = 10.sp)
                                    Text("Bowler: ${ball.bowlerName}", color = MutedText, fontSize = 10.sp)
                                }
                            }
                            Text(
                                text = "${ball.runs}",
                                color = if (ball.isWicket) WicketRed else StadiumGrass,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- SCREEN 3: Teams & Players Roster editor ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TeamsPlayersScreen(
    viewModel: CricketViewModel,
    modifier: Modifier = Modifier
) {
    val teamsState by viewModel.teams.collectAsStateWithLifecycle()
    val playersState by viewModel.players.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) }

    // Forms
    var teamName by remember { mutableStateOf("") }

    var playerName by remember { mutableStateOf("") }
    var playerRole by remember { mutableStateOf("Batsman") }
    var basePriceInput by remember { mutableStateOf("100000") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("teams_players_column"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Toggle tabs
        item {
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = CardSurfaceDark,
                contentColor = StadiumGrass
            ) {
                Tab(selected = activeTab == 0, onClick = { activeTab = 0 }) {
                    Text("TEAMS", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, color = if (activeTab == 0) StadiumGrass else MutedText)
                }
                Tab(selected = activeTab == 1, onClick = { activeTab = 1 }) {
                    Text("PLAYERS", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, color = if (activeTab == 1) StadiumGrass else MutedText)
                }
            }
        }

        if (activeTab == 0) {
            // Team Register Form
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                    border = BorderStroke(1.dp, OutlinedContainerBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Register New Team", color = GoldAccent, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = teamName,
                            onValueChange = { teamName = it },
                            label = { Text("Team Name") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = PureWhite,
                                unfocusedTextColor = PureWhite,
                                focusedBorderColor = StadiumGrass,
                                unfocusedBorderColor = OutlinedContainerBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                if (teamName.isNotBlank()) {
                                    viewModel.createTeam(teamName, null)
                                    teamName = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StadiumGrass, contentColor = DarkBackground),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("SAVE TEAM", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Teams list
            item {
                Text("Registered Teams", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            if (teamsState.isEmpty()) {
                item {
                    Text("No teams configured.", color = MutedText, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            } else {
                items(teamsState) { team ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                        border = BorderStroke(1.dp, OutlinedContainerBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CustomSportsLogo(title = team.name, subtitle = "LM", modifier = Modifier.size(42.dp))
                            Column(modifier = Modifier.weight(1.0f)) {
                                Text(team.name, color = PureWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text("Budget Allocated: ${viewModel.currencySymbol.value} ${team.budgetTotal}", color = MutedText, fontSize = 11.sp)
                                Text("Remaining Budget: ${viewModel.currencySymbol.value} ${team.budgetRemaining}", color = StadiumGrass, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

        } else {
            // Player Register Form
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                    border = BorderStroke(1.dp, OutlinedContainerBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Register Custom Athlete", color = GoldAccent, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = playerName,
                            onValueChange = { playerName = it },
                            label = { Text("Athlete Full Name") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = PureWhite,
                                unfocusedTextColor = PureWhite,
                                focusedBorderColor = StadiumGrass,
                                unfocusedBorderColor = OutlinedContainerBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Role Selector
                        Column {
                            Text("Role", color = MutedText, fontSize = 12.sp)
                            val roles = listOf("Batsman", "Bowler", "All-Rounder", "Wicketkeeper")
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                roles.forEach { role ->
                                    val active = playerRole == role
                                    FilledTonalButton(
                                        onClick = { playerRole = role },
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = if (active) StadiumGrass else OutlinedContainerBorder,
                                            contentColor = if (active) DarkBackground else PureWhite
                                        ),
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    ) {
                                        Text(role)
                                    }
                                }
                            }
                        }

                        // Base budget Price
                        OutlinedTextField(
                            value = basePriceInput,
                            onValueChange = { basePriceInput = it },
                            label = { Text("Base Price (${viewModel.currencySymbol.value})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = PureWhite,
                                unfocusedTextColor = PureWhite,
                                focusedBorderColor = StadiumGrass,
                                unfocusedBorderColor = OutlinedContainerBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                if (playerName.isNotBlank()) {
                                    val price = basePriceInput.toDoubleOrNull() ?: 100000.0
                                    viewModel.createPlayer(playerName, playerRole, price)
                                    playerName = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StadiumGrass, contentColor = DarkBackground),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("SAVE ATHLETE", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Players Roster list
            item {
                Text("Athlete Performance Cards", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            if (playersState.isEmpty()) {
                item {
                    Text("No players enrolled.", color = MutedText, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            } else {
                items(playersState) { p ->
                    var isBioExpanded by remember { mutableStateOf(false) }
                    var playerBioText by remember { mutableStateOf("Generating biography logs...") }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                        border = BorderStroke(1.dp, OutlinedContainerBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                CustomSportsLogo(title = p.name, subtitle = p.role.take(2), modifier = Modifier.size(38.dp))
                                Column(modifier = Modifier.weight(1.0f)) {
                                    Text(p.name, color = PureWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(p.role, color = MutedText, fontSize = 11.sp)
                                        Text("Status: ${p.status}", color = if (p.status == "Sold") StadiumGrass else ScoreOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(StadiumGrass.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Text("${p.percentPerformance.toInt()}% Perf", color = StadiumGrass, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Stats Grid
                            Divider(color = OutlinedContainerBorder)
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Column {
                                    Text("Runs", color = MutedText, fontSize = 9.sp)
                                    Text("${p.totalRuns}", color = PureWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("Fours/Sixes", color = MutedText, fontSize = 9.sp)
                                    Text("${p.fours}/${p.sixes}", color = PureWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("Wickets", color = MutedText, fontSize = 9.sp)
                                    Text("${p.totalWickets}", color = PureWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("SR / Econ", color = MutedText, fontSize = 9.sp)
                                    val sr = if (p.totalBalls > 0) String.format("%.1f", (p.totalRuns.toDouble()/p.totalBalls)*100) else "0.0"
                                    val econ = if (p.ballsBowled > 0) String.format("%.2f", p.runsConceded.toDouble()/(p.ballsBowled.toDouble()/6.0)) else "0.0"
                                    Text("$sr / $econ", color = PureWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("50s/100s", color = MutedText, fontSize = 9.sp)
                                    Text("${p.fifties}/${p.centuries}", color = PureWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = {
                                    isBioExpanded = !isBioExpanded
                                    if (isBioExpanded) {
                                        viewModel.generateAiPlayerBio(p) { generatedBio ->
                                            playerBioText = generatedBio
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = OutlinedContainerBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (isBioExpanded) "Hide AI Biography" else "View AI Biography Generator", fontSize = 11.sp, color = GoldAccent)
                            }

                            if (isBioExpanded) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(OutlinedContainerBorder)
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = playerBioText,
                                        color = PureWhite,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 16.sp,
                                        style = MaterialTheme.typography.bodyMedium
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

// --- SCREEN 4: Dynamic Live Cricket Auction ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AuctionScreen(
    viewModel: CricketViewModel,
    modifier: Modifier = Modifier
) {
    val playersState by viewModel.players.collectAsStateWithLifecycle()
    val teamsState by viewModel.teams.collectAsStateWithLifecycle()
    val isRunning by viewModel.isAuctionRunning.collectAsStateWithLifecycle()
    val currentP by viewModel.currentAuctionPlayer.collectAsStateWithLifecycle()
    val logs by viewModel.auctionMessages.collectAsStateWithLifecycle()
    val biddingPrice by viewModel.currentHighestBid.collectAsStateWithLifecycle()
    val activeCurrency by viewModel.currencySymbol.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("auction_column"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("LIVE SPORTS AUCTION MODULE", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Enlist registered players on the bidding table with custom currencies.", color = MutedText, fontSize = 12.sp)
        }

        // Currency Setting Config
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                border = BorderStroke(1.dp, OutlinedContainerBorder)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Choose Auction Currency:", color = PureWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("₹", "$", "£", "AED").forEach { cur ->
                            val active = activeCurrency == cur
                            FilledTonalButton(
                                onClick = { viewModel.currencySymbol.value = cur },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (active) StadiumGrass else OutlinedContainerBorder,
                                    contentColor = if (active) DarkBackground else PureWhite
                                ),
                                contentPadding = PaddingValues(horizontal = 4.dp),
                                modifier = Modifier.size(48.dp, 36.dp)
                            ) {
                                Text(cur, fontWeight = FontWeight.Black, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        if (!isRunning || currentP == null) {
            // Select Player to Start auction
            item {
                Text("Select registered player to trigger auction", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            val unsoldList = playersState.filter { it.status == "Registered" }
            if (unsoldList.isEmpty()) {
                item {
                    Text("No unsold players with 'Registered' status found. Custom add them on TEAMS tab first.", color = MutedText)
                }
            } else {
                items(unsoldList) { p ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                        border = BorderStroke(1.dp, OutlinedContainerBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CustomSportsLogo(title = p.name, subtitle = p.role.take(2), modifier = Modifier.size(36.dp))
                                Column {
                                    Text(p.name, color = PureWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("Perf. Index: ${p.percentPerformance.toInt()}% | ${p.role}", color = MutedText, fontSize = 11.sp)
                                }
                            }
                            Button(
                                onClick = { viewModel.registerPlayerForAuction(p) },
                                colors = ButtonDefaults.buttonColors(containerColor = StadiumGrass, contentColor = DarkBackground)
                            ) {
                                Text("BID ${activeCurrency} ${p.basePrice}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            val athlete = currentP!!
            // Auction in progressive state
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                    border = BorderStroke(1.dp, StadiumGrass)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("ACTIVE BIDDING RING", color = StadiumGrass, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CustomSportsLogo(title = athlete.name, subtitle = athlete.role.take(2), modifier = Modifier.size(48.dp))
                            Column {
                                Text(text = athlete.name, color = PureWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text(text = "${athlete.role} | Base Price: $activeCurrency ${athlete.basePrice}", color = MutedText, fontSize = 12.sp)
                            }
                        }
                        Divider(color = OutlinedContainerBorder)
                        Text(text = "CURRENT HIGHEST BID STATUS", color = MutedText, fontSize = 11.sp)
                        Text(text = "$activeCurrency $biddingPrice", color = GoldAccent, fontSize = 32.sp, fontWeight = FontWeight.Black)

                        // Placing bids button triggers for teams
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            teamsState.forEach { t ->
                                Button(
                                    onClick = {
                                        viewModel.placeAuctionBid(t.id, biddingPrice + (athlete.basePrice * 0.1).coerceAtLeast(50000.0))
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = OutlinedContainerBorder),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                ) {
                                    Text("+10% ${t.name.take(8)}", fontSize = 10.sp, color = PureWhite)
                                }
                            }
                        }

                        Button(
                            onClick = { viewModel.completeAuction() },
                            colors = ButtonDefaults.buttonColors(containerColor = StadiumGrass, contentColor = DarkBackground),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("HAMMER DOWN - ASSIGN ATHLETE NOW", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Auctioneer live logs log chat channel
            item {
                Text("AUCTION CHAT STREAM", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            items(logs.reversed()) { log ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                    border = BorderStroke(1.dp, OutlinedContainerBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("[${log.sender}]:", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(log.content, color = PureWhite, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// --- SCREEN 5: Master Cricket Draft Module ---
@Composable
fun DraftScreen(
    viewModel: CricketViewModel,
    modifier: Modifier = Modifier
) {
    val playersState by viewModel.players.collectAsStateWithLifecycle()
    val teamsState by viewModel.teams.collectAsStateWithLifecycle()
    val currentTeamIdx by viewModel.currentDraftTeamIndex.collectAsStateWithLifecycle()
    val roundNumber by viewModel.draftRound.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("draft_column"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("MASTER CRICKET DRAFT BOARD", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Select strategic athletes based on detailed computer analytics scores.", color = MutedText, fontSize = 12.sp)
        }

        val choosingTeam = teamsState.getOrNull(currentTeamIdx)
        if (choosingTeam != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                    border = BorderStroke(1.dp, GoldAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("ACTIVE TEAM DRAFTING TURN", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                        Text(choosingTeam.name, color = PureWhite, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Text("Draft Round $roundNumber Pick Progression", color = MutedText, fontSize = 12.sp)
                    }
                }
            }
        }

        // Draft Selection pool
        item {
            Text("Draftable Athlete Pool Ordered by performance analytics", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        val draftableList = playersState.filter { it.status == "Registered" }.sortedByDescending { it.percentPerformance }
        if (draftableList.isEmpty()) {
            item {
                Text("No draftable candidates available. Generate more players or complete matches to update player performance indexes.", color = MutedText)
            }
        } else {
            items(draftableList) { p ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                    border = BorderStroke(1.dp, OutlinedContainerBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CustomSportsLogo(title = p.name, subtitle = p.role.take(2), modifier = Modifier.size(36.dp))
                            Column {
                                Text(p.name, color = PureWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Perf Index: ${p.percentPerformance.toInt()}% | ${p.role}", color = MutedText, fontSize = 11.sp)
                            }
                        }

                        Button(
                            onClick = {
                                if (choosingTeam != null) {
                                    viewModel.selectDraftPlayer(choosingTeam, p)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StadiumGrass, contentColor = DarkBackground)
                        ) {
                            Text("DRAFT PICK", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// --- SCREEN 6: AI Chatbot, custom logo prompt creator, biography generators ---
@Composable
fun AiAssistantScreen(
    viewModel: CricketViewModel,
    modifier: Modifier = Modifier
) {
    val history by viewModel.chatbotHistory.collectAsStateWithLifecycle()
    val isLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()

    var userQuery by remember { mutableStateOf("") }
    var generatorLogoConcept by remember { mutableStateOf("") }
    var generatedLogoPromptResult by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("ai_assistant_column"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("AI CHATBOT, CREATOR & GENERATORS PANEL", color = StadiumGrass, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Design graphics concept codes and probe tactics with our Gemini Analyst.", color = MutedText, fontSize = 12.sp)
        }

        // Section A: AI LOGO CREATOR TOOL
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                border = BorderStroke(1.dp, OutlinedContainerBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Instant Sport Logo Prompt Generator", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Input your team mascot/concept, and we will formulate professional graphic prompts.", color = MutedText, fontSize = 11.sp)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = generatorLogoConcept,
                            onValueChange = { generatorLogoConcept = it },
                            placeholder = { Text("e.g. Blazing Tigers, Golden Lions") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = PureWhite,
                                unfocusedTextColor = PureWhite,
                                focusedBorderColor = StadiumGrass,
                                unfocusedBorderColor = OutlinedContainerBorder
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                if (generatorLogoConcept.isNotBlank()) {
                                    viewModel.generateAiLogoPrompt(generatorLogoConcept) { text ->
                                        generatedLogoPromptResult = text
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StadiumGrass, contentColor = DarkBackground)
                        ) {
                            Text("BUILD CODE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (generatedLogoPromptResult.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(OutlinedContainerBorder)
                                .padding(10.dp)
                        ) {
                            Text(generatedLogoPromptResult, color = PureWhite, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        // Section B: Live AI Chatbot History & Prompt
        item {
            Text("Chat with Gemini Tactical Analyst", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        if (history.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                    border = BorderStroke(1.dp, OutlinedContainerBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "No discussions logged. Ask anything about match setup, draft statistics, player matchups, or local budget limits to initiate.",
                        color = MutedText,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            items(history) { msg ->
                val alignment = if (msg.isAi) Alignment.Start else Alignment.End
                val color = if (msg.isAi) StadiumGrass.copy(alpha = 0.15f) else OutlinedContainerBorder
                Column(horizontalAlignment = alignment, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(color)
                            .padding(12.dp)
                            .widthIn(max = 280.dp)
                    ) {
                        Column {
                            Text(text = if (msg.isAi) "Gemini Analyst" else "User", color = GoldAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = msg.content, color = PureWhite, fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }

        if (isLoading) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(color = StadiumGrass, modifier = Modifier.size(16.dp))
                    Text("AI is compiling sports tactical reviews...", color = StadiumGrass, fontSize = 11.sp)
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = userQuery,
                    onValueChange = { userQuery = it },
                    placeholder = { Text("Ask Gemini about batsman rotations...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite,
                        focusedBorderColor = StadiumGrass,
                        unfocusedBorderColor = OutlinedContainerBorder
                    ),
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        if (userQuery.isNotBlank()) {
                            viewModel.askGemini(userQuery)
                            userQuery = ""
                        }
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .background(StadiumGrass, CircleShape)
                        .testTag("btn_send_bot")
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = DarkBackground)
                }
            }
        }
    }
}

// --- SCREEN 7: Simulated Offline Cloud Sync configs ---
@Composable
fun SyncScreen(
    viewModel: CricketViewModel,
    modifier: Modifier = Modifier
) {
    val isAutoSync by viewModel.autoSyncEnabled.collectAsStateWithLifecycle()
    val isConnected by viewModel.syncConnected.collectAsStateWithLifecycle()
    val latency by viewModel.syncLatencyMs.collectAsStateWithLifecycle()
    val dbDigestSize by viewModel.dbCheckSumSize.collectAsStateWithLifecycle()

    var mockLogs by remember { mutableStateOf(listOf("Sync session loaded successfully.", "No local database conflicts detected.")) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("sync_column"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("CLOUD SYNC CONFIGURATION PANEL", color = StadiumGrass, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Simulate multi-device live scorekeeping and resolution controls.", color = MutedText, fontSize = 12.sp)
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                border = BorderStroke(1.dp, OutlinedContainerBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Server Sync Status", color = PureWhite, fontWeight = FontWeight.Bold)

                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Multi-Device Scorekeeping Status", color = MutedText, fontSize = 13.sp)
                        Switch(
                            checked = isConnected,
                            onCheckedChange = {
                                viewModel.syncConnected.value = it
                                val text = if (it) "Connection is active." else "Switched to Local Offline Storage mode."
                                mockLogs = mockLogs + text
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = StadiumGrass)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Automated Push Syncing", color = MutedText, fontSize = 13.sp)
                        Switch(
                            checked = isAutoSync,
                            onCheckedChange = {
                                viewModel.autoSyncEnabled.value = it
                                val text = if (it) "Background push enabled." else "Manual Syncing mode enforced."
                                mockLogs = mockLogs + text
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = StadiumGrass)
                        )
                    }

                    Divider(color = OutlinedContainerBorder)

                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Database Digested Size", color = MutedText, fontSize = 12.sp)
                        Text(dbDigestSize, color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Simulated API Ping Latency", color = MutedText, fontSize = 12.sp)
                        Text("${latency} ms", color = StadiumGrass, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            mockLogs = mockLogs + "Manual Force Push completed. 0 database items updated, all in sync."
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StadiumGrass, contentColor = DarkBackground),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("FORCE PUSH DATABASE TO CENTRAL CLOUD", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text("Session Sync Log Streams", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        items(mockLogs.reversed()) { log ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                border = BorderStroke(1.dp, OutlinedContainerBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(StadiumGrass).align(Alignment.CenterVertically))
                    Text(log, color = PureWhite, fontSize = 11.sp)
                }
            }
        }
    }
}
