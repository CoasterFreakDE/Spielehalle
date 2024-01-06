package one.devsky.spielehalle.modules.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame
import net.dv8tion.jda.api.audio.AudioSendHandler
import java.nio.ByteBuffer

/**
 * The AudioPlayerSendHandler class is responsible for providing audio data from the
 * audio player to be sent to a Discord voice channel.
 *
 * @property audioPlayer The audio player that provides the audio frames.
 * @property lastFrame The last audio frame provided by the audio player.
 */
class AudioPlayerSendHandler(private val audioPlayer: AudioPlayer) : AudioSendHandler {
    private var lastFrame: AudioFrame? = null

    override fun canProvide(): Boolean {
        lastFrame = audioPlayer.provide()
        return lastFrame != null
    }

    override fun provide20MsAudio(): ByteBuffer? {
        return ByteBuffer.wrap(lastFrame!!.data)
    }

    override fun isOpus(): Boolean {
        return true
    }
}