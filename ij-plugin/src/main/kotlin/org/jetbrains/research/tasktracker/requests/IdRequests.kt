package org.jetbrains.research.tasktracker.requests

import com.intellij.openapi.diagnostic.Logger
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.research.tasktracker.TaskTrackerPlugin
import org.jetbrains.research.tasktracker.config.MainTaskTrackerConfig.Companion.getRoute
import org.jetbrains.research.tasktracker.ui.main.panel.storage.GlobalPluginStorage

object IdRequests {
    private val client = HttpClient(CIO)
    private val logger = Logger.getInstance(IdRequests::class.java)

    @Suppress("TooGenericExceptionCaught")
    fun getUserId(name: String, email: String): Int? =
        runBlocking {
            val url = getRoute("create-user")
            val researchId = TaskTrackerPlugin.mainConfig.pluginInfoConfig?.let {
                it.researchId
            } ?: error("Plugin info config is uninitialized")
            try {
                return@runBlocking client.submitForm(
                    url = url,
                    formParameters = mapOf(
                        "name" to "${researchId}_$name",
                        "email" to email
                    ).buildParameters()
                ).body<Int>()
            } catch (e: Exception) {
                logger.warn("Server interaction error while getting user id! Url: $url", e)
            }
            return@runBlocking null
        }

    @Suppress("TooGenericExceptionCaught")
    fun getResearchId(): Int? =
        runBlocking {
            val url = getRoute("create-research")
            val pluginInfoConfig = TaskTrackerPlugin.mainConfig.pluginInfoConfig
                ?: error("MainPageConfig must not be null")
            try {
                requireNotNull(GlobalPluginStorage.userId) { "User id is not defined" }
                val researchId = TaskTrackerPlugin.mainConfig.pluginInfoConfig?.let {
                    it.researchId
                } ?: error("Plugin info config is uninitialized")
                return@runBlocking client.submitForm(
                    url = url,
                    formParameters = mapOf(
                        "name" to pluginInfoConfig.pluginName,
                        "description" to pluginInfoConfig.pluginDescription,
                        "user_id" to GlobalPluginStorage.userId.toString(),
                        "research_unique_id" to researchId
                    ).buildParameters()
                ).body<Int>()
            } catch (e: IllegalArgumentException) {
                logger.warn(e.localizedMessage)
            } catch (e: Exception) {
                logger.warn("Server interaction error while getting research id! Url: $url", e)
            }
            return@runBlocking null
        }

    private fun Map<String, String>.buildParameters() = parameters {
        this@buildParameters.forEach { (name, param) ->
            append(name, param)
        }
    }
}
