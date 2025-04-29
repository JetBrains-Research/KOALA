package org.jetbrains.research.tasktracker.tracking.debugging

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.util.messages.MessageBusConnection
import com.intellij.xdebugger.XDebugProcess
import com.intellij.xdebugger.XDebugSessionListener
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.XDebuggerManagerListener
import com.intellij.xdebugger.breakpoints.XBreakpoint
import com.intellij.xdebugger.breakpoints.XBreakpointListener
import org.jetbrains.research.tasktracker.tracking.BaseTracker

class DebuggingTracker(private val project: Project) : BaseTracker("debugging") {
    override val trackerLogger = DebuggingLogger(project)
    private var breakpointConnection: MessageBusConnection? = null
    private var sessionConnection: MessageBusConnection? = null

    override fun startTracking() {
        listenBreakpoints()
        listenDebuggingSessions()
    }

    override fun stopTracking() {
        breakpointConnection?.disconnect()
        sessionConnection?.disconnect()
    }

    private fun listenDebuggingSessions() {
        val debuggerManager = XDebuggerManager.getInstance(project)

        // Create a session listener that will be added to each debug session
        val sessionListener = object : XDebugSessionListener {
            override fun sessionPaused() {
                val session = debuggerManager.currentSession
                val position = session?.currentPosition
                val info = if (position != null) {
                    val file = position.file
                    val line = position.line
                    DebuggingInfo.BreakpointLocation(file.path, line)
                } else {
                    DebuggingInfo.UnknownLocation
                }
                trackerLogger.log(Type.BreakpointHit, info)
            }

            override fun sessionResumed() {
                trackerLogger.log(Type.Resume, DebuggingInfo.Message("Session resumed"))
            }

            override fun sessionStopped() {
                trackerLogger.log(Type.DebuggingStopped, DebuggingInfo.Message("Session stopped"))
            }
        }

        // Add listener to current session if it exists
        val currentSession = debuggerManager.currentSession
        currentSession?.addSessionListener(sessionListener)

        // Register a manager listener to catch new debug sessions
        val managerListener = object : XDebuggerManagerListener {
            override fun processStarted(debugProcess: XDebugProcess) {
                trackerLogger.log(Type.DebuggingStarted, DebuggingInfo.SessionInfo(debugProcess.session.sessionName))
                debugProcess.session.addSessionListener(sessionListener)
            }
        }

        // Register the manager listener
        sessionConnection = ApplicationManager.getApplication().messageBus.connect()
        sessionConnection?.subscribe(XDebuggerManager.TOPIC, managerListener)
    }

    private fun listenBreakpoints() {
        val breakpointListener = object : XBreakpointListener<XBreakpoint<*>> {
            override fun breakpointAdded(breakpoint: XBreakpoint<*>) {
                val sourcePosition = breakpoint.sourcePosition
                val info = if (sourcePosition != null) {
                    DebuggingInfo.BreakpointLocation(sourcePosition.file.path, sourcePosition.line)
                } else {
                    DebuggingInfo.UnknownLocation
                }
                trackerLogger.log(Type.BreakpointAdded, info)
            }

            override fun breakpointRemoved(breakpoint: XBreakpoint<*>) {
                val sourcePosition = breakpoint.sourcePosition
                val info = if (sourcePosition != null) {
                    DebuggingInfo.BreakpointLocation(sourcePosition.file.path, sourcePosition.line)
                } else {
                    DebuggingInfo.UnknownLocation
                }
                trackerLogger.log(Type.BreakpointRemoved, info)
            }

            override fun breakpointChanged(breakpoint: XBreakpoint<*>) {
                val sourcePosition = breakpoint.sourcePosition
                val info = if (sourcePosition != null) {
                    DebuggingInfo.BreakpointLocation(sourcePosition.file.path, sourcePosition.line)
                } else {
                    DebuggingInfo.UnknownLocation
                }
                trackerLogger.log(Type.BreakpointChanged, info)
            }
        }

        breakpointConnection = ApplicationManager.getApplication().messageBus.connect().apply {
            subscribe(XBreakpointListener.TOPIC, breakpointListener)
        }
    }
}
