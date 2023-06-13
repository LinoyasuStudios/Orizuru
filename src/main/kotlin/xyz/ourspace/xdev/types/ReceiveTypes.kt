package xyz.ourspace.xdev.types

import xyz.ourspace.xdev.utils.GenericData

class AuthContent(
		val err: Boolean?,
		val body: AuthBody?,
		val code: String?
)
class AuthBody (
		val player: PlayerInfo?,
		var name: String?,
		val identifier : String?
)

class UserData(val username :String, val identifier: String, val name: String) : GenericData()