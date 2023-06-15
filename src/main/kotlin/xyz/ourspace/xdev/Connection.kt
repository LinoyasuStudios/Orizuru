package xyz.ourspace.xdev

import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import xyz.ourspace.xdev.utils.Logger


data class JSONObject(val content: String?, val content_type: String, val args: Any, val id: String)
data class HTTPResponse<T>(val status: Int, val content: T)

class APIConnection {
	private var gson: Gson = Gson()
	private lateinit var url: String
	private lateinit var password: String
	private lateinit var id: String
	var initialized: Boolean = false
	fun init(url: String, password: String, id: String) {
		this.url = url
		this.password = password
		this.id = id
		initialized = true
	}

	// Send the arguments as a JSON string to the webhook
	fun post(content: String, content_type: String, args: Any) {
		if (!initialized) {
			Logger.consoleLogWarning("APIConnection not initialized")
			return
		}
		val obj = JSONObject(content, content_type, args, id)
		val json = gson.toJson(obj).toString()
		Fuel.post(url).header("Content-Type" to "application/json")
				.header("Authorization" to password)
				.header("User-Agent" to "Orizuru Plugin")
				.header("Content-Length" to content.length.toString())
				.timeout(5000)
				.body(json, Charsets.UTF_8)
	}

	fun <T> postWithResponse(content_type: String, args: Any, responseClass: Class<T>?): HTTPResponse<T?> {
		if (!initialized) {
			Logger.consoleLogWarning("APIConnection not initialized")
			return HTTPResponse(0, null)
		}
		val obj = JSONObject(null, content_type, args, id)
		val json = gson.toJson(obj).toString()
		var httpResponse: HTTPResponse<T?>? = null
		runBlocking {

			val response = Fuel.post(url)
					.header("Content-Type" to "application/json")
					.header("Authorization" to password)
					.header("User-Agent" to "Orizuru Plugin")
					.header("Content-Length" to json.length.toString())
					.timeout(5000)
					.body(json, Charsets.UTF_8)
					.response()
			val (data, error) = response.third
			if (error != null) {
				Logger.consoleLogWarning("Failed to send webhook of ContentType $content_type: $error")
			}
			if (data == null) {
				Logger.consoleLogWarning("Failed to send webhook of ContentType $content_type: $error")
			} else {
				httpResponse = HTTPResponse(response.second.statusCode, gson.fromJson(data.toString(Charsets.UTF_8), responseClass))
			}
		}
		return httpResponse ?: HTTPResponse(0, null)
	}

	fun <T> get(content_type: String, args: Any, responseClass: Class<T>?): HTTPResponse<T?> {
		if (!initialized) {
			Logger.consoleLogWarning("APIConnection not initialized")
			return HTTPResponse(0, null)
		}
		val obj = JSONObject(null, content_type, args, id)
		val json = gson.toJson(obj).toString()
		var httpResponse: HTTPResponse<T?>? = null

		runBlocking {
			val response = Fuel.get(url).header("Content-Type" to "application/json").header("Authorization" to password)
					.header("User-Agent" to "Orizuru Plugin")
					.header("Content-Length" to json.length.toString())
					.timeout(5000)
					.body(json, Charsets.UTF_8)
					.response()
			val (data, error) = response.third
			if (error != null) {
				Logger.consoleLogWarning("Failed to send webhook of ContentType $content_type: $error")
			}
			if (data == null) {
				Logger.consoleLogWarning("Failed to send webhook of ContentType $content_type: $error")
			} else {
				httpResponse = HTTPResponse(response.second.statusCode, gson.fromJson(data.toString(Charsets.UTF_8), responseClass))
			}
		}
		return httpResponse ?: HTTPResponse(0, null)
	}
}