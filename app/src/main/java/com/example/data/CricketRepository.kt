package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class CricketRepository(private val dao: CricketDao) {

    // Tournaments
    val allTournaments: Flow<List<Tournament>> = dao.getAllTournaments()
    suspend fun getTournamentById(id: Int): Tournament? = dao.getTournamentById(id)
    suspend fun insertTournament(tournament: Tournament): Long = dao.insertTournament(tournament)
    suspend fun updateTournament(tournament: Tournament) = dao.updateTournament(tournament)
    suspend fun deleteTournament(tournament: Tournament) = dao.deleteTournament(tournament)

    // Teams
    val allTeamsFlow: Flow<List<Team>> = dao.getAllTeamsFlow()
    fun getTeamsByTournamentFlow(tournamentId: Int): Flow<List<Team>> = dao.getTeamsByTournamentFlow(tournamentId)
    suspend fun getTeamsByTournamentOrFree(tournamentId: Int?): List<Team> = dao.getTeamsByTournamentOrFree(tournamentId)
    suspend fun getTeamById(id: Int): Team? = dao.getTeamById(id)
    suspend fun insertTeam(team: Team): Long = dao.insertTeam(team)
    suspend fun updateTeam(team: Team) = dao.updateTeam(team)
    suspend fun deleteTeam(team: Team) = dao.deleteTeam(team)

    // Players
    val allPlayersFlow: Flow<List<Player>> = dao.getAllPlayersFlow()
    fun getPlayersByTeamFlow(teamId: Int): Flow<List<Player>> = dao.getPlayersByTeamFlow(teamId)
    suspend fun getPlayerById(id: Int): Player? = dao.getPlayerById(id)
    suspend fun getAllPlayers(): List<Player> = dao.getAllPlayers()
    suspend fun insertPlayer(player: Player): Long = dao.insertPlayer(player)
    suspend fun updatePlayer(player: Player) = dao.updatePlayer(player)
    suspend fun deletePlayer(player: Player) = dao.deletePlayer(player)

    // Matches
    val allMatchesFlow: Flow<List<Match>> = dao.getAllMatchesFlow()
    fun getMatchesByTournamentFlow(tournamentId: Int): Flow<List<Match>> = dao.getMatchesByTournamentFlow(tournamentId)
    suspend fun getMatchById(id: Int): Match? = dao.getMatchById(id)
    suspend fun insertMatch(match: Match): Long = dao.insertMatch(match)
    suspend fun updateMatch(match: Match) = dao.updateMatch(match)

    // Ball-by-ball
    fun getBallsForMatchFlow(matchId: Int): Flow<List<BallByBall>> = dao.getBallsForMatchFlow(matchId)
    suspend fun getBallsForMatch(matchId: Int): List<BallByBall> = dao.getBallsForMatch(matchId)
    suspend fun insertBall(ball: BallByBall): Long = dao.insertBall(ball)
    suspend fun deleteBallsForMatch(matchId: Int) = dao.deleteBallsForMatch(matchId)

    // Auction Bids
    fun getBidsForPlayerFlow(playerId: Int): Flow<List<AuctionBid>> = dao.getBidsForPlayerFlow(playerId)
    val allAuctionBidsFlow: Flow<List<AuctionBid>> = dao.getAllAuctionBidsFlow()
    suspend fun insertAuctionBid(bid: AuctionBid): Long = dao.insertAuctionBid(bid)
    suspend fun clearAllBids() = dao.clearAllBids()

    // Messages
    fun getMessagesByChannelFlow(channel: String): Flow<List<Message>> = dao.getMessagesByChannelFlow(channel)
    suspend fun getMessagesByChannel(channel: String): List<Message> = dao.getMessagesByChannel(channel)
    suspend fun insertMessage(message: Message): Long = dao.insertMessage(message)
    suspend fun clearMessagesForChannel(channel: String) = dao.clearMessagesForChannel(channel)

    // Database pre-seeding
    suspend fun seedDatabase() {
        // Empty implementation - Let the user create their own teams and players.
    }
}
