package org.jetbrains.research.tasktracker.tracking.debugging

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.LocalFileSystem
import org.jetbrains.research.tasktracker.tracking.logger.BaseLogger
import org.jetbrains.research.tasktracker.tracking.logger.LoggedData
import org.joda.time.DateTime
import java.io.File

class DebuggingLogger(val project: Project) : BaseLogger() {
    override val logPrinterFilename: String
        get() = "debugging_${project.hashCode()}_${project.name}"
    override val loggedData: LoggedData<*, *>
        get() = DebuggingLoggedData

    fun log(type: Type, info: DebuggingInfo) =
        DebuggingEvent(DateTime.now(), type, info, getSelectedText(info)).also {
            log(DebuggingLoggedData.getData(it))
        }

    /**
     * @return text from the breakpoint location or selected text based on the debugging info
     */
    private fun getSelectedText(info: DebuggingInfo): String? =
        ApplicationManager.getApplication().runReadAction<String?> {
            when (info) {
                is DebuggingInfo.BreakpointLocation -> {
                    // Get text from the file at the breakpoint location
                    val virtualFile = LocalFileSystem.getInstance().findFileByIoFile(File(info.filePath))
                    if (virtualFile != null) {
                        val document = FileDocumentManager.getInstance().getDocument(virtualFile)
                        if (document != null && info.line < document.lineCount) {
                            // Get the text of the line at the breakpoint location
                            val lineStartOffset = document.getLineStartOffset(info.line)
                            val lineEndOffset = document.getLineEndOffset(info.line)
                            return@runReadAction document.getText(TextRange(lineStartOffset, lineEndOffset)).trim()
                        }
                    }
                    // If file not found or line is out of range, return null
                    null
                }

                else -> {
                    // For other types of info, get selected text from the current editor
                    FileEditorManager.getInstance(project).selectedTextEditor?.selectionModel?.selectedText
                }
            }
        }
}
