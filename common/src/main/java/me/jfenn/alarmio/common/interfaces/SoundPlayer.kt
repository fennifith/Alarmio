package me.jfenn.alarmio.common.interfaces

import me.jfenn.alarmio.common.data.SoundData

interface SoundPlayer {

    fun play(sound: SoundData)

    fun isPlaying(sound: SoundData? = null): Boolean

    fun stop()

}