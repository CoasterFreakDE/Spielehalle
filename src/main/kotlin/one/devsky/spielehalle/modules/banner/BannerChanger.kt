package one.devsky.spielehalle.modules.banner

import dev.inmo.krontab.doInfinity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Icon
import one.devsky.spielehalle.extensions.getLogger
import one.devsky.spielehalle.utils.Environment
import one.devsky.spielehalle.utils.ImageUtils

class BannerChanger(private val jda: JDA) {

    private val job: Job

    init {
        job = CoroutineScope(Dispatchers.Default).launch {
            start()
        }
    }

    private suspend fun start() {
        val guild = Environment.getEnv("MAIN_GUILD")?.let { jda.getGuildById(it) } ?: return
        doInfinity("0 /10 * * *") {
            if (!guild.features.contains("BANNER")) {
                getLogger().info("Banner feature not enabled: ${guild.features.joinToString(", ")}")
                return@doInfinity
            }

            guild.findMembers { it.isBoosting }.onSuccess { boosters ->
                val avatars = boosters.mapNotNull { it.user.avatarUrl }

                try {
                    val image =
                        ImageUtils.generateBanner(guild, avatars)

                    guild.manager.setBanner(Icon.from(image)).queue()
                } catch (e: Exception) {
                    getLogger().error("Error while generating banner", e)
                }
            }.onError {
                getLogger().error("Error while fetching boosters", it)
            }
        }
    }

    fun stop() {
        job.cancel()
    }
}