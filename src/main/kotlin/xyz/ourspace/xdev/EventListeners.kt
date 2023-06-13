package xyz.ourspace.xdev

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import xyz.ourspace.xdev.types.EventInfo
import xyz.ourspace.xdev.types.PlayerInfo
import xyz.ourspace.xdev.types.ServerPlayerStats
import xyz.ourspace.xdev.utils.AuthUtility
import xyz.ourspace.xdev.utils.Logger.consoleLog
import xyz.ourspace.xdev.utils.Logger.consoleLogWarning

data class EventArguments(val player: PlayerInfo, val server: ServerPlayerStats?, val event: EventInfo?)

class EventListeners(private val apiConnection: APIConnection, private val auth: AuthUtility) : Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	fun onPlayerLogin(event: AsyncPlayerPreLoginEvent) {
		val player = Bukkit.getServer().getOfflinePlayer(event.uniqueId);
		val hasJoined = player.hasPlayedBefore()
		if (!hasJoined) {
			consoleLogWarning("Player ${player.name} has not joined before, skipping authentication")
			return
		}
		val authenticated = auth.verify(player, event.address.hostAddress)
		if (!authenticated.first) {
			consoleLog("Player ${player.name} failed to authenticate, ${authenticated.second}")
			event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Orizuru.instance.config.getString("auth.messages.failureMessage")!!)
			return
		}
	}

	@EventHandler
	fun onPlayerJoin(event: PlayerJoinEvent) {
		val player = event.player
		auth.welcome(player)
		val playerCount = Bukkit.getOnlinePlayers().size
		val location = """${player.location.x}, ${player.location.y}, ${player.location.z}"""
		val playerInfo = PlayerInfo(player.name, player.uniqueId.toString(), player.address.toString())
		val obj = EventArguments(
				playerInfo,
				ServerPlayerStats(playerCount, Bukkit.getMaxPlayers()),
				EventInfo(location, playerInfo, "join")
		)
		runCatching {
			apiConnection.post("Player ${player.name} Joined", "PlayerJoin", obj)
		}.onFailure {
			consoleLogWarning("Failed to send PlayerJoin event to API")
		}

	}

	@EventHandler
	fun onPlayerQuit(event: PlayerQuitEvent) {
		val playerCount = Bukkit.getOnlinePlayers().size - 1
		val player = event.player

		val location = """${player.location.x}, ${player.location.y}, ${player.location.z}"""
		val playerInfo = PlayerInfo(player.name, player.uniqueId.toString(), player.address.toString())
		val obj = EventArguments(
				playerInfo,
				ServerPlayerStats(playerCount, Bukkit.getMaxPlayers()),
				EventInfo(location, playerInfo, "quit")
		)
		runCatching {
			apiConnection.post("Player ${player.name} Left", "PlayerLeave", obj)
		}.onFailure {
			consoleLogWarning("Failed to post PlayerLeave event")
		}
	}

	@EventHandler
	fun onPlayerChat(event: AsyncPlayerChatEvent) {
		var eventMsg = event.message
		val player = event.player
		eventMsg = eventMsg.replace("@everyone".toRegex(), "``@everyone``").replace("@here".toRegex(), "``@here``")
		val location = """${player.location.x}, ${player.location.y}, ${player.location.z}"""
		val playerInfo = PlayerInfo(player.name, player.uniqueId.toString(), null)
		val obj = EventArguments(
				playerInfo,
				null,
				EventInfo(location, playerInfo, "chat")
		)
		runCatching {
			apiConnection.post(eventMsg, "Chat", obj)
		}.onFailure {
			consoleLogWarning("Failed to send chat message to API")
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	fun onPlayerDeath(event: PlayerDeathEvent) {
		val player = event.entity

		val location = """${player.location.x}, ${player.location.y}, ${player.location.z}"""
		val playerInfo = PlayerInfo(player.name, player.uniqueId.toString(), null)
		val obj = EventArguments(
				playerInfo,
				null,
				EventInfo(location, playerInfo, "death")
		)
		val msg = event.deathMessage ?: "Player ${player.name} died"
		apiConnection.post(msg, "PlayerDeath", obj)
	}
}