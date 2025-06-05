package org.jetbrains.research.tasktracker.config

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import org.jetbrains.research.tasktracker.config.agreement.AgreementConfig
import org.jetbrains.research.tasktracker.config.content.FinalPageContentConfig
import org.jetbrains.research.tasktracker.config.content.PluginInfoConfig
import org.jetbrains.research.tasktracker.config.content.ServerErrorPageConfig
import org.jetbrains.research.tasktracker.config.content.TaskContentConfig
import org.jetbrains.research.tasktracker.config.ide.MainIdeConfig
import org.jetbrains.research.tasktracker.config.scenario.ScenarioConfig
import org.jetbrains.research.tasktracker.config.survey.SurveyConfig
import org.jetbrains.research.tasktracker.config.tracking.ActivityTrackingConfig
import org.jetbrains.research.tasktracker.config.tracking.CodeTrackingConfig
import org.jetbrains.research.tasktracker.config.util.buildBaseConfig
import org.jetbrains.research.tasktracker.properties.PluginProperties
import java.io.File
import java.util.*

/**
 * Plugin's main config. Initializes and stores configs of all subsystems.
 */
data class MainTaskTrackerConfig(
    val pluginProperties: PluginProperties,

    var taskContentConfig: TaskContentConfig? = null,
    var mainIdeConfig: MainIdeConfig? = null,
    var activityTrackingConfig: ActivityTrackingConfig? = null,
    var codeTrackingConfig: CodeTrackingConfig? = null,
    var pluginInfoConfig: PluginInfoConfig? = null,
    var finalPageConfig: FinalPageContentConfig? = null,
    var serverErrorPageConfig: ServerErrorPageConfig? = null,

    var scenarioConfig: ScenarioConfig? = null,
    var surveyConfig: SurveyConfig? = null,
    var agreementConfig: AgreementConfig? = null
) {
    fun getAllConfigs() = listOf(
        taskContentConfig,
        mainIdeConfig,
        activityTrackingConfig,
        codeTrackingConfig,
        scenarioConfig,
        pluginInfoConfig,
        finalPageConfig,
        serverErrorPageConfig,
        agreementConfig
    )

    companion object {
        val logger = Logger.getInstance(MainTaskTrackerConfig::class.java)

        const val PLUGIN_NAME = "koala"
        val pluginFolderPath = "${PathManager.getPluginsPath()}/$PLUGIN_NAME"
        val agreementFilePath = "$pluginFolderPath/agreement/agreement.json"
        val logFilesFolder = "$pluginFolderPath/logs"
        const val PLUGIN_PROPERTIES_FILE = "$PLUGIN_NAME.properties"
        private val DOMAIN = ResourceBundle.getBundle("properties.actual.domain")
            .getString("serverAddress")

        fun getRoute(path: String) = "$DOMAIN/$path"

        private fun File.isConfigFile() = this.extension == "yaml"

        // TODO: add a builder for server-based properties
        @Suppress("LongMethod")
        fun buildConfig(pluginProperties: PluginProperties, configRoot: File): MainTaskTrackerConfig {
            val mainConfig = MainTaskTrackerConfig(pluginProperties)

            val configFiles = configRoot.walkTopDown().filter { it.isConfigFile() }.toList()
            require(configFiles.isNotEmpty()) { "The root directory with config files must not be empty!" }
            configFiles.forEach { configFile ->
                val fileName = configFile.name
                when {
                    fileName.startsWith(TaskContentConfig.CONFIG_FILE_PREFIX) -> {
                        mainConfig.taskContentConfig = buildBaseConfig(
                            mainConfig.taskContentConfig, configFile, TaskContentConfig::buildConfig, logger
                        )
                    }

                    fileName.startsWith(ScenarioConfig.CONFIG_FILE_PREFIX) -> {
                        mainConfig.scenarioConfig = buildBaseConfig(
                            mainConfig.scenarioConfig, configFile, ScenarioConfig::buildConfig, logger
                        )
                    }

                    fileName.startsWith(PluginInfoConfig.CONFIG_FILE_PREFIX) -> {
                        mainConfig.pluginInfoConfig = buildBaseConfig(
                            mainConfig.pluginInfoConfig, configFile, PluginInfoConfig::buildConfig, logger
                        )
                    }

                    fileName.startsWith(FinalPageContentConfig.CONFIG_FILE_PREFIX) -> {
                        mainConfig.finalPageConfig = buildBaseConfig(
                            mainConfig.finalPageConfig, configFile, FinalPageContentConfig::buildConfig, logger
                        )
                    }

                    fileName.startsWith(ServerErrorPageConfig.CONFIG_FILE_PREFIX) -> {
                        mainConfig.serverErrorPageConfig = buildBaseConfig(
                            mainConfig.serverErrorPageConfig, configFile, ServerErrorPageConfig::buildConfig, logger
                        )
                    }

                    fileName.startsWith(SurveyConfig.CONFIG_FILE_PREFIX) -> {
                        mainConfig.surveyConfig = buildBaseConfig(
                            mainConfig.surveyConfig, configFile, SurveyConfig::buildConfig, logger
                        )
                    }

                    fileName.startsWith(AgreementConfig.CONFIG_FILE_PREFIX) -> {
                        mainConfig.agreementConfig = buildBaseConfig(
                            mainConfig.agreementConfig, configFile, AgreementConfig::buildConfig, logger
                        )
                    }
                }
            }
            mainConfig.mainIdeConfig = MainIdeConfig.buildConfig(configFiles)
            return mainConfig
        }
    }
}
