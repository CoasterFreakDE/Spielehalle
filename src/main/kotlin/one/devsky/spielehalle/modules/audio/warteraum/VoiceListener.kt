package one.devsky.spielehalle.modules.audio.warteraum

import dev.fruxz.ascend.extension.getResource
import dev.fruxz.ascend.tool.time.calendar.Calendar
import net.dv8tion.jda.api.audio.SpeakingMode
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Widget.VoiceChannel
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import one.devsky.spielehalle.Spielehalle
import one.devsky.spielehalle.db.cache.users.CasinoUserCache
import one.devsky.spielehalle.extensions.getLogger
import one.devsky.spielehalle.modules.audio.AudioPlayerManager
import one.devsky.spielehalle.modules.audio.AudioPlayerSendHandler
import one.devsky.spielehalle.utils.Environment
import java.io.File

class VoiceListener : ListenerAdapter() {


    override fun onGuildVoiceUpdate(event: GuildVoiceUpdateEvent): Unit = with(event) {
        if (member.user.isBot) return
        if (channelJoined == null && channelLeft == null) return

        if (channelLeft != null) onLeaveVoice(channelLeft!!)
        if (channelJoined != null) onJoinVoice(channelJoined!!)
    }

    private fun onJoinVoice(channel: AudioChannelUnion) {
        if (channel.members.isEmpty()) return
        if (channel.id != Environment.getEnv("CHANNEL_WARTERAUM")) return

        val audioManager = channel.guild.audioManager

        if (audioManager.isConnected) return

        audioManager.setSpeakingMode(SpeakingMode.SOUNDSHARE)
        audioManager.sendingHandler = AudioPlayerSendHandler(AudioPlayerManager.audioPlayer)
        audioManager.openAudioConnection(channel)

        AudioPlayerManager.playTrack(File("data/sounds/FlashWarteraum.wav").path)
    }

    private fun onLeaveVoice(channel: AudioChannelUnion) {
        if (channel.members.size > 1) return
        if (channel.id != Environment.getEnv("CHANNEL_WARTERAUM")) return
        channel.guild.audioManager.closeAudioConnection()
    }

    override fun onGuildReady(event: GuildReadyEvent) {
        if (event.guild.id != Environment.getEnv("MAIN_GUILD")) return
        for (member in event.guild.members) {
            if (member.user.isBot) continue
            if (member.voiceState?.channel == null) continue

            onJoinVoice(member.voiceState?.channel!!)
        }
    }
}