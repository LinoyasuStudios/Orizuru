package xyz.ourspace.xdev.utils

import org.bukkit.configuration.file.FileConfiguration

class ConfigValidator() {
	fun validate(config: FileConfiguration): Boolean {
		if (!this.validateAPI(config)) {
			return false
		}
		if (!this.validateAuth(config)) {
			return false
		}
		return true
	}
	private fun validateAPI(config: FileConfiguration): Boolean {
		if(!config.isConfigurationSection("api")) {
			Logger.consoleLogWarning("The config.yml is not valid, please check the config.yml file")
			return false
		}
		if(config.getString("api.url") == null) {
			Logger.consoleLogWarning("The config is invalid (Missing API IP or Domain), disabling the plugin")
			return false
		}
		if(config.getString("api.password") == null) {
			Logger.consoleLogWarning("The config is invalid (Missing API Password), disabling the plugin")
			return false
		}
		if(config.getString("api.uniqueId") == null) {
			Logger.consoleLogWarning("The config is invalid (Missing API UniqueId), disabling the plugin")
			return false
		}
		return true
	}
	private fun validateAuth(config: FileConfiguration): Boolean {
		if(!config.isConfigurationSection("auth")) {
			Logger.consoleLogWarning("The config.yml is not valid, missing auth section, please check the config.yml file")
			return false
		}
		if(!config.isConfigurationSection("auth.messages")) {
			Logger.consoleLogWarning("The config.yml is not valid, missing auth.messages section, please check the config.yml file")
			return false
		}
		if(config.getString("auth.messages.successMessage") == null) {
			Logger.consoleLogWarning("The config is invalid (Missing Auth SuccessMessage), disabling the plugin")
			return false
		}
		if(config.getString("auth.messages.failureMessage") == null) {
			Logger.consoleLogWarning("The config is invalid (Missing Auth FailureMessage), disabling the plugin")
			return false
		}
		return true
	}
}