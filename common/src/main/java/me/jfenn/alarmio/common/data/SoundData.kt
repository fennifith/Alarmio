package me.jfenn.alarmio.common.data

import java.io.Serializable

data class SoundData(
        val name: String,
        val type: String,
        val url: String
): Serializable {

    /**
     * Returns an identifier string that can be used to recreate this
     * SoundDate class.
     *
     * @return                  A non-null identifier string.
     */
    override fun toString(): String {
        return "$name:SoundData:$type:SoundData:$url"
    }

    /**
     * Decide if two SoundDatas are equal.
     *
     * @param other             The object to compare to.
     * @return                  True if the SoundDatas contain the same sound.
     */
    override fun equals(other: Any?): Boolean {
        return (other as? SoundData)?.let {
            url == it.url
        } ?: super.equals(other)
    }

    companion object {

        /**
         * Construct a new instance of SoundData from an identifier string which was
         * (hopefully) created by [toString](#tostring).
         *
         * @param str               A non-null identifier string.
         * @return                  A recreated SoundData instance.
         */
        fun fromString(str: String): SoundData? {
            val data = str.split(":SoundData:")
            return if (data.size == 3) {
                SoundData(data[0], data[1], data[2])
            } else null
        }

    }
}