package org.jetbrains.research.tasktracker.tracking.debugging

import org.joda.time.DateTime

/**
 * Structure that encompasses all the data that needs to be logged for any debugging activity.
 */
data class DebuggingEvent(
    val time: DateTime,
    val type: Type,
    /**
     * Information about the debugging event, depending on its type.
     * Example for Type.BreakpointAdded - file path and line number.
     * Example for Type.BreakpointHit - file path, line number, and variable values.
     */
    val info: DebuggingInfo,
    /**
     * Selected text in the current opened file.
     */
    val selectedText: String? = null
)

enum class Type {
    /**
     * Breakpoint added to the code.
     */
    BreakpointAdded,

    /**
     * Breakpoint removed from the code.
     */
    BreakpointRemoved,

    /**
     * Breakpoint changed (e.g., condition changed, enabled/disabled).
     */
    BreakpointChanged,

    /**
     * Breakpoint hit during debugging.
     */
    BreakpointHit,

    /**
     * Debugging session started.
     */
    DebuggingStarted,

    /**
     * Debugging session stopped.
     */
    DebuggingStopped,

    /**
     * Resume program action during debugging.
     */
    Resume
}
