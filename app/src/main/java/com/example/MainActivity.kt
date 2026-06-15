package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.StadiumGrass
import com.example.ui.theme.CardSurfaceDark
import com.example.ui.theme.MutedText
import com.example.ui.theme.RealWhite
import com.example.ui.theme.PureWhite
import com.example.ui.theme.EmeraldTertiary
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape

class MainActivity : ComponentActivity() {

    private val viewModel: CricketViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = false, dynamicColor = false) {
                var selectedBottomTab by remember { mutableStateOf(0) }
                var selectedArenaSubTab by remember { mutableStateOf(0) }

                Scaffold(
                    modifier = Modifier.fillMaxSize().testTag("app_scaffold"),
                    topBar = {
                        Surface(
                            shadowElevation = 1.dp,
                            color = CardSurfaceDark,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .statusBarsPadding()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Elite Letter Logotype Graphic Box
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                                    colors = listOf(StadiumGrass, EmeraldTertiary)
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "TK",
                                            color = RealWhite,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            style = androidx.compose.ui.text.TextStyle(
                                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                            )
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = "TK SCORE",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 18.sp,
                                            color = PureWhite,
                                            lineHeight = 18.sp
                                        )
                                        Text(
                                            text = "LOCAL SCORING HUB",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            color = StadiumGrass,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = { /* Status action */ },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(DarkBackground)
                                    ) {
                                        Text("📊", fontSize = 16.sp)
                                    }
                                    Box(contentAlignment = Alignment.TopEnd) {
                                        IconButton(
                                            onClick = { /* Notifications */ },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(DarkBackground)
                                        ) {
                                            Text("🔔", fontSize = 16.sp)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFEF4444))
                                                .offset(x = (-2).dp, y = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = CardSurfaceDark,
                            contentColor = StadiumGrass,
                            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars).testTag("bottom_nav")
                        ) {
                            NavigationBarItem(
                                selected = selectedBottomTab == 0,
                                onClick = { selectedBottomTab = 0 },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                                label = { Text("Dashboard", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = DarkBackground,
                                    selectedTextColor = StadiumGrass,
                                    unselectedIconColor = MutedText,
                                    unselectedTextColor = MutedText,
                                    indicatorColor = StadiumGrass
                                ),
                                modifier = Modifier.testTag("nav_dashboard")
                            )

                            NavigationBarItem(
                                selected = selectedBottomTab == 1,
                                onClick = { selectedBottomTab = 1 },
                                icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Scoring") },
                                label = { Text("Scoring", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = DarkBackground,
                                    selectedTextColor = StadiumGrass,
                                    unselectedIconColor = MutedText,
                                    unselectedTextColor = MutedText,
                                    indicatorColor = StadiumGrass
                                ),
                                modifier = Modifier.testTag("nav_scoring")
                            )

                            NavigationBarItem(
                                selected = selectedBottomTab == 2,
                                onClick = { selectedBottomTab = 2 },
                                icon = { Icon(Icons.Default.List, contentDescription = "Roster") },
                                label = { Text("Rosters", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = DarkBackground,
                                    selectedTextColor = StadiumGrass,
                                    unselectedIconColor = MutedText,
                                    unselectedTextColor = MutedText,
                                    indicatorColor = StadiumGrass
                                ),
                                modifier = Modifier.testTag("nav_rosters")
                            )

                            NavigationBarItem(
                                selected = selectedBottomTab == 3,
                                onClick = { selectedBottomTab = 3 },
                                icon = { Icon(Icons.Default.Star, contentDescription = "Arena") },
                                label = { Text("Sports Arena", fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = DarkBackground,
                                    selectedTextColor = StadiumGrass,
                                    unselectedIconColor = MutedText,
                                    unselectedTextColor = MutedText,
                                    indicatorColor = StadiumGrass
                                ),
                                modifier = Modifier.testTag("nav_arena")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DarkBackground)
                            .padding(innerPadding)
                    ) {
                        when (selectedBottomTab) {
                            0 -> DashboardScreen(
                                viewModel = viewModel,
                                onNavigateScoring = { selectedBottomTab = 1 }
                            )
                            1 -> ScoringScreen(
                                viewModel = viewModel
                            )
                            2 -> TeamsPlayersScreen(
                                viewModel = viewModel
                            )
                            3 -> {
                                // Arena Screen carrying secondary horizontal tabs
                                Column(modifier = Modifier.fillMaxSize()) {
                                    TabRow(
                                        selectedTabIndex = selectedArenaSubTab,
                                        containerColor = CardSurfaceDark,
                                        contentColor = StadiumGrass,
                                        modifier = Modifier.fillMaxWidth().testTag("arena_tabrow")
                                    ) {
                                        Tab(
                                            selected = selectedArenaSubTab == 0,
                                            onClick = { selectedArenaSubTab = 0 }
                                        ) {
                                            Text(
                                                "AUCTION",
                                                modifier = Modifier.padding(10.dp),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (selectedArenaSubTab == 0) StadiumGrass else MutedText
                                            )
                                        }
                                        Tab(
                                            selected = selectedArenaSubTab == 1,
                                            onClick = { selectedArenaSubTab = 1 }
                                        ) {
                                            Text(
                                                "DRAFT",
                                                modifier = Modifier.padding(10.dp),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (selectedArenaSubTab == 1) StadiumGrass else MutedText
                                            )
                                        }
                                        Tab(
                                            selected = selectedArenaSubTab == 2,
                                            onClick = { selectedArenaSubTab = 2 }
                                        ) {
                                            Text(
                                                "AI CHAT",
                                                modifier = Modifier.padding(10.dp),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (selectedArenaSubTab == 2) StadiumGrass else MutedText
                                            )
                                        }
                                    }

                                    Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                                        when (selectedArenaSubTab) {
                                            0 -> AuctionScreen(viewModel = viewModel)
                                            1 -> DraftScreen(viewModel = viewModel)
                                            2 -> AiAssistantScreen(viewModel = viewModel)
                                            else -> AuctionScreen(viewModel = viewModel)
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
}
