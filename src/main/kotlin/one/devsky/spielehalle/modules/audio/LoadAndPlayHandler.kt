package one.devsky.spielehalle.modules.audio

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack

class LoadAndPlayHandler : AudioLoadResultHandler {
    override fun trackLoaded(audioTrack: AudioTrack) {
        AudioPlayerManager.audioPlayer.playTrack(audioTrack)
    }

    override fun playlistLoaded(audioPlaylist: AudioPlaylist) {
        audioPlaylist.tracks.forEach { AudioPlayerManager.audioPlayer.playTrack(it) }
    }

    override fun noMatches() {
        error("No matches found.")
    }

    override fun loadFailed(friendlyException: FriendlyException) {
        friendlyException.message?.let { error(it) } ?: error("An unknown error occurred.")
    }
}