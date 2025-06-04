package org.jetbrains.research.tasktracker.tracking.mock

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.research.tasktracker.config.MainTaskTrackerConfig
import org.jetbrains.research.tasktracker.config.content.task.base.ITaskFileInfo
import org.jetbrains.research.tasktracker.config.content.task.base.TaskWithFiles
import org.jetbrains.research.tasktracker.models.Extension
import org.jetbrains.research.tasktracker.tracking.DefaultContentProvider
import org.jetbrains.research.tasktracker.tracking.task.SourceSet

data class MockTask(
    override val name: String,
    override val files: List<ITaskFileInfo>,
    override val root: String = "",
    override val description: String = "",
    override val focusFileId: String? = null,
    override val id: String = "",
    override val trackAllFiles: Boolean = false,
) : TaskWithFiles

data class MockTaskFile(
    override val filename: String,
    override val extension: Extension,
    override val relativePath: String = "",
    override val content: String = DefaultContentProvider.getDefaultContent(extension, relativePath),
    override val id: String? = null,
    override val templateFile: String? = null,
) : ITaskFileInfo {
    override val sourceSet: SourceSet = SourceSet.SRC
    override val isInternal: Boolean
        get() = true
}

fun List<VirtualFile>.toMockTask(project: Project): MockTask {
    val firstFile = this.firstOrNull() ?: error("Empty list is unexpected")
    val taskFiles = map { file ->
        MockTaskFile(
            file.nameWithoutExtension,
            file.extensionEnum(),
            file.getRelativePath(project),
            FileDocumentManager.getInstance().getDocument(file)?.text ?: error("file `${file.name}` does not exist"),
        )
    }
    return MockTask(firstFile.nameWithoutExtension, taskFiles, firstFile.getRoot(project))
}

private fun VirtualFile.getRoot(project: Project) =
    getRootAndRelativePath(project).first().takeIf { it != "src" }.orEmpty()

private fun VirtualFile.getRelativePath(project: Project) =
    getRootAndRelativePath(project).takeLastWhile { it != "src" }.joinToString("/")

private fun VirtualFile.getRootAndRelativePath(project: Project): List<String> =
    path.removePrefix("${project.basePath}/${MainTaskTrackerConfig.PLUGIN_NAME}/${extensionEnum().name.lowercase()}/")
        .removeSuffix("/$name").split("/")

private fun VirtualFile.extensionEnum() =
    Extension.values().find { it.ext == ".$extension" } ?: error("Unexpected extension `$extension`")
