package org.jetbrains.research.tasktracker.tracking.debugging

import org.jetbrains.research.tasktracker.tracking.logger.LoggedData
import org.jetbrains.research.tasktracker.tracking.logger.LoggedDataGetter

object DebuggingLoggedData : LoggedData<DebuggingEvent, String?>() {
    override val loggedDataGetters: List<LoggedDataGetter<DebuggingEvent, String?>> = arrayListOf(
        LoggedDataGetter("date") { it.time.toString() },
        LoggedDataGetter("type") { it.type.name },
        LoggedDataGetter("info") { it.info.toString() },
        LoggedDataGetter("file_path") { 
            when (val info = it.info) {
                is DebuggingInfo.BreakpointLocation -> info.filePath
                else -> null
            }
        },
        LoggedDataGetter("line") { 
            when (val info = it.info) {
                is DebuggingInfo.BreakpointLocation -> info.line.toString()
                else -> null
            }
        },
        LoggedDataGetter("message") { 
            when (val info = it.info) {
                is DebuggingInfo.Message -> info.message
                is DebuggingInfo.SessionInfo -> info.sessionName
                else -> null
            }
        },
        LoggedDataGetter("selected_text") { it.selectedText }
    )
}
