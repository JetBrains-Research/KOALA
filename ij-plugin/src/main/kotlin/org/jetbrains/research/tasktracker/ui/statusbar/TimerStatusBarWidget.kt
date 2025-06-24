package org.jetbrains.research.tasktracker.ui.statusbar

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.util.Consumer
import org.jetbrains.research.tasktracker.ui.getTimeText
import java.awt.Component
import java.awt.event.MouseEvent

class TimerStatusBarWidget(@Suppress("UnusedPrivateProperty") private val project: Project) :
    StatusBarWidget,
    StatusBarWidget.TextPresentation {
    companion object {
        const val ID = "TaskTrackerTimer"

        const val SECONDS_IN_MINUTE = 60
    }

    private var timeRemaining: Long? = null
    private var statusBar: StatusBar? = null

    override fun ID(): String = ID

    override fun getPresentation(): StatusBarWidget.WidgetPresentation = this

    @Suppress("ImplicitDefaultLocale")
    override fun getText(): String {
        val time = timeRemaining ?: return ""
        return getTimeText(time)
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
