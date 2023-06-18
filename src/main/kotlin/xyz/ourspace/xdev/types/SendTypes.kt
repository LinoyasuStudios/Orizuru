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

data class ServerPlayerStats(
		val online: Int,
		val max: Int
)

data class PerformanceData(
		val tps: DoubleArray,
		val availableMemory: Long,
		val maxMemory: Long,
		val usedMemory: Long,
		val memPercent: Long,
		val cpuPercent: Long,
)

data class AuthArguments(val player: PlayerInfo)
data class JoinArguments(
		val player: PlayerInfo,
		val server: ServerPlayerStats,
		val event: EventInfo
)
data class LeaveArguments(
		val player: PlayerInfo,
		val server: ServerPlayerStats,
		val event: EventInfo
)
data class DeathArguments(
		val player: PlayerInfo,
		val event: EventInfo,
		val message: String
)
data class CommandArguments(
		val player: PlayerInfo,
		val event: EventInfo,
		val command: String,
		val args: Array<String>,
)
data class AdvancementArguments(
		val player: PlayerInfo,
		val event: EventInfo,
		val advancement: String,
		val advancementCriteria: String,
)
data class ChatArguments(
		val player: PlayerInfo,
		val event: EventInfo,
		val message: String
)