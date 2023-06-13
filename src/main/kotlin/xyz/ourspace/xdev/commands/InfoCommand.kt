package xyz.ourspace.xdev.commands

import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import xyz.ourspace.xdev.Orizuru

class InfoCommand(private val plugin: Orizuru) : CommandExecutor {
	override fun onCommand(sender: CommandSender, command: Command, s: String, strings: Array<String>): Boolean {
		if (sender is Player) {
			val branding = ChatColor.LIGHT_PURPLE.toString() + "Space" + ChatColor.AQUA + "Project"
			sender.sendMessage(
					"""
	${ChatColor.LIGHT_PURPLE}| NetMonAuth Plugin
	| Version: ${ChatColor.AQUA}${plugin.description.version}${ChatColor.GREEN}
	| With Resources from $branding${ChatColor.YELLOW}
	""".trimIndent()
			)
		} else {
			plugin.logger.info(
					"""
	|NetMonAuth Plugin
	|Version: ${plugin.description.version}
	|Plugin Correctly Loaded
	""".trimIndent()
			)
		}
		return true
	}
}