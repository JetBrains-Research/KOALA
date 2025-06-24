package org.jetbrains.research.tasktracker.tracking.debugging

/**
 * Sealed class hierarchy for different types of debugging information.
 */
sealed class DebuggingInfo {
    /**
     * Information about a breakpoint location.
     */
    data class BreakpointLocation(
        val filePath: String,
        val line: Int
    ) : DebuggingInfo() {
        override fun toString(): String = "$filePath:$line"
    }

    /**
     * Information about a debugging session.
     */
    data class SessionInfo(
        val sessionName: String
    ) : DebuggingInfo() {
        override fun toString(): String = sessionName
    }

    /**
     * Simple message information.
     */
    data class Message(
        val message: String
    ) : DebuggingInfo() {
        override fun toString(): String = message
    }

    /**
     * Unknown location information.
     */
    object UnknownLocation : DebuggingInfo() {
        override fun toString(): String = "Unknown location"
    }
}
