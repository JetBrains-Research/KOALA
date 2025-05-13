package org.jetbrains.research.tasktracker.config.scenario.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.research.tasktracker.config.ide.MainIdeConfig

@Serializable
sealed interface ScenarioUnit

/**
 * List of tasks from which user selects a task to run.
 * The chosen task is removed from the list after completion.
 * The process runs until there are no tasks left.
 */
@Serializable
@SerialName("List")
class TaskListUnit(val taskIds: List<String>) : ScenarioUnit

/**
 * List of tasks in which user can select one task.
 * After completion of this task next unit will be processed.
 * Unselected tasks won't be present.
 */
@Serializable
@SerialName("SingleList")
class TaskListWithSingleChoiceUnit(val taskIds: List<String>) : ScenarioUnit

/**
 * Task unit loaded by the id.
 * @see [org.jetbrains.research.tasktracker.config.content.TaskContentConfig]
 * And [org.jetbrains.research.tasktracker.config.content.task.ProgrammingTask]
 *
 * @param id The ID of the task to load
 * @param timerSeconds Optional timer in seconds
 */
@Serializable
@SerialName("Task")
class TaskUnit(val id: String, val timerSeconds: Long? = null) : ScenarioUnit

/**
 * Setting Unit represents by MainIdeConfig format.
 * @see [org.jetbrains.research.tasktracker.config.ide.MainIdeConfig].
 */
@Serializable
@SerialName("Setting")
class IdeSettingUnit(val mainIdeConfig: MainIdeConfig) : ScenarioUnit

/**
 * Survey unit. Uses *id* of the survey defined in the SurveyConfig.
 * @see [org.jetbrains.research.tasktracker.config.survey.SurveyConfig].
 */
@Serializable
@SerialName("Survey")
class SurveyUnit(val id: String) : ScenarioUnit

/**
 * Opens external url in the JCEF.
 */
@Serializable
@SerialName("External")
class ExternalSourceUnit(val url: String) : ScenarioUnit
