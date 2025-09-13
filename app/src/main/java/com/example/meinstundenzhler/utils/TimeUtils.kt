package com.example.meinstundenzhler.utils

fun computeDurationMinutes(startMs: Long, endMs: Long, breakMin: Int): Int {
    val raw = ((endMs - startMs) / 60_000L).toInt()
    return (raw - breakMin).coerceAtLeast(0)
}

fun formatHours(totalMin: Int): String {
    val h = totalMin / 60
    val m = totalMin % 60
    return "%d:%02d".format(h, m)
}

fun monthName(index: Int): String = listOf(
    "Januar","Februar","März","April","Mai","Juni",
    "Juli","August","September","Oktober","November","Dezember"
)[index.coerceIn(0, 11)]
