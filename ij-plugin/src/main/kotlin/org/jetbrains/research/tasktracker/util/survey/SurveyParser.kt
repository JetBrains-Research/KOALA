package org.jetbrains.research.tasktracker.util.survey

import com.intellij.openapi.project.Project
import org.jetbrains.concurrency.await
import org.jetbrains.research.tasktracker.config.survey.*
import org.jetbrains.research.tasktracker.tracking.Loggable
import org.jetbrains.research.tasktracker.ui.main.panel.MainPluginWindow
import java.io.File

class SurveyParser(private val mainWindow: MainPluginWindow, project: Project) : Loggable() {
    private val surveyLogger = SurveyLogger(project)

    suspend fun parseAndLog(survey: Survey) =
        survey.htmlQuestions.forEachIndexed { index, surveyItem -> parseAndLog(surveyItem, index) }

    private suspend fun parseAndLog(item: HtmlQuestion, id: Int) {
        when (item) {
            is InputHtmlQuestion -> {
                val result = mainWindow.getElementValue(item.elementId).await()
                surveyLogger.log(item.text, result.toString(), option = item.elementId, questionId = id)
            }

            is TextAreaHtmlQuestion -> {
                val result = mainWindow.getElementValue(item.elementId).await()
                surveyLogger.log(item.text, result.toString(), option = item.elementId, questionId = id)
            }

            is RadioHtmlQuestion -> item.infos.forEach { info ->
                val result = mainWindow.checkIfRadioButtonChecked(info.id).await()
                surveyLogger.log(item.text, result.toString(), option = info.id, questionId = id)
            }

            is HtmlQuestionContainer -> item.subQuestions.forEach {
                parseAndLog(it, id)
            }

            is CheckboxHtmlQuestion -> item.infos.forEach { info ->
                val result = mainWindow.checkIfRadioButtonChecked(info.id).await()
                surveyLogger.log(item.text, result.toString(), option = info.id, questionId = id)
            }

            is SliderHtmlQuestion -> {
                val result = mainWindow.getElementValue(item.elementId).await()
                surveyLogger.log(item.text, result, option = item.elementId, questionId = id)
            }
        }
    }

    override val logFileType: String
        get() = "survey"

    override fun getLogFiles(): List<File> = surveyLogger.getLogFiles()
}
