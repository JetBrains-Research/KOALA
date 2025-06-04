package org.jetbrains.research.tasktracker.config.content.task

import kotlinx.serialization.Serializable
import org.jetbrains.research.tasktracker.config.content.task.base.TaskFileInfo
import org.jetbrains.research.tasktracker.config.content.task.base.TaskWithFiles
import org.jetbrains.research.tasktracker.models.Extension

@Serializable
data class ProgrammingTask(
    override val name: String,
    override val description: String,
    override val id: String,
    val inputsOutputs: List<InputOutput>? = null,
    override val files: List<TaskFileInfo> = emptyList(),
    override val root: String = name,
    val language: Extension? = null,
    override val focusFileId: String? = null,
    override val trackAllFiles: Boolean = false,
) : TaskWithFiles
