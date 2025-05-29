package org.jetbrains.research.tasktracker.ui.main.panel

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBPanel
import com.intellij.ui.jcef.JBCefApp
import com.intellij.util.ui.JBUI
import kotlinx.serialization.json.Json
import org.jetbrains.concurrency.Promise
import org.jetbrains.research.tasktracker.TaskTrackerPlugin
import org.jetbrains.research.tasktracker.config.content.task.base.Task
import org.jetbrains.research.tasktracker.tracking.TaskFileHandler
import org.jetbrains.research.tasktracker.tracking.TrackingService
import org.jetbrains.research.tasktracker.ui.main.panel.models.AgreementChecker
import org.jetbrains.research.tasktracker.ui.main.panel.models.ButtonState
import org.jetbrains.research.tasktracker.ui.main.panel.models.LinkType
import org.jetbrains.research.tasktracker.ui.main.panel.panelStates.agreementAcceptance
import org.jetbrains.research.tasktracker.ui.main.panel.storage.GlobalPluginStorage
import org.jetbrains.research.tasktracker.ui.main.panel.template.HtmlTemplate
import org.jetbrains.research.tasktracker.util.UIBundle
import java.awt.BorderLayout
import java.awt.FlowLayout

/**
 * The class is intended to manage JCEF and Swing components simultaneously,
 * and its methods primarily serve as a way to switch between frames.
 *
 * Note: This class requires JBCefApp to be supported for proper functioning.
 *
 */
@Suppress("TooManyFunctions")
class MainPluginPanelFactory : ToolWindowFactory {
    // TODO: init in other place, states can be saved between sessions
    private val nextButton = createJButton("ui.button.next")
    private val backButton = createJButton("ui.button.back", isVisibleProp = false)

    //    private val pauseButton = createJButton("ui.button.pause", isVisibleProp = false)

    private val logger: Logger = Logger.getInstance(MainPluginPanelFactory::class.java)

    lateinit var trackingService: TrackingService
    lateinit var mainWindow: MainPluginWindow
    lateinit var project: Project

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        TaskFileHandler.initProject(project)
        TaskTrackerPlugin.initPlugin()
        this.project = project
        mainWindow = project.getService(MainWindowService::class.java).mainWindow
        trackingService = project.getService(TrackingService::class.java)
        mainWindow.jComponent.size = JBUI.size(toolWindow.component.width, toolWindow.component.height)
//        pauseButton.setListener { stopTracking() }
        mainWindow.onError {
            nextButton.text = UIBundle.message("ui.button.welcome")
            setNextAction { agreementAcceptance() }
        }
        agreementAcceptance()
        val buttonPanel = JBPanel<JBPanel<*>>(FlowLayout()).apply {
            add(backButton)
//            add(pauseButton)
            add(nextButton)
        }
        val panel = JBPanel<JBPanel<*>>(BorderLayout()).apply {
            add(mainWindow.jComponent)
            add(buttonPanel, BorderLayout.SOUTH)
        }
        toolWindow.component.add(panel)
        listenRedirection()
    }

    override fun isApplicable(project: Project) = super.isApplicable(project) && JBCefApp.isSupported()

    @Suppress("LongParameterList")
    fun loadBasePage(
        template: HtmlTemplate,
        buttonTextKey: String? = null,
        isVisibleBackButton: Boolean = false,
        backButtonText: String? = null,
        isVisibleNextButton: Boolean = true,
        @Suppress("UnusedParameter")
        pauseButtonText: String? = null,
        @Suppress("UnusedParameter")
        isVisiblePauseButton: Boolean = true
    ) {
        mainWindow.loadHtmlTemplate(template)
        backButton.isVisible = isVisibleBackButton
        nextButton.isVisible = isVisibleNextButton
//        pauseButton.isVisible = isVisiblePauseButton
        backButtonText?.let {
            backButton.text = UIBundle.message(it)
        }
        buttonTextKey?.let {
            nextButton.text = UIBundle.message(it)
        }
//        pauseButtonText?.let {
//            pauseButton.text = UIBundle.message(it)
//        }
    }

    fun focusOnfFileById(task: Task, id: String?) {
        id?.let {
            TaskFileHandler.getVirtualFileByProjectTaskId(project, task, id)?.let {
                focusOnFile(it)
            }
        } ?: TaskFileHandler.projectToTaskToFiles[project]?.get(task)?.first()?.let {
            focusOnFile(it)
        } ?: logger.error("Can't find any file for '$task' task")
    }

    /**
     * Switches the editor to the virtualFile.
     */
    private fun focusOnFile(virtualFile: VirtualFile) {
        ApplicationManager.getApplication().invokeLater {
            FileEditorManager.getInstance(project).openFile(
                virtualFile, true
            )
        }
    }

    /**
     * Opening file in editor by html link.
     */
    fun listenFileRedirection(task: Task) {
        mainWindow.jslinkProcess(LinkType.FILE) {
            focusOnfFileById(task, it)
        }
    }

    /**
     * Redirecting to external websites in JCEF.
     */
    private fun listenRedirection() {
        mainWindow.jslinkProcess(LinkType.JCEF) {
            val previousBackState = backButton.getState()
            val previousNextState = nextButton.getState()
            nextButton.changeState(ButtonState(previousNextState.text, false))
            backButton.changeState(
                ButtonState(UIBundle.message("ui.button.back"), true) {
                    backButton.changeState(previousBackState)
                    nextButton.changeState(previousNextState)
                    mainWindow.loadCurrentTemplate()
                }
            )
        }
    }

    /**
     * If all required fields are filled then
     * [saveAgreements][org.jetbrains.research.tasktracker.ui.main.panel.saveAgreements]
     * function will be called.
     *
     * @return **true** if any required field is not filled. **false** otherwise.
     */
    fun checkAgreementInputs(): Promise<Boolean> =
        mainWindow.executeJavaScriptAsync("checkAllInputs()").then {
            val agreementChecker = Json.decodeFromString(AgreementChecker.serializer(), it.toString())
            if (agreementChecker.allRequiredChecked()) {
                GlobalPluginStorage.agreementChecker = agreementChecker
                saveAgreements(it.toString())
                return@then false
            }
            true
        }

    fun checkSurveyInputs(): Promise<Boolean> = mainWindow.executeJavaScriptAsync("checkAllInputs()").then {
        it.toBoolean()
    }

    fun openExternalUrl(url: String) = mainWindow.openExternalUrl(url)

    fun setNextAction(listener: () -> Unit = {}) = nextButton.setListener(listener)

    fun setBackAction(listener: () -> Unit = {}) = backButton.setListener(listener)
}
