package one.devsky.spielehalle.modules.stats

import dev.fruxz.ascend.tool.time.calendar.Calendar
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import one.devsky.spielehalle.db.cache.users.CasinoUserCache
import one.devsky.spielehalle.extensions.getLogger
import one.devsky.spielehalle.utils.Environment

class VoiceListener : ListenerAdapter() {

    companion object {
        private val joinTimes = mutableMapOf<String, Calendar>()

        fun onShutdown() {
            for ((member, joinTime) in joinTimes) {
                val time = Calendar.now().durationFrom(joinTime)
                val xp = time.inWholeMinutes.toInt()

                val casinoUser = CasinoUserCache.getUser(member)
                CasinoUserCache.saveUser(casinoUser.copy(voiceTime = casinoUser.voiceTime + time, xp = casinoUser.xp + xp))
                getLogger().debug("User $member has been in voice for $time gaining $xp xp.")
            }
        }
    }


    override fun onGuildVoiceUpdate(event: GuildVoiceUpdateEvent): Unit = with(event) {
        if (member.user.isBot) return
        if (channelJoined == null && channelLeft == null) return

        if (channelLeft != null) onLeaveVoice(member)
        if (channelJoined != null) onJoinVoice(member)
    }

    private fun onJoinVoice(member: Member) {
        joinTimes[member.id] = Calendar.now()
    }

    private fun onLeaveVoice(member: Member) {
        val joinTime = joinTimes[member.id] ?: return
        val time = Calendar.now().durationFrom(joinTime)

        val xp = time.inWholeMinutes.toInt()

        val casinoUser = CasinoUserCache.getUser(member.id)
        CasinoUserCache.saveUser(casinoUser.copy(voiceTime = casinoUser.voiceTime + time, xp = casinoUser.xp + xp))
        getLogger().debug("User ${member.user.name} has been in voice for $time gaining $xp xp.")
    }

    override fun onGuildReady(event: GuildReadyEvent) {
        if (event.guild.id != Environment.getEnv("MAIN_GUILD")) return
        for (member in event.guild.members) {
            if (member.user.isBot) continue
            if (member.voiceState?.channel == null) continue

            onJoinVoice(member)
        }
    }
}