package org.jetbrains.research.tasktracker.tracking.fileEditor

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.messages.MessageBusConnection
import org.jetbrains.research.tasktracker.tracking.BaseTracker
import org.jetbrains.research.tasktracker.tracking.logger.FileEditorLogger
import kotlin.io.path.Path

// TODO make photos and log Emotions if webcam switched on
class FileEditorTracker(
    private val project: Project,
) : BaseTracker("fileEditor") {
    private var messageBusConnection: MessageBusConnection? = null
    override val trackerLogger = FileEditorLogger(project)

    override fun startTracking() {
        val fileEditorListener = object : FileEditorManagerListener {
            override fun selectionChanged(event: FileEditorManagerEvent) {
                super.selectionChanged(event)
                trackerLogger.log(FileEditorAction.FOCUS, event.oldFile?.projectPath, event.newFile?.projectPath)
            }

            override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
                super.fileOpened(source, file)
                trackerLogger.log(FileEditorAction.OPEN, null, file.projectPath)
            }

            override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
                super.fileClosed(source, file)
                trackerLogger.log(FileEditorAction.CLOSE, file.projectPath)
            }
        }
        messageBusConnection = project.messageBus.connect()
        messageBusConnection?.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, fileEditorListener)
    }

    val VirtualFile.projectPath: String
        get() =
            toNioPath().let { filepath ->
                project.basePath?.let { projectPath ->
                    Path(projectPath).relativize(filepath)
                } ?: filepath
            }.toString()

    override fun stopTracking() {
        messageBusConnection?.disconnect()
    }
}
