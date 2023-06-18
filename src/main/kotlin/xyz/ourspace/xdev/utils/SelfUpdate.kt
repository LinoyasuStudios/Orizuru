package xyz.ourspace.xdev.utils

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.jackson.responseObject
import kotlinx.coroutines.runBlocking
import org.bukkit.Bukkit
import xyz.ourspace.xdev.Orizuru
import java.io.File


class SelfUpdate {
	// This class lets us get the latest version of the plugin from GitHub
	private val versionRegex = Regex("""(\d+)\.(\d+)\.(\d+)(?:-(\w+))?""")
	private val filenameRegex = Regex("""orizuru-(\d+)\.(\d+)\.(\d+)(?:-(\w+))?.jar""")
	private val gitRepo = "GaryCraft/Orizuru"
	private fun getCurrentVersion(): Version {
		val v = Orizuru.instance.description.version
		return Version.fromString(v)
	}

	private fun getLatestVersion(): Version {
		// Fetch GitHub API for latest version
		val url = "https://api.github.com/repos/$gitRepo/releases/latest"
		var version = Version(0, 0, 0, "")
		runBlocking {
			val response = Fuel.get(url).responseObject<GitHubAPIRelease>()
			val (data, error) = response.third
			if (error != null) {
				throw Exception("Failed to fetch latest version from GitHub API")
			}
			if (data == null) {
				throw Exception("Failed to fetch latest version from GitHub API")
			}
			val v = data.tag_name
			version = Version.fromString(v)
			if (version.isEquivalent(Version(0, 0, 0, ""))) {
				throw Exception("Failed to fetch latest version from GitHub API")
			}
		}
		return version
	}

	private fun getLatestVersionUrl(): String {
		val url = "https://api.github.com/repos/$gitRepo/releases/latest"
		var version: Version;
		var downloadUrl = "";
		runBlocking {
			val response = Fuel.get(url).responseObject<GitHubAPIRelease>()
			val (data, error) = response.third
			if (error != null) {
				throw Exception("Failed to fetch latest version from GitHub API")
			}
			if (data == null) {
				throw Exception("Failed to fetch latest version from GitHub API")
			}
			val v = data.tag_name
			version = Version.fromString(v)
			downloadUrl = data.assets.firstOrNull { it.name.matches(filenameRegex) }?.browser_download_url ?: ""
			if (version.isEquivalent(Version(0, 0, 0, ""))) {
				throw Exception("Failed to fetch latest version from GitHub API")
			}
			if (downloadUrl.isEmpty()) {
				throw Exception("Failed to fetch latest version from GitHub API")
			}
		}
		return downloadUrl
	}

	private fun downloadUpdateDeferred() {
		// Get latest version url
		val url = getLatestVersionUrl()
		// Download file
		Bukkit.getScheduler().runTaskAsynchronously(Orizuru.instance, Runnable {
			Logger.consoleLog("Downloading update...")
			val response = Fuel.download(url).response()
			val file = File("plugins/update/orizuru.jar")
			file.writeBytes(response.third.get())
			Logger.consoleLog("Update downloaded, please restart the server to apply the update")
		})
	}

	fun runSelfUpdate() {
		Logger.consoleLog("Checking for updates...")
		val currentVersion = getCurrentVersion()
		val latestVersion = getLatestVersion()
		if (currentVersion < latestVersion) {
			Logger.consoleLog("""Update found!
				|Current version: $currentVersion
				|Latest version: $latestVersion
				|""".trimMargin())
			// Download update
			downloadUpdateDeferred()
		} else {
			Logger.consoleLog("""
				|No updates found
				|Current version: $currentVersion
				|Latest version: $latestVersion
			""".trimIndent())
		}
	}
	companion object {
		fun isUpdateDownloaded(): Boolean {
			val file = File("plugins/update/orizuru.jar")
			return file.exists()
		}
		fun moveUpdatetoSelf() {
			val file = File("plugins/update/orizuru.jar")
			val self = File("plugins/orizuru.jar")
			file.copyTo(self, true)
			file.delete()
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

	fun isEquivalent(other: Version): Boolean {
		return major == other.major && minor == other.minor && patch == other.patch
	}

	companion object {
		fun fromString(s: String): Version {
			val versionRegex = Regex("""(\d+)\.(\d+)\.(\d+)(?:-(\w+))?""")
			// Error out if the version is not in the correct format
			if (!versionRegex.matches(s)) {
				throw Exception("Version is not in the correct format")
			}
			val (major, minor, patch, tag) = versionRegex.find(s)!!.destructured
			return Version(major.toInt(), minor.toInt(), patch.toInt(), tag)
		}
	}
}

data class GitHubAPIAsset(
		var name: String = "",
		var browser_download_url: String = "",
		var content_type: String = "",
		var size: Int = 0,
		var created_at: String = "",
		var updated_at: String = "",
		var label: String = "",
		var state: String = "",
		var download_count: Int = 0,
)

data class GitHubAPIRelease(
		var url: String = "",
		var html_url: String = "",
		var assets_url: String = "",
		var upload_url: String = "",
		var id: Int = 0,
		var tag_name: String = "",
		var assets: List<GitHubAPIAsset> = listOf(),
		var draft: Boolean = false,
		var prerelease: Boolean = false,
		var created_at: String = "",
		var published_at: String = "",
		var body: String = "",
)