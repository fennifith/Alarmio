package me.jfenn.alarmio.common.data.interfaces

import me.jfenn.alarmio.common.data.SoundData

interface AlertData {

    var isVibrate: Boolean
    var sound: SoundData?

}