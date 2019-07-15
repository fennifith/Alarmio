package me.jfenn.alarmio.impl

import android.content.Context
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import me.jfenn.alarmio.common.data.SoundData
import me.jfenn.alarmio.common.interfaces.SoundPlayer

const val TYPE_RINGTONE = "ringtone"
const val TYPE_RADIO = "radio"

class SoundPlayerImpl(
        val context: Context
): SoundPlayer, Player.EventListener {

    private var player: SimpleExoPlayer = ExoPlayerFactory.newSimpleInstance(context, DefaultTrackSelector())
    private var hlsMediaSourceFactory: HlsMediaSource.Factory = HlsMediaSource.Factory(DefaultDataSourceFactory(context, Util.getUserAgent(context, context.packageName), null))
    private var currentStream: String? = null

    private var currentRingtone: Ringtone? = null

    init {
        player.addListener(this)
        player.audioAttributes = com.google.android.exoplayer2.audio.AudioAttributes.Builder()
                .setUsage(C.USAGE_ALARM)
                .build()
    }


    /**
     * Play the passed sound url.
     *
     * @param sound         Information about the sound to be played.
     */
    override fun play(sound: SoundData) {
        if (isPlaying(sound)) return
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
            TYPE_RINGTONE -> currentRingtone?.isPlaying == true
            TYPE_RADIO -> currentStream == sound.url
            else -> false
        }
    }

    /**
     * Stop the currently playing sound, regardless of whether it is a ringtone
     * or a stream.
     */
    override fun stop() {
        player.stop()
        currentRingtone?.stop()
        currentRingtone = null
    }

    /**
     * Play a stream ringtone.
     *
     * @param url           The URL of the stream to be passed to ExoPlayer.
     * @see [ExoPlayer Repo](https://github.com/google/ExoPlayer)
     */
    fun playStream(url: String) {
        player.prepare(hlsMediaSourceFactory.createMediaSource(Uri.parse(url)))
        player.playWhenReady = true

        currentStream = url
    }


    /**
     * Get a Ringtone object from a URI string using RingtoneManager.
     *
     * @param uri           The ringtone URI to obtain.
     * @see [RingtoneManager documentation](https://developer.android.com/reference/android/media/RingtoneManager.html)
     */
    fun getRingtone(uri: String): Ringtone? {
        val ringtone = RingtoneManager.getRingtone(context, Uri.parse(uri))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ringtone.audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
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

        currentRingtone = ringtone
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when (playbackState) {
            Player.STATE_BUFFERING -> return
            Player.STATE_READY -> return
            else -> currentStream = null
        }
    }

    override fun onPlayerError(error: ExoPlaybackException?) {
        currentStream = null

        val exception: Exception
        when (error?.type) {
            ExoPlaybackException.TYPE_RENDERER -> exception = error.rendererException
            ExoPlaybackException.TYPE_SOURCE -> exception = error.sourceException
            ExoPlaybackException.TYPE_UNEXPECTED -> exception = error.unexpectedException
            else -> return
        }

        exception.printStackTrace()
        Toast.makeText(context, "${exception.javaClass.name}: ${exception.message}", Toast.LENGTH_SHORT).show()
    }
}