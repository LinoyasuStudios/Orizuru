package xyz.ourspace.xdev.utils


object StringTemplateReplacer {
	fun replace(text: String, replacements: Map<String, String>): String {
		var result = text
		for ((key, value) in replacements) {
			// Match {{ key }} and replace with value
			val pattern = "\\{\\{${key}}}"
			val keyRegex = Regex(pattern)
			result = result.replace(keyRegex, value)
		}
		return result
	}
	fun replaceSingle(text: String, regex:Regex, replacement:String): String {
		val result = StringBuilder(text)
		result.replace(regex, replacement)
		return result.toString()
	}
}