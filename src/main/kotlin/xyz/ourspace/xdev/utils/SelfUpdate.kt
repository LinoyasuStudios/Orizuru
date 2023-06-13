package xyz.ourspace.xdev.utils

import khttp.get
import xyz.ourspace.xdev.Orizuru
import java.io.File


class SelfUpdate {
	// This class lets us get the latest version of the plugin from GitHub
	private val versionRegex = Regex("""(\d+)\.(\d+)\.(\d+)(?:-(\w+))?""")
	private val filenameRegex = Regex("""Orizuru-(\d+)\.(\d+)\.(\d+)(?:-(\w+))?.jar""")
	private val gitRepo = "GaryCraft/Orizuru"
	private fun getCurrentVersion(): Version {
		val v = Orizuru.instance.description.version
		// Error out if the version is not in the correct format
		if (!versionRegex.matches(v)) {
			throw Exception("Version is not in the correct format")
		}
		val (major, minor, patch, tag) = versionRegex.find(v)!!.destructured
		return Version(major.toInt(), minor.toInt(), patch.toInt(), tag)
	}

	private fun getLatestVersion(): Version {
		// Fetch GitHub API for latest version
		val url = "https://api.github.com/repos/$gitRepo/releases/latest"
		val response = get(url)
		// Error out if the version is not in the correct format
		if (!response.jsonObject.has("tag_name")) {
			throw Exception("Version is not in the correct format")
		}
		val v = response.jsonObject.getString("tag_name")
		if (!versionRegex.matches(v)) {
			throw Exception("Version is not in the correct format")
		}
		val (major, minor, patch, tag) = versionRegex.find(v)!!.destructured
		return Version(major.toInt(), minor.toInt(), patch.toInt(), tag)
	}

	private fun getLatestVersionUrl(): String {
		val url = "https://api.github.com/repos/$gitRepo/releases/latest"
		val response = get(url)
		// Error out if the version is not in the correct format
		if (!response.jsonObject.has("assets")) {
			throw Exception("Version is not in the correct format")
		}
		val assets = response.jsonObject.getJSONArray("assets")
		for (i in 0 until assets.length()) {
			val asset = assets.getJSONObject(i)
			// Match filename to jar file
			if (asset.getString("name")
							.matches(filenameRegex)) {
				return asset.getString("browser_download_url")
			}
		}
		throw Exception("Version is unavailable")
	}
	private fun downloadUpdate() {
		// Get latest version url
		val url = getLatestVersionUrl()
		// Download file
		val response = get(url)
		val file = File("plugins/orizuru.jar")
		file.writeBytes(response.content)
	}

	fun onPluginLoad() {
		Logger.consoleLog("Checking for updates...")
		val currentVersion = getCurrentVersion()
		val latestVersion = getLatestVersion()
		if (currentVersion < latestVersion) {
			Logger.consoleLog("""Update found!
				|Current version: $currentVersion
				|Latest version: $latestVersion
				|""".trimMargin())
			// Download update
			downloadUpdate()
			Logger.consoleLog("Update downloaded, please restart the server to apply the update")
		}
	}
}

data class Version(val major: Int, val minor: Int, val patch: Int, val tag: String?) {
	override fun toString(): String {
		return "$major.$minor.$patch${if (tag != null) "-$tag" else ""}"
	}

	operator fun compareTo(other: Version): Int {
		if (major != other.major) {
			return major - other.major
		}
		if (minor != other.minor) {
			return minor - other.minor
		}
		if (patch != other.patch) {
			return patch - other.patch
		}
		if (tag != other.tag) {
			if (tag == null) {
				return 1
			}
			if (other.tag == null) {
				return -1
			}
			return tag.compareTo(other.tag)
		}
		return 0
	}
}
