package org.jetbrains.research.tasktracker.config.content.task.base

interface TaskWithFiles : Task {
    /**
     * @return Task files of the task.
     */
    val files: List<ITaskFileInfo>

    val trackAllFiles: Boolean

    /**
     * @return id of the file that the development environment
     * will focus on when the current task init.
     */
    val focusFileId: String?

    /**
     * @return root directory relative to the plugin directory in the project.
     */
    val root: String
}
