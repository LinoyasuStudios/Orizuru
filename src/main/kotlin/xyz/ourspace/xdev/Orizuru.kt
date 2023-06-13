package xyz.ourspace.xdev

//import org.bukkit.Bukkit
//import org.bukkit.command.CommandMap
import kotlinx.coroutines.Dispatchers.Unconfined
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bukkit.plugin.java.JavaPlugin
import xyz.ourspace.xdev.commands.InfoCommand
import xyz.ourspace.xdev.types.ServerInfo
import xyz.ourspace.xdev.utils.AuthUtility
import xyz.ourspace.xdev.utils.ConfigValidator
import xyz.ourspace.xdev.utils.Logger.consoleLogWarning
import xyz.ourspace.xdev.utils.MemoryHolder
import xyz.ourspace.xdev.utils.SelfUpdate
import java.io.File
import java.io.IOException

class Orizuru : JavaPlugin() {
	val connection : APIConnection = APIConnection()

	private val instance : Orizuru = this
	private val configValidator = ConfigValidator()
	private val authUtility : AuthUtility = AuthUtility(instance)
	val memoryHolder = MemoryHolder(instance)

	override fun onEnable() {
		// Plugin startup logic
		//Check config.yml if exists, if not create it
		try {
			if (!File(this.dataFolder, "config.yml").exists()) {
				this.saveDefaultConfig()
			}
		}
		catch (e: Exception) {
			consoleLogWarning(e)
		}
		//Check if the config is valid, if not disable the plugin
		if (!configValidator.validate(this.config)) {
			consoleLogWarning("Config is not valid, disabling plugin")
			this.isEnabled = false
			return
		}
		connection.init(
			"${config.getString("api.url")}",
			"${config.getString("api.password")}",
			"${config.getString("api.uniqueId")}",
		)
		runBlocking(Unconfined) {
			//Create a Webhook to send data through

			server.pluginManager.registerEvents(EventListeners(connection, authUtility), instance)

			launch {
				try {
					val serverInfo = ServerInfo(
						server.name,
						server.version,
						server.motd,
						server.maxPlayers,
						server.ip,
						server.port,
						description.version,
					)
					connection.post("Server Online", "Log", serverInfo)
				} catch (e: IOException) {
					consoleLogWarning(e)
				}
			}
		}

		server.getPluginCommand("orizuru")!!.setExecutor(InfoCommand(this))
		memoryHolder.startCleanTask()

		SelfUpdate().onPluginLoad()
	}

	override fun onDisable() {
		// Stop all scheduled tasks
		server.scheduler.cancelTasks(this)
		val serverInfo = ServerInfo(
			server.name,
			server.version,
			server.motd,
			server.maxPlayers,
			server.ip,
			server.port,
			description.version,
		)
		try {
			connection.post("Server Shutdown", "Log", serverInfo)
		}catch (e: IOException) {
			consoleLogWarning("Failed to send shutdown webhook")
		}

	}

	companion object {
		val instance: Orizuru
			get() = getPlugin(Orizuru::class.java)
	}
}