package com.hironytic.moltonfkmp.story

/**
 * Hour part of a [Talk.time] value (milliseconds since midnight).
 */
fun hourPartOf(time: Int): Int = time / 3_600_000

/**
 * Minute part of a [Talk.time] value (milliseconds since midnight).
 */
fun minutePartOf(time: Int): Int = (time / 60_000) % 60

/**
 * Formats a [Talk.time] value (milliseconds since midnight) as "HH:MM".
 */
fun timeString(time: Int): String {
    val hour = hourPartOf(time).toString().padStart(2, '0')
    val minute = minutePartOf(time).toString().padStart(2, '0')
    return "$hour:$minute"
}
