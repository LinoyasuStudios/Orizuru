package xyz.ourspace.xdev

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.util.stream.Collectors
import xyz.ourspace.xdev.utils.Logger
import java.net.HttpURLConnection


data class JSONObject(val content:String?, val content_type:String, val args:Any, val id:String)
data class HTTPResponse<T>(val status:Int, val content: T)

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
	fun post(content:String, content_type:String, args:Any) {
		if (!initialized) {
			Logger.consoleLogWarning("APIConnection not initialized")
			return
		}
		runBlocking {
			val obj = JSONObject(content, content_type, args, id)
			val json = gson.toJson(obj).toString()
			//consoleLog("Sending webhook: $json")
			launch {
				val connection = withContext(Dispatchers.IO) {
					URL(url).openConnection()
				} as HttpURLConnection
			connection.apply {
				doOutput = true
				doInput = true
				requestMethod = "POST"
				connectTimeout = 5 * 1000;
				setRequestProperty("User-Agent", "NetMonAuth Plugin")
				setRequestProperty("Content-Type", "application/json")
				setRequestProperty("Content-Length", json.length.toString())
				setRequestProperty("Authorization", password)
				val outputStream = outputStream
				outputStream.write(json.toByteArray())
				outputStream.flush()
				outputStream.close()
				connect()
			}
			try {
				if (connection.responseCode != 200) {
					throw IOException("HTTP error code: ${connection.responseCode}, Response: ${connection.content}")
				}
			} finally {
				connection.disconnect()
			}
			}
		}
	}
	fun <T> postWithResponse(content_type: String, args: Any, responseClass: Class<T>?): HTTPResponse<T?> {
		if (!initialized) {
			Logger.consoleLogWarning("APIConnection not initialized")
			return HTTPResponse(0, null)
		}
		val obj = JSONObject(null,content_type, args, id)
		val json = gson.toJson(obj).toString()
		var responseBody: String
		val connection = URL(url).openConnection() as HttpURLConnection
		connection.requestMethod = "POST"
		connection.apply {
			doOutput = true
			doInput = true
			connectTimeout = 5 * 1000;
			setRequestProperty("User-Agent", "NetMonAuth Plugin")
			setRequestProperty("Content-Type", "application/json")
			setRequestProperty("Content-Length", json.length.toString())
			setRequestProperty("Authorization", password)
			val outputStream = outputStream
			outputStream.write(json.toByteArray())
			outputStream.flush()
			outputStream.close()
			val br:BufferedReader = if (connection.responseCode in 100..399) {
				BufferedReader(InputStreamReader(connection.inputStream))
			} else {
				BufferedReader(InputStreamReader(connection.errorStream))
			}
			responseBody = br.lines().collect(Collectors.joining())
			connect()
		}
		try {
			if (connection.responseCode != 200) {
				return HTTPResponse(connection.responseCode, gson.fromJson(responseBody, responseClass))
			}
		} finally {
			connection.disconnect()
		}
		return HTTPResponse(connection.responseCode, gson.fromJson(responseBody, responseClass))
	}

	fun <T> get(content_type: String, args: Any, responseClass: Class<T>?): HTTPResponse<T?> {
		if (!initialized) {
			Logger.consoleLogWarning("APIConnection not initialized")
			return HTTPResponse(0, null)
		}
		val obj = JSONObject(null,content_type, args, id)
		val json = gson.toJson(obj).toString()
		var responseBody: String
		val connection = URL(url).openConnection() as HttpURLConnection
		connection.requestMethod = "GET"
		connection.apply {
			doInput = true
			connectTimeout = 5 * 1000;
			setRequestProperty("User-Agent", "NetMonAuth Plugin")
			setRequestProperty("Content-Type", "application/json")
			setRequestProperty("Content-Length", json.length.toString())
			setRequestProperty("Authorization", password)
			val outputStream = outputStream
			outputStream.write(json.toByteArray())
			outputStream.flush()
			outputStream.close()
			val br:BufferedReader = if (connection.responseCode in 100..399) {
				BufferedReader(InputStreamReader(connection.inputStream))
			} else {
				BufferedReader(InputStreamReader(connection.errorStream))
			}
			responseBody = br.lines().collect(Collectors.joining())
			connect()
		}
		try {
			if (connection.responseCode != 200) {
				return HTTPResponse(connection.responseCode, gson.fromJson(responseBody, responseClass))
			}
		} finally {
			connection.disconnect()
		}
		return HTTPResponse(connection.responseCode, gson.fromJson(responseBody, responseClass))
	}
}