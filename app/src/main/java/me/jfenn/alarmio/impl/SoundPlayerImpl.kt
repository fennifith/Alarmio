package me.jfenn.alarmio.impl

import android.content.Context
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import me.jfenn.alarmio.data.SoundData
import me.jfenn.alarmio.interfaces.SoundPlayer

const val TYPE_RINGTONE = "ringtone"
const val TYPE_RADIO = "radio"

class SoundPlayerImpl(
        val context: Context
) : SoundPlayer, Player.EventListener {

    private val audioManager: AudioManager? = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private var audioInitialVolume: Int = 0
    private var audioMinVolume: Int = 0

    private val player: SimpleExoPlayer = SimpleExoPlayer.Builder(context).build()
    private val playerSourceFactory: DefaultDataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(context, "exoplayer2example"), null)
    private val playerHLSFactory: HlsMediaSource.Factory = HlsMediaSource.Factory(playerSourceFactory)
    private val playerProgressiveFactory: ProgressiveMediaSource.Factory = ProgressiveMediaSource.Factory(playerSourceFactory)
    private var playerStream: String? = null

    private var ringtone: Ringtone? = null

    init {
        player.addListener(this)
        player.audioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_ALARM)
                .build()

        audioManager?.let { audioManager ->
            audioInitialVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)

            if (Build.VERSION.SDK_INT >= 28)
                audioMinVolume = audioManager.getStreamMinVolume(AudioManager.STREAM_ALARM)
        }
    }

    /**
     * Play the passed sound object.
     *
     * @param sound         Information about the sound to be played.
     */
    override fun play(sound: SoundData) {
        if (isPlaying(sound)) return;
        stop()

        when (sound.type) {
            TYPE_RINGTONE -> getRingtone(sound.url)?.let { playRingtone(it) }
            TYPE_RADIO -> playStream(sound.url)
            else -> throw RuntimeException("Can't play sound type: '${sound.type}'.")
        }
    }

    /**
     * Determine if the passed sound matches the sound that is currently playing.
     *
     * @param sound         The sound to match the current sound to.
     * @return              True if the URL matches that of the currently playing
     *                      sound.
     */
    override fun isPlaying(sound: SoundData?): Boolean {
        return when (sound?.type) {
            TYPE_RINGTONE -> ringtone?.isPlaying == true
            TYPE_RADIO -> playerStream == sound.url
            else -> ringtone?.isPlaying == true || playerStream != null
        }
    }

    /**
     * Stop the currently playing sound, regardless of whether it is a ringtone
     * or a stream.
     */
    override fun stop() {
        player.stop()
        playerStream = null
        ringtone?.stop()
        ringtone = null
    }

    /**
     * Set the current volume level.
     */
    override fun setVolume(volumeLevel: Float) {
        if (Build.VERSION.SDK_INT >= 28) {
            ringtone?.apply {
                volume = volumeLevel
                return
            }
        }

        if (playerStream != null) {
            player.volume = volumeLevel
            return
        }

        audioManager?.setStreamVolume(
                AudioManager.STREAM_ALARM,
                (audioMinVolume + (volumeLevel * (audioInitialVolume - audioMinVolume))).toInt(),
                0
        )
    }

    /**
     * Play a stream ringtone.
     *
     * @param url           The URL of the stream to be passed to ExoPlayer.
     * @see [ExoPlayer Repo](https://github.com/google/ExoPlayer)
     */
    fun playStream(url: String, factory: MediaSourceFactory = playerHLSFactory) {
        player.prepare(factory.createMediaSource(Uri.parse(url)))
        player.playWhenReady = true

        playerStream = url
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when (playbackState) {
            Player.STATE_BUFFERING -> return
            Player.STATE_READY -> return
            Player.STATE_IDLE -> return // We are idle while switching from HLS to Progressive streaming
            else -> playerStream = null
        }
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        val lastStream = playerStream
        playerStream = null

        val exception = when (error.type) {
            ExoPlaybackException.TYPE_RENDERER -> error.rendererException
            ExoPlaybackException.TYPE_SOURCE -> {
                if (lastStream != null && error.sourceException.message?.contains("does not start with the #EXTM3U header") == true) {
                    playStream(lastStream, playerProgressiveFactory)
                    return
                }

                error.sourceException
            }
            ExoPlaybackException.TYPE_UNEXPECTED -> error.unexpectedException
            else -> return
        }

        exception.printStackTrace()
        Toast.makeText(context, "${exception.javaClass.name}: ${exception.message}", Toast.LENGTH_SHORT).show()
    }

    /**
     * Get a Ringtone object from a URI string using RingtoneManager.
     *
     * @param uri           The ringtone URI to obtain.
     * @see [RingtoneManager documentation](https://developer.android.com/reference/android/media/RingtoneManager.html)
     */
    fun getRingtone(uri: String): Ringtone? {
        if (!uri.startsWith("content://"))
            return null

        val ringtone = RingtoneManager.getRingtone(context, Uri.parse(uri))
        if (Build.VERSION.SDK_INT >= 21) {
            ringtone.audioAttributes = android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                    .build()
        }

        return ringtone
    }

    /**
     * Play a ringtone URI.
     *
     * @param ringtone      The ringtone to play.
     */
    fun playRingtone(ringtone: Ringtone) {
        if (!ringtone.isPlaying)
            ringtone.play()

        this.ringtone = ringtone
    }

}