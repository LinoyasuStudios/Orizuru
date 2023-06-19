package xyz.ourspace.xdev

import org.bukkit.Bukkit
import xyz.ourspace.xdev.types.PerformanceData
import xyz.ourspace.xdev.utils.TPSUtil
import java.lang.management.ManagementFactory


class PerformanceWorker {
	companion object {
		fun start() {
			val intervalMinutes = Orizuru.instance.config.getLong("metrics.interval")
			Bukkit.getScheduler().scheduleSyncRepeatingTask(Orizuru.instance, Runnable {
				val tps = TPSUtil.getRecentTps()
				val memory = Runtime.getRuntime().freeMemory() / 1024 / 1024
				val maxMemory = Runtime.getRuntime().maxMemory() / 1024 / 1024
				val usedMemory = maxMemory - memory
				val memPercent = usedMemory * 100 / maxMemory
				val cpuCount = Runtime.getRuntime().availableProcessors()
				val cpuUsage = ManagementFactory.getOperatingSystemMXBean().systemLoadAverage / cpuCount
				val data = PerformanceData(
						tps,
						memory,
						maxMemory,
						usedMemory,
						memPercent,
						cpuUsage.toLong()
				)
				Orizuru.instance.connection.postAsync(
						"Performance Metrics for the last $intervalMinutes minutes",
						OrizContentType.PERFORMANCE,
						data
				)
				// Wait 20 seconds before first run, then run every intervalMinutes minutes
			}, 20 * 20, intervalMinutes * 20 * 60)
		}
	}
}