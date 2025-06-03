@file:Suppress("TooManyFunctions")

package org.jetbrains.research.tasktracker.ui.main.panel.panelStates

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.WindowManager
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.research.tasktracker.TaskTrackerPlugin
import org.jetbrains.research.tasktracker.config.content.task.base.Task
import org.jetbrains.research.tasktracker.config.content.task.base.TaskWithFiles
import org.jetbrains.research.tasktracker.config.scenario.models.*
import org.jetbrains.research.tasktracker.requests.IdRequests
import org.jetbrains.research.tasktracker.tracking.TaskFileHandler
import org.jetbrains.research.tasktracker.ui.getTimeText
import org.jetbrains.research.tasktracker.ui.main.panel.MainPluginPanelFactory
import org.jetbrains.research.tasktracker.ui.main.panel.runOnSuccess
import org.jetbrains.research.tasktracker.ui.main.panel.storage.GlobalPluginStorage
import org.jetbrains.research.tasktracker.ui.main.panel.storage.MainPanelStorage
import org.jetbrains.research.tasktracker.ui.main.panel.template.*
import org.jetbrains.research.tasktracker.ui.statusbar.TimerStatusBarWidget
import org.jetbrains.research.tasktracker.util.UIBundle
import org.jetbrains.research.tasktracker.util.notifier.notifyError
import org.jetbrains.research.tasktracker.util.survey.SurveyParser
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

typealias Panel = MainPluginPanelFactory

const val TIMER_DELAY = 1000L
const val TIMER_PERIOD = 1000L

/**
 * A page for collecting user data, and checkboxes for user agreement acceptance.
 */
fun Panel.agreementAcceptance() {
    GlobalPluginStorage.resetSession()
    loadBasePage(AgreementTemplate.loadCurrentTemplate(), "ui.button.next", false, isVisiblePauseButton = false)
    setNextAction {
        checkAgreementInputs().runOnSuccess {
            if (!it) {
                GlobalPluginStorage.agreementChecker?.let { agreement ->
                    GlobalPluginStorage.userId = IdRequests.getUserId(agreement.name, agreement.email).also {
                        if (it == null) {
                            notifyError(
                                project,
                                UIBundle.message("ui.connection.lose")
                            )
                            return@runOnSuccess
                        }
                    }
                }
                GlobalPluginStorage.currentResearchId = IdRequests.getResearchId()
                welcomePage()
            } else {
                notifyError(project, UIBundle.message("ui.please.fill"))
            }
        }
    }
}

/**
 * Switches the panel to the plugin description window.
 */
fun Panel.welcomePage() {
    loadBasePage(MainPageTemplate.loadCurrentTemplate(), "ui.button.next", false, isVisiblePauseButton = false)
    setNextAction {
        TaskTrackerPlugin.initializationHandler.setupEnvironment(project)
        trackingService.startTracking(project)
        processScenario()
    }
}

/**
 * Switches the panel to the task selection window.
 */
private fun Panel.selectTask(taskIds: List<String>, allRequired: Boolean = true) {
    val tasks = TaskTrackerPlugin.mainConfig.taskContentConfig?.tasks?.filter { it.id in taskIds } ?: emptyList()
    loadBasePage(TasksPageTemplate(tasks), isVisiblePauseButton = false)
    setNextAction {
        mainWindow.getElementValue("tasks").runOnSuccess { name ->
            solveTask(name, if (allRequired) taskIds.filter { it != name } else emptyList())
        }
    }
}

/**
 * Loads configs by selected task and language
 */
fun Panel.processTask(id: String): Task {
    val task =
        MainPanelStorage.taskIdTask.values.find { it.id == id } ?: error("Can't find task with id '$id'")
    ApplicationManager.getApplication().invokeAndWait {
        TaskFileHandler.initTask(project, task)
    }
    (task as? TaskWithFiles)?.focusFileId?.let { fileId ->
        focusOnfFileById(task, fileId)
    }
    return task
}

private val Project.timerWidget: TimerStatusBarWidget?
    get() = WindowManager.getInstance().getStatusBar(this)?.getWidget(TimerStatusBarWidget.ID) as? TimerStatusBarWidget

private fun Panel.startTimerFor(timer: Timer, seconds: Long, onEachSecond: (Long) -> Unit = {}, nextAction: () -> Unit) {
    // Get or create the timer widget
    val timerWidget = project.timerWidget

    // Initialize with total time
    timerWidget?.updateTime(seconds)
    onEachSecond.invoke(seconds)

    // Create a timer that updates every second
    var remainingSeconds = seconds
    timer.scheduleAtFixedRate(
        object : TimerTask() {
            override fun run() {
                val time = --remainingSeconds
                if (time <= 0) {
                    timer.cancel()
                }
                ApplicationManager.getApplication().invokeLater {
                    onEachSecond.invoke(time)
                    timerWidget?.updateTime(time)
                    if (time <= 0) {
                        timerWidget?.stopTime()

                        Messages.showInfoMessage(
                            project,
                            "Time is up! Go to the Koala plugin and follow instructions.",
                            "Time To Continue",
                        )
                        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("KOALA")
                        toolWindow?.show()

                        nextAction.invoke()
                    }
                }
            }
        },
        TIMER_DELAY, // 1 second delay
        TIMER_PERIOD // 1 second period
    )
}

/**
 * Switches the panel to the task solving window.
 * It contains task name, description and I/O data.
 *
 * @param id The ID of the task to solve
 * @param nextTasks List of task IDs to select after this task is completed
 * @param timerSeconds Optional timer in seconds
 */
private fun Panel.solveTask(id: String, nextTasks: List<String> = emptyList(), timerSeconds: Long? = null) {
    val task = processTask(id)
    loadBasePage(SolvePageTemplate(task), isNextButtonEnabled = timerSeconds == null)
    val timer = Timer()
    val isNextActionPerforming = AtomicBoolean(false)
    val nextAction = {
        if (isNextActionPerforming.compareAndSet(false, true)) {
            timer.cancel()
            project.timerWidget?.stopTime()
            TaskFileHandler.disposeTask(project, task)
            if (nextTasks.isNotEmpty()) {
                selectTask(nextTasks)
            } else {
                processScenario()
            }
        }
    }

    setNextAction(nextAction)

    // Set up timer if specified
    timerSeconds?.let { seconds ->
        startTimerFor(
            timer, seconds,
            onEachSecond = { time ->
                nextButton.text = "Time left: ${getTimeText(time)}"
            },
            nextAction = nextAction,
        )
    }

    listenFileRedirection(task)
}

@OptIn(DelicateCoroutinesApi::class)
fun Panel.survey(id: String) {
    val survey = TaskTrackerPlugin.mainConfig.surveyConfig?.surveys?.find { it.id == id }
        ?: error("Survey with id `$id` hasn't been found.")
    loadBasePage(SurveyTemplate(survey))
    setNextAction {
        checkSurveyInputs().runOnSuccess {
            if (it) {
                val surveyParser = SurveyParser(mainWindow, project)
                GlobalScope.launch {
                    surveyParser.parseAndLog(survey)
                    surveyParser.send()
                    processScenario()
                }
            } else {
                notifyError(project, UIBundle.message("ui.please.fill"))
            }
        }
    }
}

fun Panel.serverErrorPage() {
    loadBasePage(ServerErrorPage(), "ui.button.welcome", false, isVisiblePauseButton = false)
    setNextAction {
        agreementAcceptance()
    }
}

fun Panel.finalPage() {
    loadBasePage(FinalPageTemplate.loadCurrentTemplate(), "ui.button.welcome", false, isVisiblePauseButton = false)
    setNextAction {
        agreementAcceptance()
    }
}

fun Panel.processScenario() {
    val scenario =
        TaskTrackerPlugin.mainConfig.scenarioConfig?.scenario
            ?: error("Unexpected error, Scenario config must exist!")
    when (val unit = scenario.getNextUnit(project)) {
        is TaskListUnit -> {
            selectTask(unit.taskIds)
        }

        is TaskListWithSingleChoiceUnit -> {
            selectTask(unit.taskIds, allRequired = false)
        }

        is TaskUnit -> {
            solveTask(unit.id, timerSeconds = unit.timerSeconds)
        }

        is IdeSettingUnit -> {
            unit.mainIdeConfig.buildHandler(project).also {
                MainPanelStorage.activeIdeHandlers.addFirst(it)
                it.setup()
            }
            processScenario()
        }

        is SurveyUnit -> {
            survey(unit.id)
        }

        is ExternalSourceUnit -> {
            openExternalUrl(unit.url)
        }

        null -> {
            stopTracking()
        }
    }
}

fun Panel.stopTracking() {
    TaskTrackerPlugin.mainConfig.scenarioConfig?.scenario?.reset()
    loadBasePage(LoadTemplate())
    TaskFileHandler.disposeAllTasks()
    ApplicationManager.getApplication().invokeLater {
        trackingService.stopTracking(::finalPage, ::serverErrorPage)
    }
}
