package me.jfenn.alarmio.interfaces

import me.jfenn.alarmio.data.SoundData

interface SoundPlayer {

    fun play(sound: SoundData)

    fun isPlaying(sound: SoundData? = null): Boolean

    fun stop()

    fun setVolume(volumeLevel: Float)

}