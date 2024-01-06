package one.devsky.spielehalle.modules.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers

object AudioPlayerManager {

    private val playerManager = DefaultAudioPlayerManager()
    val audioPlayer: AudioPlayer

    init {
        AudioSourceManagers.registerLocalSource(playerManager)
        audioPlayer = playerManager.createPlayer()
    }

    fun playTrack(track: String) {
        playerManager.loadItem(track, LoadAndPlayHandler())
    }
}