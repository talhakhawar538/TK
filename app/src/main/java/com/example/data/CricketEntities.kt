package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "tournaments")
data class Tournament(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val format: String, // "T20" | "ODI" | "Test"
    val status: String, // "Scheduled" | "Live" | "Completed"
    val logoUri: String? = null
) : Serializable

@Entity(tableName = "teams")
data class Team(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tournamentId: Int? = null,
    val name: String,
    val logoUri: String? = null,
    val budgetRemaining: Double = 10000000.0, // base 10M
    val budgetTotal: Double = 10000000.0,
    val matchesPlayed: Int = 0,
    val matchesWon: Int = 0,
    val matchesLost: Int = 0,
    val matchesTied: Int = 0,
    val points: Int = 0,
    val netRunRate: Double = 0.0
) : Serializable

@Entity(tableName = "players")
data class Player(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val role: String, // "Batsman" | "Bowler" | "All-Rounder" | "Wicketkeeper"
    val basePrice: Double = 100000.0,
    val sellingPrice: Double = 0.0,
    val currentTeamId: Int? = null,
    val status: String = "Registered", // "Registered" | "Unsold" | "Sold" | "Drafted"
    val avatarUri: String? = null,
    val matchesPlayed: Int = 0,
    val totalRuns: Int = 0,
    val totalBalls: Int = 0,
    val totalWickets: Int = 0,
    val runsConceded: Int = 0,
    val ballsBowled: Int = 0,
    val sixes: Int = 0,
    val fours: Int = 0,
    val highestScore: Int = 0,
    val bestBowlingWickets: Int = 0,
    val bestBowlingRuns: Int = 0,
    val strikeRate: Double = 0.0,
    val economyRate: Double = 0.0,
    val fifties: Int = 0,
    val centuries: Int = 0,
    val fastestFifty: Int = 0, // in balls
    val fastestCentury: Int = 0, // in balls
    val matchesPlayedHistoric: Int = 0,
    val totalRunsHistoric: Int = 0,
    val totalWicketsHistoric: Int = 0,
    val runsConcededHistoric: Int = 0,
    val ballsBowledHistoric: Int = 0,
    val percentPerformance: Double = 0.0 // Computed performance percentage
) : Serializable

@Entity(tableName = "matches")
data class Match(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tournamentId: Int? = null,
    val team1Id: Int,
    val team2Id: Int,
    val team1Name: String,
    val team2Name: String,
    val format: String, // "T20" | "ODI" | "Test"
    val oversMax: Int = 20,
    val status: String = "Scheduled", // "Scheduled" | "Live" | "Completed"
    val tossWinnerId: Int? = null,
    val tossDecision: String? = null, // "Bat" | "Bowl"
    val winnerId: Int? = null,
    val team1Runs: Int = 0,
    val team1Wickets: Int = 0,
    val team1Overs: Double = 0.0,
    val team2Runs: Int = 0,
    val team2Wickets: Int = 0,
    val team2Overs: Double = 0.0,
    val currentInnings: Int = 1, // 1 or 2
    val winMargin: String? = null
) : Serializable

@Entity(tableName = "balls")
data class BallByBall(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val matchId: Int,
    val innings: Int,
    val overNumber: Int,
    val ballNumber: Int,
    val bowlerId: Int,
    val bowlerName: String,
    val batsmanId: Int,
    val batsmanName: String,
    val runs: Int,
    val extras: Int,
    val extraType: String? = null, // "Wide" | "No-Ball" | "Bye" | "Leg-Bye"
    val isWicket: Boolean = false,
    val wicketType: String? = null, // "Bowled" | "Caught" | "LBW" | "Run Out" | "Stumped"
    val commentary: String = ""
) : Serializable

@Entity(tableName = "auction_bids")
data class AuctionBid(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val playerId: Int,
    val playerName: String,
    val teamId: Int,
    val teamName: String,
    val bidAmount: Double,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val channel: String, // "auction" | "draft" | "chatbot"
    val sender: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isAi: Boolean = false
) : Serializable
