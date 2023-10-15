package xyz.ourspace.xdev

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.*
import xyz.ourspace.xdev.types.*
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
			/*Bukkit.getScheduler().runTaskLater(Orizuru.instance, Runnable {
				// Kick player 5 Seconds after they join
				Bukkit.getPlayer(player.uniqueId)?.kickPlayer("Please rejoin to authenticate")
			}, 100L)*/
			return
		}
		val authenticated = auth.verify(player, event.address.hostAddress)
		if (!authenticated.first) {
			consoleLog("Player ${player.name} failed to authenticate, ${authenticated.second}")
			event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Orizuru.instance.config.getString("auth.messages.failureMessage")!!)
			return
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
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
			apiConnection.postAsync("Player ${player.name} Joined", OrizContentType.PLAYERJOIN, obj)
		}.onFailure {
			consoleLogWarning("Failed to send PlayerJoin event to API")
		}

	}

	@EventHandler(priority = EventPriority.MONITOR)
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
			apiConnection.postAsync("Player ${player.name} Left", OrizContentType.PLAYERLEAVE, obj)
		}.onFailure {
			consoleLogWarning("Failed to post PlayerLeave event")
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	fun onPlayerChat(event: AsyncPlayerChatEvent) {
		val eventMsg = event.message
		val player = event.player
		val location = """${player.location.x}, ${player.location.y}, ${player.location.z}"""
		val playerInfo = PlayerInfo(player.name, player.uniqueId.toString(), null)
		val obj = ChatArguments(
				playerInfo,
				EventInfo(location, playerInfo, "chat"),
				eventMsg
		)
		runCatching {
			apiConnection.postAsync(eventMsg, OrizContentType.PLAYERCHAT, obj)
		}.onFailure {
			consoleLogWarning("Failed to send chat message to API")
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	fun onPlayerDeath(event: PlayerDeathEvent) {
		val player = event.entity

		val location = """${player.location.x}, ${player.location.y}, ${player.location.z}"""
		val playerInfo = PlayerInfo(player.name, player.uniqueId.toString(), null)
		val obj = DeathArguments(
				playerInfo,
				EventInfo(location, playerInfo, "death"),
				event.deathMessage ?: "Player ${player.name} died"
		)
		val msg = event.deathMessage ?: "Player ${player.name} died"
		runCatching {
			apiConnection.postAsync(msg, OrizContentType.PLAYERDEATH, obj)
		}.onFailure {
			consoleLogWarning("Failed to send PlayerDeath event to API")
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	fun onPlayerCommand(event: AsyncPlayerChatEvent){
		if (!event.message.startsWith("/")) return
		val player = event.player
		val location = """${player.location.x}, ${player.location.y}, ${player.location.z}"""
		val playerInfo = PlayerInfo(player.name, player.uniqueId.toString(), null)
		val commandName = event.message.split(" ")[0].replace("/", "")
		val commandArgs = event.message.split(" ").drop(1).toTypedArray()
		val obj = CommandArguments(
				playerInfo,
				EventInfo(location, playerInfo, "command"),
				commandName,
				commandArgs,
		)
		runCatching {
			apiConnection.postAsync(event.message, OrizContentType.PLAYERCOMMAND, obj)
		}.onFailure {
			consoleLogWarning("Failed to send PlayerCommand event to API")
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	fun onPlayerAchievement(event: PlayerAdvancementDoneEvent) {
		val player = event.player
		val location = """${player.location.x}, ${player.location.y}, ${player.location.z}"""
		val playerInfo = PlayerInfo(player.name, player.uniqueId.toString(), null)
		val advancementName = event.advancement.toString()
		val criteria = event.advancement.criteria.toString()
		val obj = AdvancementArguments(
				playerInfo,
				EventInfo(location, playerInfo, "achievement"),
				advancementName,
				criteria,
		)
		runCatching {
			apiConnection.postAsync(event.advancement.toString(), OrizContentType.PLAYERADVANCEMENT, obj)
		}.onFailure {
			consoleLogWarning("Failed to send PlayerAchievement event to API")
		}
	}
}