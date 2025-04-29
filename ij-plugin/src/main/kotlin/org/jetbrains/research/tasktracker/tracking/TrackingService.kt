package org.jetbrains.research.tasktracker.tracking

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.research.tasktracker.TaskTrackerPlugin
import org.jetbrains.research.tasktracker.tracking.activity.ActivityTracker
import org.jetbrains.research.tasktracker.tracking.debugging.DebuggingTracker
import org.jetbrains.research.tasktracker.tracking.fileEditor.FileEditorTracker
import org.jetbrains.research.tasktracker.tracking.toolWindow.ToolWindowTracker
import kotlin.io.path.Path

@Service(Service.Level.PROJECT)
class TrackingService : Disposable {

    private val trackers: MutableList<BaseTracker> = mutableListOf()
    private val logs: MutableList<Loggable> = mutableListOf()

    fun startTracking(project: Project) {
        if (trackers.isNotEmpty()) { // Otherwise we can lose data
            return
        }
        trackers.addAll(
            listOf(
                ActivityTracker(project),
                ToolWindowTracker(project),
                FileEditorTracker(project),
                DebuggingTracker(project),
            )
        )
        project.basePath?.let { path ->
            logs.addAll(
                TaskTrackerPlugin.mainConfig.pluginInfoConfig?.logs?.map {
                    ExternalLogger(
                        Path(path),
                        it
                    )
                } ?: emptyList()
            )
        }
        trackers.forEach { it.startTracking() }
    }

    /**
     * Stops the tracking process by calling the stopTracking method of each tracker.
     * After stopping the tracking, it invokes the send method of each tracker to send the tracked data.
     *
     * @param success A lambda that will be executed after the tracking is stopped and the data is sent successfully.
     * @param failure A lambda that will be executed if there is a failure in stopping the tracking or sending the data.
     */
    fun stopTracking(success: () -> Unit = {}, failure: () -> Unit = {}) {
        trackers.forEach {
            it.stopTracking()
        }
        CoroutineScope(Dispatchers.IO).launch {
            val result = trackers.all {
                it.send()
            }.and(
                logs.all { it.send() }
            )
            trackers.clear()
            logs.clear()
            if (result) success.invoke() else failure.invoke()
        }
    }

    override fun dispose() {
        stopTracking()
    }
}
