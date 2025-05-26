package org.jetbrains.research.tasktracker.ui.statusbar

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.util.Consumer
import java.awt.Component
import java.awt.event.MouseEvent

class TimerStatusBarWidget(private val project: Project) : StatusBarWidget, StatusBarWidget.TextPresentation {
    companion object {
        const val ID = "TaskTrackerTimer"
    }

    private var timeRemaining: Long? = null
    private var statusBar: StatusBar? = null

    override fun ID(): String = ID

    override fun getPresentation(): StatusBarWidget.WidgetPresentation = this

    override fun getText(): String {
        val time = timeRemaining ?: return ""
        val minutes = time / 60
        val seconds = time % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun getTooltipText(): String = "Time remaining for current task"

    override fun getClickConsumer(): Consumer<MouseEvent>? = null

    override fun getAlignment(): Float = Component.CENTER_ALIGNMENT

    override fun dispose() {
        statusBar = null
    }

    override fun install(statusBar: StatusBar) {
        this.statusBar = statusBar
    }

    fun stopTime() {
        timeRemaining = null
        statusBar?.updateWidget(ID)
    }

    fun updateTime(seconds: Long) {
        timeRemaining = seconds
        statusBar?.updateWidget(ID)
    }
}