package org.jetbrains.research.tasktracker.ui

import org.jetbrains.research.tasktracker.ui.statusbar.TimerStatusBarWidget.Companion.SECONDS_IN_MINUTE

fun getTimeText(time: Long): String {
    val minutes = time / SECONDS_IN_MINUTE
    val seconds = time % SECONDS_IN_MINUTE
    return String.format("%02d:%02d", minutes, seconds)
}
