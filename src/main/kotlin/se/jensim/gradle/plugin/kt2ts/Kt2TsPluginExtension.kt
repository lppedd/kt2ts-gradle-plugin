package se.jensim.gradle.plugin.kt2ts

import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import java.io.File

open class Kt2TsPluginExtension {
    var annotation: String? = null
    var classesDirs: FileCollection? = null
    var outputFile: File? = null
}

fun Project.kt2ts(config: Kt2TsPluginExtension.() -> Unit) {
    @Suppress("UnstableApiUsage")
    extensions.configure("kt2ts", config)
}

val TaskContainer.kt2ts: TaskProvider<Kt2TsTask> get() = named("kt2ts", Kt2TsTask::class.java)
