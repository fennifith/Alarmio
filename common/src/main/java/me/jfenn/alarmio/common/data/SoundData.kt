package me.jfenn.alarmio.common.data

data class SoundData(
        val name: String,
        val type: String,
        val url: String
) {

    /**
     * Returns an identifier string that can be used to recreate this
     * SoundDate class.
     *
     * @return                  A non-null identifier string.
     */
    override fun toString(): String {
        return "$name:$type:$url"
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
            val data = str.split(':')
            return if (data.size == 3) {
                SoundData(data[0], data[1], data[2])
            } else null
        }

    }
}