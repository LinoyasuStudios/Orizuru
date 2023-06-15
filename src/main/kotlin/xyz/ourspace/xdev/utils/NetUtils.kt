package xyz.ourspace.xdev.utils

import com.github.kittinunf.fuel.Fuel

class NetUtils {
	companion object {
		fun getExternalIp(): String {
			val response = Fuel.get("https://api.ipify.org").responseString()
			val (data, error) = response.third
			if (error != null) {
				Logger.consoleLogWarning("Failed to fetch external IP")
				return ""
			}
			if (data == null) {
				Logger.consoleLogWarning("Failed to fetch external IP")
				return ""
			}
			return data
		}
	}
}