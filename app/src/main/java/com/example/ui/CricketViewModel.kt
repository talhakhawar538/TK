package com.example.ui

import android.app.Application
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.concurrent.TimeUnit

// --- Direct OkHttp-Powered Gemini API Dispatcher ---
suspend fun executeGeminiPost(apiKey: String, prompt: String, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
    val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val mediaType = "application/json; charset=utf-8".toMediaType()
    
    val escapedPrompt = prompt
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
    
    val jsonString = if (systemInstruction != null) {
        val escapedSys = systemInstruction
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
        "{\"contents\":[{\"parts\":[{\"text\":\"$escapedPrompt\"}]}],\"systemInstruction\":{\"parts\":[{\"text\":\"$escapedSys\"}]}}"
    } else {
        "{\"contents\":[{\"parts\":[{\"text\":\"$escapedPrompt\"}]}]}"
    }

    val body = jsonString.toRequestBody(mediaType)
    val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
    val request = Request.Builder()
        .url(url)
        .post(body)
        .header("Content-Type", "application/json")
        .build()

    val response = client.newCall(request).execute()
    val bodyString = response.body?.string() ?: ""
    if (!response.isSuccessful) {
        throw Exception("Gemini API Error: Status Code ${response.code}\n$bodyString")
    }

    try {
        val root = org.json.JSONObject(bodyString)
        val candidates = root.getJSONArray("candidates")
        val candidate = candidates.getJSONObject(0)
        val content = candidate.getJSONObject("content")
        val parts = content.getJSONArray("parts")
        val part = parts.getJSONObject(0)
        part.getString("text")
    } catch (e: Exception) {
        Log.e("GeminiParse", "Failed to parse API Response: $bodyString", e)
        throw Exception("API parsing error: response formatting was incorrect or returned null.")
    }
}

class CricketViewModel(application: Application) : AndroidViewModel(application) {

    private val database = CricketDatabase.getDatabase(application)
    private val repository = CricketRepository(database.cricketDao())

    // --- State Streams ---
    val tournaments: StateFlow<List<Tournament>> = repository.allTournaments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val teams: StateFlow<List<Team>> = repository.allTeamsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val players: StateFlow<List<Player>> = repository.allPlayersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val matches: StateFlow<List<Match>> = repository.allMatchesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auctionBids: StateFlow<List<AuctionBid>> = repository.allAuctionBidsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Dynamic UI Selection States ---
    var selectedTournamentId = MutableStateFlow<Int?>(null)
    var selectedMatchId = MutableStateFlow<Int?>(null)

    // --- Ball-by-ball logic helpers ---
    private val _currentMatch = MutableStateFlow<Match?>(null)
    val currentMatch: StateFlow<Match?> = _currentMatch.asStateFlow()

    private val _matchBalls = MutableStateFlow<List<BallByBall>>(emptyList())
    val matchBalls: StateFlow<List<BallByBall>> = _matchBalls.asStateFlow()

    // Active bat/bowl player IDs
    var strikerId = MutableStateFlow<Int?>(null)
    var nonStrikerId = MutableStateFlow<Int?>(null)
    var bowlerId = MutableStateFlow<Int?>(null)

    // --- UI Filters ---
    val filterFormat = MutableStateFlow<String>("All") // "All" | "T20" | "ODI" | "Test"

    // --- Auction State ---
    val currencySymbol = MutableStateFlow<String>("₹") // Customized currency symbol
    val currentAuctionPlayer = MutableStateFlow<Player?>(null)
    val auctionMessages = MutableStateFlow<List<Message>>(emptyList())
    val isAuctionRunning = MutableStateFlow<Boolean>(false)
    val currentHighestBid = MutableStateFlow<Double>(0.0)
    val currentHighestBidderId = MutableStateFlow<Int?>(null)

    // --- Live Draft Arena ---
    val draftRound = MutableStateFlow<Int>(1)
    val currentDraftTeamIndex = MutableStateFlow<Int>(0)
    val draftLog = MutableStateFlow<List<Message>>(emptyList())
    val draftSelections = MutableStateFlow<List<Player>>(emptyList())

    // --- AI Chatbot Core ---
    val chatbotHistory = MutableStateFlow<List<Message>>(emptyList())
    val isChatLoading = MutableStateFlow<Boolean>(false)

    // --- Game Notification Stream ---
    val alertNotifications = MutableStateFlow<List<String>>(
        listOf(
            "Tournament Board: Premier League T20 is now LIVE!",
            "Auction Info: Pat Cummins registered at a Base Price of 15.0 Lakhs.",
            "Visual Engine: Default team emblems and customizable templates loaded."
        )
    )

    // --- Cloud Sync Configuration ---
    val syncConnected = MutableStateFlow<Boolean>(true)
    val syncLatencyMs = MutableStateFlow<Int>(12)
    val autoSyncEnabled = MutableStateFlow<Boolean>(true)
    val dbCheckSumSize = MutableStateFlow<String>("1.4 KB / 12 SQLite Tables")

    init {
        viewModelScope.launch {
            repository.seedDatabase()
            // Observe active match live updates dynamically
            selectedMatchId.collectLatest { matchId ->
                if (matchId != null) {
                    val m = repository.getMatchById(matchId)
                    _currentMatch.value = m
                    if (m != null) {
                        repository.getBallsForMatchFlow(matchId).collect { balls ->
                            _matchBalls.value = balls
                        }
                    }
                } else {
                    _currentMatch.value = null
                    _matchBalls.value = emptyList()
                }
            }
        }
    }

    // Add Simulated Game alert
    fun addAlert(message: String) {
        val currentList = alertNotifications.value.toMutableList()
        currentList.add(0, message)
        if (currentList.size > 20) currentList.removeAt(currentList.lastIndex)
        alertNotifications.value = currentList
    }

    // --- Custom Team/Tournament/Player Creators ---
    fun createTournament(name: String, format: String) {
        viewModelScope.launch {
            val id = repository.insertTournament(
                Tournament(name = name, format = format, status = "Scheduled")
            )
            addAlert("Tournament '$name' created successfully! (Format: $format)")
        }
    }

    fun createTeam(name: String, tournamentId: Int?, logoUri: String? = null) {
        viewModelScope.launch {
            repository.insertTeam(
                Team(
                    name = name,
                    tournamentId = tournamentId,
                    logoUri = logoUri,
                    budgetRemaining = 12000000.0,
                    budgetTotal = 12000000.0
                )
            )
            addAlert("New Team registered: $name")
        }
    }

    fun createPlayer(name: String, role: String, basePrice: Double, avatarUri: String? = null) {
        viewModelScope.launch {
            repository.insertPlayer(
                Player(
                    name = name,
                    role = role,
                    basePrice = basePrice,
                    status = "Registered",
                    avatarUri = avatarUri,
                    percentPerformance = (65 + (0..30).random()).toDouble()
                )
            )
            addAlert("Player '$name' registered under '$role' at ${currencySymbol.value} ${basePrice}")
        }
    }

    fun createMatch(tournamentId: Int?, team1: Team, team2: Team, overs: Int, format: String) {
        viewModelScope.launch {
            val matchId = repository.insertMatch(
                Match(
                    tournamentId = tournamentId,
                    team1Id = team1.id,
                    team2Id = team2.id,
                    team1Name = team1.name,
                    team2Name = team2.name,
                    oversMax = overs,
                    format = format,
                    status = "Live"
                )
            )
            selectedMatchId.value = matchId.toInt()
            addAlert("Match Started: ${team1.name} vs ${team2.name} (Max: $overs Overs)")
        }
    }

    // --- Scoring Engine Core ---
    fun selectStriker(id: Int) { strikerId.value = id }
    fun selectNonStriker(id: Int) { nonStrikerId.value = id }
    fun selectBowler(id: Int) { bowlerId.value = id }

    fun scoreBall(runs: Int, isExtra: Boolean, extraType: String?, isWicket: Boolean, wicketType: String?) {
        val currMatch = _currentMatch.value ?: return
        val striker = strikerId.value
        val bowler = bowlerId.value
        if (striker == null || bowler == null) {
            return
        }

        viewModelScope.launch {
            val bPlayer = repository.getPlayerById(bowler)
            val sPlayer = repository.getPlayerById(striker)
            if (bPlayer == null || sPlayer == null) return@launch

            // Ball counting
            val currentBallsList = _matchBalls.value
            val totalInningsBalls = currentBallsList.filter { it.innings == currMatch.currentInnings }.size
            val overNum = totalInningsBalls / 6
            val ballNum = (totalInningsBalls % 6) + 1

            // 1. Save ball in DB
            val commentary = when {
                isWicket -> "${sPlayer.name} is OUT! Wicket type: $wicketType. Excellent ball by ${bPlayer.name}."
                runs == 6 -> "BOOM! Smashed by ${sPlayer.name} for an elegant SIX!"
                runs == 4 -> "Four runs! Smashed down the ground by ${sPlayer.name}."
                else -> "${sPlayer.name} pushes the ball for $runs run(s)."
            }

            val newBall = BallByBall(
                matchId = currMatch.id,
                innings = currMatch.currentInnings,
                overNumber = overNum,
                ballNumber = ballNum,
                bowlerId = bPlayer.id,
                bowlerName = bPlayer.name,
                batsmanId = sPlayer.id,
                batsmanName = sPlayer.name,
                runs = runs,
                extras = if (isExtra) 1 else 0,
                extraType = extraType,
                isWicket = isWicket,
                wicketType = wicketType,
                commentary = commentary
            )
            repository.insertBall(newBall)

            // Calculate runs & wickets progression
            var extraRunValue = 0
            if (isExtra && (extraType == "Wide" || extraType == "No-Ball")) {
                extraRunValue = 1
            }

            // Update local matches state values
            val updatedMatch = if (currMatch.currentInnings == 1) {
                val totalRuns = currMatch.team1Runs + runs + extraRunValue
                val totalWickets = currMatch.team1Wickets + (if (isWicket) 1 else 0)
                val isWideNoBall = isExtra && (extraType == "Wide" || extraType == "No-Ball")
                val totalBallsNum = currentBallsList.filter { it.innings == 1 && it.extraType != "Wide" && it.extraType != "No-Ball" }.size + (if (isWideNoBall) 0 else 1)
                val oversCompleted = (totalBallsNum / 6) + ((totalBallsNum % 6) * 0.1)

                currMatch.copy(
                    team1Runs = totalRuns,
                    team1Wickets = totalWickets,
                    team1Overs = oversCompleted
                )
            } else {
                val totalRuns = currMatch.team2Runs + runs + extraRunValue
                val totalWickets = currMatch.team2Wickets + (if (isWicket) 1 else 0)
                val isWideNoBall = isExtra && (extraType == "Wide" || extraType == "No-Ball")
                val totalBallsNum = currentBallsList.filter { it.innings == 2 && it.extraType != "Wide" && it.extraType != "No-Ball" }.size + (if (isWideNoBall) 0 else 1)
                val oversCompleted = (totalBallsNum / 6) + ((totalBallsNum % 6) * 0.1)

                currMatch.copy(
                    team2Runs = totalRuns,
                    team2Wickets = totalWickets,
                    team2Overs = oversCompleted
                )
            }

            // Check if Innings ended (Max overs or 10 wickets)
            val inningsEnded = updatedMatch.team1Wickets >= 10 ||
                    (currMatch.currentInnings == 1 && updatedMatch.team1Overs >= currMatch.oversMax.toDouble()) ||
                    (currMatch.currentInnings == 2 && (updatedMatch.team2Wickets >= 10 || updatedMatch.team2Runs > updatedMatch.team1Runs || updatedMatch.team2Overs >= currMatch.oversMax.toDouble()))

            var finalMatchState = updatedMatch

            if (inningsEnded) {
                if (currMatch.currentInnings == 1) {
                    finalMatchState = finalMatchState.copy(currentInnings = 2)
                    addAlert("Innings 1 Complete! Target for Innings 2 is ${finalMatchState.team1Runs + 1} runs.")
                } else {
                    // Match Over! Declare winner
                    val winnerId = if (finalMatchState.team2Runs > finalMatchState.team1Runs) {
                        finalMatchState.team2Id
                    } else if (finalMatchState.team1Runs > finalMatchState.team2Runs) {
                        finalMatchState.team1Id
                    } else {
                        null // Tie
                    }
                    val marginStr = if (winnerId == finalMatchState.team2Id) {
                        "${finalMatchState.team2Name} won by ${10 - finalMatchState.team2Wickets} wickets"
                    } else if (winnerId == finalMatchState.team1Id) {
                        "${finalMatchState.team1Name} won by ${finalMatchState.team1Runs - finalMatchState.team2Runs} runs"
                    } else {
                        "Match Tied!"
                    }

                    finalMatchState = finalMatchState.copy(
                        status = "Completed",
                        winnerId = winnerId,
                        winMargin = marginStr
                    )
                    addAlert("Game Complete! $marginStr")

                    // Update Points Table for participating teams in database!
                    updatePointsTable(finalMatchState)
                }
            }

            repository.updateMatch(finalMatchState)
            _currentMatch.value = finalMatchState

            // Update active batsman stats
            val updatedStriker = sPlayer.copy(
                matchesPlayed = sPlayer.matchesPlayed + (if (sPlayer.matchesPlayed == 0) 1 else 0),
                totalRuns = sPlayer.totalRuns + runs,
                totalBalls = sPlayer.totalBalls + (if (isExtra && (extraType == "Wide")) 0 else 1),
                fours = sPlayer.fours + (if (runs == 4) 1 else 0),
                sixes = sPlayer.sixes + (if (runs == 6) 1 else 0),
                highestScore = maxOf(sPlayer.highestScore, sPlayer.totalRuns + runs)
            )
            repository.updatePlayer(updatedStriker)

            // Update bowler stats
            val isWideOrNoBall = isExtra && (extraType == "Wide" || extraType == "No-Ball")
            val ballsBowledIncrement = if (isWideOrNoBall) 0 else 1
            val runsConcededIncrement = runs + extraRunValue

            val updatedBowler = bPlayer.copy(
                matchesPlayed = bPlayer.matchesPlayed + (if (bPlayer.matchesPlayed == 0) 1 else 0),
                ballsBowled = bPlayer.ballsBowled + ballsBowledIncrement,
                runsConceded = bPlayer.runsConceded + runsConcededIncrement,
                totalWickets = bPlayer.totalWickets + (if (isWicket) 1 else 0)
            )
            repository.updatePlayer(updatedBowler)

            // Check if player reaches milestone
            calculateMilestoneCheck(updatedStriker)
        }
    }

    private suspend fun updatePointsTable(match: Match) {
        val team1 = repository.getTeamById(match.team1Id) ?: return
        val team2 = repository.getTeamById(match.team2Id) ?: return

        val (t1Res, t2Res) = when (match.winnerId) {
            team1.id -> Pair(1, 0)
            team2.id -> Pair(0, 1)
            else -> Pair(0, 0) // tie
        }

        // Calculate simple NRRs (simulated run rates)
        val t1RR = if (match.team1Overs > 0) match.team1Runs / match.team1Overs else 0.0
        val t2RR = if (match.team2Overs > 0) match.team2Runs / match.team2Overs else 0.0
        val nrrDiff = (t1RR - t2RR) / 10.0

        val nextT1 = team1.copy(
            matchesPlayed = team1.matchesPlayed + 1,
            matchesWon = team1.matchesWon + t1Res,
            matchesLost = team1.matchesLost + t2Res,
            points = team1.points + (t1Res * 2) + (if (match.winnerId == null) 1 else 0),
            netRunRate = team1.netRunRate + nrrDiff
        )

        val nextT2 = team2.copy(
            matchesPlayed = team2.matchesPlayed + 1,
            matchesWon = team2.matchesWon + t2Res,
            matchesLost = team2.matchesLost + t1Res,
            points = team2.points + (t2Res * 2) + (if (match.winnerId == null) 1 else 0),
            netRunRate = team2.netRunRate - nrrDiff
        )

        repository.updateTeam(nextT1)
        repository.updateTeam(nextT2)
    }

    private fun calculateMilestoneCheck(player: Player) {
        if (player.totalRuns == 50 && player.fifties == 0) {
            addAlert("MILESTONE! ${player.name} scored a brilliant Half-Century!")
        } else if (player.totalRuns == 100 && player.centuries == 0) {
            addAlert("MILESTONE! ${player.name} scored an epic Legendary Century!")
        }
    }

    // --- PDF Match Exporter ---
    fun exportMatchPdf(context: Context, match: Match) {
        viewModelScope.launch(Dispatchers.IO) {
            val balls = repository.getBallsForMatch(match.id)
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            val pTitle = Paint().apply {
                color = Color.BLACK
                textSize = 20f
                isFakeBoldText = true
            }

            val pHeader = Paint().apply {
                color = Color.DKGRAY
                textSize = 14f
                isFakeBoldText = true
            }

            val pText = Paint().apply {
                color = Color.BLACK
                textSize = 11f
            }

            val pLine = Paint().apply {
                color = Color.LTGRAY
                strokeWidth = 1f
            }

            canvas.drawText("Cricket Ultimate Suite - Official Match Report", 40f, 50f, pTitle)
            canvas.drawText("Status: ${match.status.uppercase()}", 40f, 75f, pHeader)
            canvas.drawLine(40f, 90f, 555f, 90f, pLine)

            canvas.drawText("Match: ${match.team1Name} vs ${match.team2Name}", 40f, 120f, pText)
            canvas.drawText("Format: ${match.format} Tournament Mode | Limit: ${match.oversMax} Overs", 40f, 140f, pText)
            canvas.drawText("Innings 1 Score : ${match.team1Runs}/${match.team1Wickets} in ${match.team1Overs} Overs", 40f, 170f, pText)
            canvas.drawText("Innings 2 Score : ${match.team2Runs}/${match.team2Wickets} in ${match.team2Overs} Overs", 40f, 190f, pText)
            canvas.drawText("Verdict: ${match.winMargin ?: "No result recorded"}", 40f, 220f, pHeader)

            canvas.drawLine(40f, 250f, 555f, 250f, pLine)
            canvas.drawText("Recent Match Highlights / Balls: ", 40f, 280f, pHeader)

            var yOffset = 310f
            balls.take(12).forEach { ball ->
                val ballStr = "Over ${ball.overNumber}.${ball.ballNumber}: ${ball.bowlerName} to ${ball.batsmanName} -> ${ball.runs} runs${if (ball.isWicket) " [WICKET - ${ball.wicketType}]" else ""}"
                canvas.drawText(ballStr, 50f, yOffset, pText)
                yOffset += 24f
            }

            canvas.drawLine(40f, yOffset + 20f, 555f, yOffset + 20f, pLine)
            canvas.drawText("Generated globally at 2026-06-15", 40f, yOffset + 45f, pText)

            document.finishPage(page)

            try {
                // Save to downloads directory
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val pdfFile = File(downloadsDir, "CricketMatch_Report_${match.id}.pdf")
                val outputStream: OutputStream = FileOutputStream(pdfFile)
                document.writeTo(outputStream)
                document.close()
                outputStream.close()

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Report exported cleanly: Downloads/CricketMatch_Report_${match.id}.pdf", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Export Error: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("PDF", "Export error", e)
                }
            }
        }
    }

    // --- Team Selection Draft Core ---
    fun selectDraftPlayer(team: Team, player: Player) {
        viewModelScope.launch {
            val updatedPlayer = player.copy(
                currentTeamId = team.id,
                status = "Drafted"
            )
            repository.updatePlayer(updatedPlayer)

            // Record choice in log
            val logMessage = Message(
                channel = "draft",
                sender = "Draft Command",
                content = "${team.name} has drafted standard All-Star ${player.name} (${player.role}) based on performance index: ${player.percentPerformance}%."
            )
            repository.insertMessage(logMessage)

            // Switch to next team index
            val totalTeams = teams.value.size
            if (totalTeams > 0) {
                currentDraftTeamIndex.value = (currentDraftTeamIndex.value + 1) % totalTeams
            }

            addAlert("Draft: ${player.name} drafted by ${team.name}")
        }
    }

    // --- Interactive Live Auction simulator core ---
    fun registerPlayerForAuction(player: Player) {
        currentAuctionPlayer.value = player
        currentHighestBid.value = player.basePrice
        currentHighestBidderId.value = null
        isAuctionRunning.value = true
        auctionMessages.value = emptyList()

        viewModelScope.launch {
            val announcement = Message(
                channel = "auction",
                sender = "Auctioneer",
                content = "Welcome to the real-time Cricket Board Auction! We are bidding on \"${player.name}\" (${player.role}). Base price triggers at ${currencySymbol.value} ${player.basePrice}."
            )
            repository.insertMessage(announcement)
            auctionMessages.value = listOf(announcement)
        }
    }

    fun placeAuctionBid(teamId: Int, bidAmount: Double) {
        val player = currentAuctionPlayer.value ?: return
        if (bidAmount <= currentHighestBid.value) return

        currentHighestBid.value = bidAmount
        currentHighestBidderId.value = teamId

        viewModelScope.launch {
            val sellingTeam = teams.value.firstOrNull { it.id == teamId } ?: return@launch
            val bidMsg = Message(
                channel = "auction",
                sender = sellingTeam.name,
                content = "Bidding raised to ${currencySymbol.value} $bidAmount!"
            )
            repository.insertMessage(bidMsg)

            // Refresh logs
            val currentMsgs = auctionMessages.value.toMutableList()
            currentMsgs.add(bidMsg)
            auctionMessages.value = currentMsgs

            // Trigger AI Automated Counter-Bidders up to player evaluation limits!
            triggerAiCounterBids(teamId, bidAmount)
        }
    }

    private fun triggerAiCounterBids(userTeamId: Int, bidAmount: Double) {
        viewModelScope.launch {
            delay(1500)
            val player = currentAuctionPlayer.value ?: return@launch
            if (currentHighestBidderId.value != userTeamId) return@launch // Somebody else bid

            // Base limit is 1.8x base price
            val maxHeuristicBid = player.basePrice * 2.2
            if (bidAmount < maxHeuristicBid) {
                // Find a competitive team
                val otherTeams = teams.value.filter { it.id != userTeamId }
                if (otherTeams.isNotEmpty()) {
                    val aiTeam = otherTeams.random()
                    val nextBid = bidAmount + (player.basePrice * 0.1).coerceAtLeast(50000.0)

                    currentHighestBid.value = nextBid
                    currentHighestBidderId.value = aiTeam.id

                    val aiMsg = Message(
                        channel = "auction",
                        sender = aiTeam.name,
                        content = "Counter bid raised to ${currencySymbol.value} $nextBid. We want him!"
                    )
                    repository.insertMessage(aiMsg)

                    val currentMsgs = auctionMessages.value.toMutableList()
                    currentMsgs.add(aiMsg)
                    auctionMessages.value = currentMsgs

                    addAlert("Auction Bid: ${aiTeam.name} bids ${currencySymbol.value} $nextBid")
                }
            } else {
                // AI stands down / sold to user
                val finalMsg = Message(
                    channel = "auction",
                    sender = "Auctioneer",
                    content = "Going once... Going twice... SOLD!"
                )
                repository.insertMessage(finalMsg)
                val currentMsgs = auctionMessages.value.toMutableList()
                currentMsgs.add(finalMsg)
                auctionMessages.value = currentMsgs
            }
        }
    }

    fun completeAuction() {
        val player = currentAuctionPlayer.value ?: return
        val winningTeamId = currentHighestBidderId.value
        val finalPrice = currentHighestBid.value

        if (winningTeamId != null) {
            viewModelScope.launch {
                val team = repository.getTeamById(winningTeamId)
                if (team != null && team.budgetRemaining >= finalPrice) {
                    val updatedPlayer = player.copy(
                        currentTeamId = winningTeamId,
                        sellingPrice = finalPrice,
                        status = "Sold"
                    )
                    repository.updatePlayer(updatedPlayer)

                    val updatedTeam = team.copy(
                        budgetRemaining = team.budgetRemaining - finalPrice
                    )
                    repository.updateTeam(updatedTeam)

                    addAlert("SOLD: ${player.name} sold to ${team.name} for ${currencySymbol.value} $finalPrice")
                }
            }
        }
        isAuctionRunning.value = false
        currentAuctionPlayer.value = null
    }

    // --- AI Chatbot and Generators Core (Gemini API Integration) ---
    fun askGemini(prompt: String) {
        if (prompt.isBlank()) return

        val userMessage = Message(channel = "chatbot", sender = "You", content = prompt)
        val currentHistory = chatbotHistory.value.toMutableList()
        currentHistory.add(userMessage)
        chatbotHistory.value = currentHistory
        isChatLoading.value = true

        val apiKey = BuildConfig.GEMINI_API_KEY

        viewModelScope.launch {
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                // Graceful sandbox simulated response if key is placeholder
                delay(1200)
                val simulatedResult = getSimulatedExpertCricketResponse(prompt)
                val aiMessage = Message(
                    channel = "chatbot",
                    sender = "Gemini Analyst",
                    content = simulatedResult,
                    isAi = true
                )
                val updated = chatbotHistory.value.toMutableList()
                updated.add(aiMessage)
                chatbotHistory.value = updated
                isChatLoading.value = false
                return@launch
            }

            try {
                val sysInstruction = "You are an elite, mathematical Cricket Team Manager, coach, and analyst chatbot. Provide expert team stats reviews, bidding strategies, player matchups, recommendations, and tactical insights based on the user's queries."
                val replyText = executeGeminiPost(apiKey, prompt, sysInstruction)

                val aiMsg = Message(
                    channel = "chatbot",
                    sender = "Gemini Analyst",
                    content = replyText,
                    isAi = true
                )
                val updatedHistory = chatbotHistory.value.toMutableList()
                updatedHistory.add(aiMsg)
                chatbotHistory.value = updatedHistory

            } catch (e: Exception) {
                val errorMsg = Message(
                    channel = "chatbot",
                    sender = "Gemini Analyst",
                    content = "Connection limitation. Let me give you a local tactical summary: ${getSimulatedExpertCricketResponse(prompt)}",
                    isAi = true
                )
                val updatedHistory = chatbotHistory.value.toMutableList()
                updatedHistory.add(errorMsg)
                chatbotHistory.value = updatedHistory
            } finally {
                isChatLoading.value = false
            }
        }
    }

    private fun getSimulatedExpertCricketResponse(prompt: String): String {
        return when {
            prompt.contains("auction", ignoreCase = true) -> {
                "Strategic Advice: In auctions, reserve 40% of your budget for top-tier, death-overs bowlers. All-rounders (with strike rates > 140) should be bid on aggressively, up to 2.5 Lakhs base price."
            }
            prompt.contains("draft", ignoreCase = true) -> {
                "Draft Recommendation: Select 'Virat Sharma' first as an anchor, then pair him with bowler 'Jaspreet Bumrah' to maintain excellent bowling economy (currently 5.33) in T20 sessions."
            }
            prompt.contains("bowl", ignoreCase = true) || prompt.contains("bowler", ignoreCase = true) -> {
                "Bowling analysis: Spin bowlers are highly effective on dry, slow surfaces (target 25% of overs in compact matches)."
            }
            else -> {
                "Tactical Advisory: Ensure a healthy run-rate progression of at least 8.5 runs-per-over during powerplays, adjusting for early wickets by rotating boundaries with singles."
            }
        }
    }

    // --- AI Generator helpers ---
    fun generateAiPlayerBio(player: Player, onGenerated: (String) -> Unit) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val prompt = "Generate a 3-sentence exciting cricket legend biography for player: ${player.name}, who plays as ${player.role}. Include a mock origin story and their iconic signature shot."

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            onGenerated("${player.name} is a legendary cricket icon known for their fierce cover drives. Born on the streets of Mumbai, they clawed their way to the absolute peak of international cricket through sheer perseverance, mastering the legendary 'Helicopter Sweep'.")
            return
        }

        viewModelScope.launch {
            try {
                val text = executeGeminiPost(apiKey, prompt)
                onGenerated(text)
            } catch (e: Exception) {
                onGenerated("${player.name} is a renowned professional cricket champion admired worldwide. Known for outstanding game sense and tactical reflexes, they are highly regarded as a key pressure player in elite tournament fixtures.")
            }
        }
    }

    fun generateAiLogoPrompt(concept: String, onGenerated: (String) -> Unit) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val prompt = "Write a clear, brief (1 sentence) image design prompt for an elegant cricket team logo based on concept: $concept"

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            onGenerated("An elegant, high-contrast, modern emblem featuring a flaming cricket ball with bold emerald and gold colors, suitable for sport application headers.")
            return
        }

        viewModelScope.launch {
            try {
                val text = executeGeminiPost(apiKey, prompt)
                onGenerated(text)
            } catch (e: Exception) {
                onGenerated("A clean vector style emblem featuring a striking cricket bat with integrated stars on a solid dark backplate.")
            }
        }
    }
}
