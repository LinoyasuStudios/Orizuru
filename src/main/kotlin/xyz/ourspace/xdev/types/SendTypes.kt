package xyz.ourspace.xdev.types

data class ServerInfo(
	val serverName: String,
	val serverVersion: String,
	val serverMotd: String,
	val serverMaxPlayers: Int,
	val serverIP: String,
	val serverPort: Int,
	var pluginVersion: String
)
data class PlayerInfo(
	val name: String?,
	val uuid: String,
	val ip: String?
)
data class EventInfo(
	val location: String,
	val entity: PlayerInfo,
	val eventName: String,
)

data class ServerStats(
	val online: Int,
	val max: Int,
	val tps: Double,
	val memory: Long,
	val cpu: Double,
	val uptime: Long,
	val pluginVersion: String
)
class ServerPlayerStats(
	online: Int,
	max: Int
)

data class AuthArguments (val player: PlayerInfo)