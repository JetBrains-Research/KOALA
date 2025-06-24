package org.jetbrains.research.tasktracker.util

import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.research.tasktracker.database.models.Research
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories

private const val FILE_DIRECTORY = "/data/tt-files/"

suspend inline fun RoutingContext.createLogFile(
    logFileType: String,
    researchId: Int
): File {
    val multipartData = call.receiveMultipart()
    val directoryPath = createDirectoryPath(researchId, logFileType)
    var file: File? = null
    multipartData.forEachPart { part ->
        if (part is PartData.FileItem) {
            val fileName = part.originalFileName as String
            val fileBytes = part.streamProvider().readBytes()
            file = file ?: getAndCreateFile(directoryPath, fileName)
            file.appendBytes(fileBytes)
        }
        part.dispose()
    }
    return file ?: error("File must be initialized for this moment")
}

fun createDirectoryPath(researchId: Int, subDirectory: String): Path {
    val userId = transaction {
        Research.findById(researchId)?.user?.id
            ?: error("There are no research with id `$researchId`")
    }
    val directoryPath = Paths.get("$FILE_DIRECTORY$researchId/$userId/$subDirectory")
    directoryPath.createDirectories()
    return directoryPath
}

fun getAndCreateFile(root: Path, filename: String): File {
    return root.resolve(filename).toFile().also {
        check(it.createNewFile()) { "file `$filename` already exists." }
    }
}
