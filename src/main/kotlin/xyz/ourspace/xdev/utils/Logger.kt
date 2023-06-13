package xyz.ourspace.xdev.utils

import org.bukkit.Bukkit
import org.bukkit.ChatColor

object Logger {
	private val PREFIX: String = "${ChatColor.LIGHT_PURPLE}[Orizuru]§r"
	fun consoleLog(message:String){
		Bukkit.getConsoleSender().sendMessage("$PREFIX $message")
	}
	fun consoleLogWarning(exception: Exception){
		Bukkit.getLogger().warning("$PREFIX §c${exception.message}")
	}
	fun consoleLogWarning(message:String){
		Bukkit.getLogger().warning("$PREFIX §c$message")
	}
	fun consoleLogError(exception: Exception){
		Bukkit.getLogger().severe("$PREFIX §c${exception.message}")
	}
	fun consoleLogError(message:String){
		Bukkit.getLogger().severe("[Orizuru] $message")
	}
}