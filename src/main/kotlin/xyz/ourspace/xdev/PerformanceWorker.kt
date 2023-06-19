package xyz.ourspace.xdev

import me.lucko.spark.api.Spark
import me.lucko.spark.api.SparkProvider
import me.lucko.spark.api.statistic.StatisticWindow
import me.lucko.spark.api.statistic.StatisticWindow.TicksPerSecond
import me.lucko.spark.api.statistic.types.DoubleStatistic
import org.bukkit.Bukkit
import xyz.ourspace.xdev.types.PerformanceData
import xyz.ourspace.xdev.types.ServerPlayerStats
import xyz.ourspace.xdev.utils.Logger


class PerformanceWorker {
	companion object {
		fun start() {
			var spark: Spark? = null
			runCatching {
				spark = SparkProvider.get()
			}.onFailure {
				Logger.consoleLog("Spark is not installed, performance metrics will not be posted")
				return
			}
			if (spark == null) {
				Logger.consoleLog("Spark is not installed, performance metrics will not be posted")
				return
			}
			val intervalMinutes = Orizuru.instance.config.getLong("metrics.interval")
			val tpsI: DoubleStatistic<TicksPerSecond> = spark!!.tps()!!
			val cpuUsageI = spark!!.cpuProcess()
			Bukkit.getScheduler().scheduleSyncRepeatingTask(Orizuru.instance, Runnable {
				val tps = tpsI.poll()
				val memory = Runtime.getRuntime().freeMemory() / 1024 / 1024
				val maxMemory = Runtime.getRuntime().maxMemory() / 1024 / 1024
				val usedMemory = maxMemory - memory
				val memPercent = usedMemory * 100 / maxMemory
				val cpuUsage: Double = cpuUsageI.poll(StatisticWindow.CpuUsage.MINUTES_1)
				val playerStats = ServerPlayerStats(
						Bukkit.getOnlinePlayers().size,
						Bukkit.getMaxPlayers()
				)
				val data = PerformanceData(
						playerStats,
						tps,
						memory,
						maxMemory,
						usedMemory,
						memPercent,
						cpuUsage,
				)
				Orizuru.instance.connection.postAsync(
						"Performance Metrics for the last $intervalMinutes minutes",
						OrizContentType.PERFORMANCE,
						data
				)
				//Logger.consoleLog("Posted performance metrics")
				// Wait 20 seconds before first run, then run every intervalMinutes minutes
			}, 20 * 20, intervalMinutes * 20 * 60)

		}
	}
}