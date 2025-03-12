import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

group = rootProject.group
version = rootProject.version

fun properties(key: String) = providers.gradleProperty(key)

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.intellij)
}

repositories {
    intellijPlatform {
        defaultRepositories()
    }
}

intellijPlatform {
    buildSearchableOptions = false
    projectName = project.name
    pluginConfiguration {
        id = project.name
        name = properties("pluginName")
        version = properties("pluginVersion")
        ideaVersion {
            sinceBuild = properties("pluginSinceBuild")
            untilBuild = properties("pluginUntilBuild")
        }
    }
}

val jdkVersion = libs.versions.jdk21.get()

dependencies {
    intellijPlatform {
        create(properties("platformType"), properties("platformVersion"))
        instrumentationTools()
        testFramework(TestFrameworkType.Platform)
        plugins(properties("platformPlugins").map { it.split(',').map(String::trim).filter(String::isNotEmpty) })
    }

    implementation(rootProject.libs.kaml)
    implementation(rootProject.libs.snakeyaml)
    implementation(rootProject.libs.ktor.client.cio)
    implementation(rootProject.libs.ktor.client.core)
    implementation(rootProject.libs.ktor.client.json)
    implementation(rootProject.libs.ktor.client.serialization)
    implementation(rootProject.libs.ktor.client.content.negotiation)
    implementation(rootProject.libs.ktor.serialization.kotlinx.json)
    implementation(rootProject.libs.slf4j)
    // Due to the IJPL-157292 issue
    testImplementation(rootProject.libs.opentest4j)
}


configurations {
    all {
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core")
    }
}

val defaultPropertiesDirectory = project.file("src/main/resources/properties/default")
val actualPropertiesDirectory =
    project.file("src/main/resources/properties/actual").also { if (!it.exists()) it.mkdirs() }

tasks {
    val checkPropertiesExist = register("checkPropertiesExist") {
        doLast {
            defaultPropertiesDirectory.listFiles()?.forEach { file ->
                val newFile = File("${actualPropertiesDirectory.absolutePath}/${file.name}")
                if (!newFile.exists()) {
                    newFile.createNewFile()
                    newFile.writeText(file.readText())
                }
            }
        }
    }

    configureEach {
        if (name != checkPropertiesExist.name) {
            dependsOn(checkPropertiesExist)
        }
    }

    patchPluginXml {
        val description = """
            KOALA - a revolutionary plugin for collecting detailed data during education.
            The plugin collects various user activities during interactions with it, such as code
            snapshots with a certain granularity, all interactions with the interface, shortcuts,
            and so on. Extensive configuration options with config files that control its entire
            functionality make it very lightweight and easy to set up and use.
        """.trimIndent()
        pluginDescription.set(description.run { markdownToHTML(this) })

        sinceBuild.set(properties("pluginSinceBuild"))
        untilBuild.set(properties("pluginUntilBuild"))
    }

    withType<JavaCompile> {
        sourceCompatibility = jdkVersion
        targetCompatibility = JavaVersion.VERSION_21.toString()
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
    }

    withType<Detekt>().configureEach {
        jvmTarget = jdkVersion
    }

}
