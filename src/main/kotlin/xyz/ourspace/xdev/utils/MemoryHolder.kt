package xyz.ourspace.xdev.utils

import org.bukkit.Bukkit.getServer
import xyz.ourspace.xdev.Orizuru
import java.util.Optional

data class MemoryWrapper<T>(var value: T, val lifetime: Long = 0) {
    private var lastAccess: Long = System.currentTimeMillis()
    fun get(): T {
        lastAccess = System.currentTimeMillis()
        return value
    }

    fun isExpired(): Boolean {
        return lifetime > 0 && System.currentTimeMillis() - lastAccess > lifetime
    }
}
abstract class GenericData{

}
class MemoryHolder(private val plugin: Orizuru) {
    private var cleanTaskId : Int? = null
    fun startCleanTask() {
        val scheduler = getServer().scheduler
        cleanTaskId = scheduler.scheduleSyncRepeatingTask(plugin, Runnable {
            removeExpired()
        }, 0L, 200L)
    }
    private val memory = mutableMapOf<String, MutableMap<String, MemoryWrapper<GenericData>>>()
    fun <T:GenericData> set(key: String, value: T, lifetime: Long = 0) {
        val type = value::class.java.name
        val subMap = memory[type] ?: mutableMapOf()
        subMap[key] = MemoryWrapper(value, lifetime)
        memory[type] = subMap
    }
    fun <T:GenericData> get(key: String, type: Class<T>): Optional<T> {
        val subMap = memory[type.name] ?: return Optional.empty()
        val wrapper = subMap[key] ?: return Optional.empty()
        return Optional.of(wrapper.get() as T)
    }
    fun <T:GenericData> remove(key: String, type: Class<T>):Boolean {
        val subMap = memory[type.name] ?: return false
        return subMap.remove(key) != null
    }

    private fun removeExpired() {
        memory.forEach { (type, subMap) ->
            subMap.forEach { (key, wrapper) ->
                if (wrapper.isExpired()) {
                    subMap.remove(key)
                }
            }
        }
    }
}
