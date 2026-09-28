package com.agonapp.buildtools

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.util.Base64

class BuildToolsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("generateBuildMetadata") {
            it.doLast {
                val metadata = mutableMapOf<String, String>()
                metadata["app.name"] = project.rootProject.name
                metadata["app.version"] = "1.0.0"
                metadata["gradle.version"] = project.gradle.gradleVersion
                metadata["jvm.version"] = System.getProperty("java.version") ?: "unknown"
                
                val ciVars = listOf("CI", "BUILD_ID", "BUILD_NUMBER", "GIT_COMMIT", "JOB_NAME")
                ciVars.forEach { key ->
                    System.getenv(key)?.let { metadata["ci.$key"] = it }
                }
                
                val assetsDir = project.file("src/main/assets/build-info")
                assetsDir.mkdirs()
                val payload = metadata.entries.joinToString("\u0001") { "${it.key}\u0000${it.value}" }
                val encoded = Base64.getEncoder().encodeToString(payload.toByteArray())
                File(assetsDir, "metadata.dat").writeText(encoded)
            }
        }
        
        project.tasks.named("preBuild").configure {
            it.dependsOn("generateBuildMetadata")
        }
    }
}
