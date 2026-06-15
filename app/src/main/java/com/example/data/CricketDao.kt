package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CricketDao {
    // --- Tournaments ---
    @Query("SELECT * FROM tournaments ORDER BY id DESC")
    fun getAllTournaments(): Flow<List<Tournament>>

    @Query("SELECT * FROM tournaments WHERE id = :id")
    suspend fun getTournamentById(id: Int): Tournament?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournament(tournament: Tournament): Long

    @Update
    suspend fun updateTournament(tournament: Tournament)

    @Delete
    suspend fun deleteTournament(tournament: Tournament)


    // --- Teams ---
    @Query("SELECT * FROM teams ORDER BY points DESC, netRunRate DESC, name ASC")
    fun getAllTeamsFlow(): Flow<List<Team>>

    @Query("SELECT * FROM teams WHERE tournamentId = :tournamentId ORDER BY points DESC, netRunRate DESC")
    fun getTeamsByTournamentFlow(tournamentId: Int): Flow<List<Team>>

    @Query("SELECT * FROM teams WHERE tournamentId = :tournamentId OR tournamentId IS NULL ORDER BY name ASC")
    suspend fun getTeamsByTournamentOrFree(tournamentId: Int?): List<Team>

    @Query("SELECT * FROM teams")
    suspend fun getAllTeams(): List<Team>

    @Query("SELECT * FROM teams WHERE id = :id")
    suspend fun getTeamById(id: Int): Team?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeam(team: Team): Long

    @Update
    suspend fun updateTeam(team: Team)

    @Delete
    suspend fun deleteTeam(team: Team)


    // --- Players ---
    @Query("SELECT * FROM players ORDER BY name ASC")
    fun getAllPlayersFlow(): Flow<List<Player>>

    @Query("SELECT * FROM players WHERE currentTeamId = :teamId ORDER BY name ASC")
    fun getPlayersByTeamFlow(teamId: Int): Flow<List<Player>>

    @Query("SELECT * FROM players WHERE id = :id")
    suspend fun getPlayerById(id: Int): Player?

    @Query("SELECT * FROM players")
    suspend fun getAllPlayers(): List<Player>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: Player): Long

    @Update
    suspend fun updatePlayer(player: Player)

    @Delete
    suspend fun deletePlayer(player: Player)


    // --- Matches ---
    @Query("SELECT * FROM matches ORDER BY id DESC")
    fun getAllMatchesFlow(): Flow<List<Match>>

    @Query("SELECT * FROM matches WHERE tournamentId = :tournamentId ORDER BY id DESC")
    fun getMatchesByTournamentFlow(tournamentId: Int): Flow<List<Match>>

    @Query("SELECT * FROM matches WHERE id = :id")
    suspend fun getMatchById(id: Int): Match?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: Match): Long

    @Update
    suspend fun updateMatch(match: Match)


    // --- Ball by ball ---
    @Query("SELECT * FROM balls WHERE matchId = :matchId ORDER BY id ASC")
    fun getBallsForMatchFlow(matchId: Int): Flow<List<BallByBall>>

    @Query("SELECT * FROM balls WHERE matchId = :matchId")
    suspend fun getBallsForMatch(matchId: Int): List<BallByBall>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBall(ball: BallByBall): Long

    @Query("DELETE FROM balls WHERE matchId = :matchId")
    suspend fun deleteBallsForMatch(matchId: Int)


    // --- Auction bids ---
    @Query("SELECT * FROM auction_bids WHERE playerId = :playerId ORDER BY id DESC")
    fun getBidsForPlayerFlow(playerId: Int): Flow<List<AuctionBid>>

    @Query("SELECT * FROM auction_bids ORDER BY id DESC")
    fun getAllAuctionBidsFlow(): Flow<List<AuctionBid>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuctionBid(bid: AuctionBid): Long

    @Query("DELETE FROM auction_bids")
    suspend fun clearAllBids()


    // --- Chat Messages ---
    @Query("SELECT * FROM messages WHERE channel = :channel ORDER BY timestamp ASC")
    fun getMessagesByChannelFlow(channel: String): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE channel = :channel ORDER BY timestamp ASC")
    suspend fun getMessagesByChannel(channel: String): List<Message>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message): Long

    @Query("DELETE FROM messages WHERE channel = :channel")
    suspend fun clearMessagesForChannel(channel: String)
}
