package xyz.ourspace.xdev.utils

import net.milkbowl.vault.permission.Permission
import org.bukkit.Bukkit
import org.bukkit.Bukkit.getServer
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import xyz.ourspace.xdev.OrizContentType
import xyz.ourspace.xdev.Orizuru
import xyz.ourspace.xdev.types.AuthArguments
import xyz.ourspace.xdev.types.AuthContent
import xyz.ourspace.xdev.types.PlayerInfo
import xyz.ourspace.xdev.types.UserData



private val authStringMap = mutableMapOf<String, String>(
		"playerName" to "noname",
		"playerUsername" to "nousername",
		"playerIdentifier" to "noidentifier"
)

class AuthUtility(private val plugin: Orizuru) {
	private var perms: Permission? = null
	init{
		val rsp = getServer().servicesManager.getRegistration(Permission::class.java)
		perms = rsp?.provider
		Logger.consoleLog("Vault Permissions initialized")
	}
	private val connection = plugin.connection
	// This Returns either a Pair of Boolean and String or null
	fun verify(player: OfflinePlayer, addr:String): Pair<Boolean, String?> {
		if(!plugin.config.getBoolean("auth.enabled")) {
			return Pair(true, null)
		}
		if(!connection.initialized){
			Logger.consoleLogError("Connection not initialized while verifying player")
			return Pair(false, "Connection not initialized")
		}
		if(perms == null){
			Logger.consoleLogError("Vault Permissions not initialized while verifying player")
		}
		val bypass = perms?.playerHas(null, player, "orizuru.auth.bypass")
		//Logger.consoleLog("Player ${player.name} bypass is $bypass")
		if (bypass == true) {
			Logger.consoleLog("Player ${player.name} bypassed authentication")
			return Pair(true, null)
		}
		val args = AuthArguments(
				player = PlayerInfo(
						uuid = player.uniqueId.toString(),
						name = player.name,
						ip = addr
				)
		)
		try {
			val res = this.connection.postWithResponse(OrizContentType.AUTH, args, AuthContent::class.java)
			if (res.status != 200) {
				return Pair(false, "Server returned ${res.status}")
			}
			val content = res.content
			if (content != null) {
				if (content.body == null || content.err == null) {
					Logger.consoleLogError("AuthUtility, Auth body or err is null")
					return Pair(false, "Auth body or err is null")
				}
			}
			if (content!!.err == true) {
				Logger.consoleLogWarning("Error: ${content.code}, while authenticating player ${player.name}")
				return Pair(false, "Error ${content.code} from authenticator")
			}
			if (content.body!!.player == null) {
				Logger.consoleLogWarning("Error: Player Received from auth is null, while authenticating player ${player.name}")
				return Pair(false, "Player Received from auth is null")
			}
			if (content.body.player!!.uuid != player.uniqueId.toString()) {
				Logger.consoleLogWarning("Error: Player ${player.name} UUID mismatch on authentication.")
				return Pair(false, "Player UUID mismatch on authentication")
			}
			if (content.body.identifier == null) {
				Logger.consoleLogWarning("Error: Player ${player.uniqueId} identifier is null on authentication.")
				return Pair(false, "Player identifier is null on authentication")
			}
			if (content.body.name == null) {
				content.body.name = player.name
			}
			plugin.memoryHolder.set(player.uniqueId.toString(),
					UserData(player.name?:"Unknown", content.body.identifier, content.body.name!!),
					20000L
			)
			return Pair(true, null)
		}catch(e: Exception) {
			Logger.consoleLogError("Failed to authenticate player ${player.name} due to exception: ${e.message}")
			return Pair(false, "Failed to authenticate, contact server admin")
		}
	}
	fun welcome(player: Player) {
		val v = plugin.memoryHolder.get(player.uniqueId.toString(), UserData::class.java)
		if(v.isEmpty) {
			return
		}
		val data = v.get()
		authStringMap["playerIdentifier"] = data.identifier
		authStringMap["playerUsername"] = data.username
		authStringMap["playerName"] = data.name

		val str = StringTemplateReplacer.replace(plugin.config.getString("auth.messages.successMessage")!!, authStringMap)
		player.sendMessage(str)
		plugin.memoryHolder.remove(player.uniqueId.toString(), UserData::class.java)
	}
}