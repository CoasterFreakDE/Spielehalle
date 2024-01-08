package one.devsky.spielehalle

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag
import one.devsky.spielehalle.extensions.getLogger
import one.devsky.spielehalle.modules.banner.BannerChanger
import one.devsky.spielehalle.modules.stats.VoiceListener
import one.devsky.spielehalle.modules.status.StatusChanger
import one.devsky.spielehalle.utils.DatabaseConnection
import one.devsky.spielehalle.utils.Environment
import one.devsky.spielehalle.utils.FeatureSystem
import one.devsky.spielehalle.utils.FeatureSystem.registerAll
import one.devsky.spielehalle.utils.FeatureSystem.registerCommands

class Spielehalle {

    companion object {
        lateinit var instance: Spielehalle
    }

    /**
     * Represents the JDA instance for the Discord bot.
     * This property is defined in the class `Spielehalle` and is used to interact with the Discord API.
     *
     * @see Spielehalle
     */
    val jda: JDA


    init {
        instance = this


        getLogger().info("Starting Spielehalle Bot Service (JDA)...")

        jda = JDABuilder.createDefault(Environment.getEnv("TOKEN") ?: error("No token provided"))
            .setEnabledIntents(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS))
            .enableCache(CacheFlag.entries)
            .registerAll()
            .build()
            .awaitReady()
            .also {
                it.presence.setPresence(
                    OnlineStatus.IDLE,
                    Activity.customStatus("☕️ Ich hol mir noch kurz nen Kaffee...")
                )
            }
            .registerCommands()

        DatabaseConnection.connect()

        val statusChanger = StatusChanger(jda)
        val bannerChanger = BannerChanger(jda)

        Runtime.getRuntime().addShutdownHook(Thread {
            getLogger().info("Shutting down...")
            bannerChanger.stop()
            statusChanger.stop()
            VoiceListener.onShutdown()
            jda.shutdown()
            DatabaseConnection.disconnect()
        }.apply { isDaemon = true })

        jda.presence.setPresence(OnlineStatus.ONLINE, Activity.customStatus("🍀 Guten Morgen/Tag/Abend!"))
        getLogger().info("Bot online. ${jda.selfUser.name} - ${jda.selfUser.id} on ${jda.guilds.size} guilds")
    }

}