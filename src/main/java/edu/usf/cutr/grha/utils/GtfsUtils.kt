package edu.usf.cutr.grha.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object GtfsUtils {
    /**
     * Function to convert timestamp to LocalDateTime object.
     * @param timeStamp String timestamp from the data
     * @param pattern Pattern of the data
     * @return returns LocalDateTime object
     */
    @JvmStatic
    fun getDateFromTimeStamp(timeStamp: String, pattern: String): LocalDateTime {
        val formatter = DateTimeFormatter.ofPattern(pattern)
        return LocalDateTime.parse(timeStamp, formatter)
    }

    @JvmStatic
    fun latLong(lat: Double, long: Double): String {
        return "$lat,$long"
    }

}