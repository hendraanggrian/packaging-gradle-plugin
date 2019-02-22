package com.hendraanggrian.packr

import com.badlogicgames.packr.Packr
import com.badlogicgames.packr.PackrConfig
import org.gradle.api.DefaultTask
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.LogLevel.INFO
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.awt.Desktop
import java.io.File
import java.io.IOException

/** Task that will generate native distribution on each platform. */
open class PackTask : DefaultTask() {

    @Input lateinit var extension: PackrExtension
    @Input lateinit var platform: PackrConfig.Platform

    @TaskAction
    @Throws(IOException::class)
    @Suppress("unused")
    fun pack() {
        val distribution = extension.getDistribution(platform)
        if (distribution == null) {
            logger.log(INFO, "No configuration found for $platform")
            return
        }
        logger.log(INFO, "Creating configuration for $platform")

        val config = PackrConfig()
        config.platform = platform
        config.jdk = checkNotNull(distribution.jdk) { "JDK path has not yet been specified" }
        config.executable = checkNotNull(extension.executable) { "Undefined executable" }
        config.classpath = extension.classpath.flatMap { file ->
            when {
                file.isDirectory -> file.listFiles().filter { it.isJar() }.map { it.absolutePath }
                file.isJar() -> listOf(file.absolutePath)
                else -> emptyList()
            }
        }
        config.mainClass = checkNotNull(extension.mainClass) { "Undefined main class" }

        val outputDirectory = File(extension.outputDirectory)
        config.outDir = outputDirectory.resolve(distribution.name ?: project.name)

        config.vmArgs = extension.vmArgs + distribution.vmArgs
        config.resources = extension.resources.toList()
        config.minimizeJre = extension.minimizeJre
        if (distribution is MacOSDistribution) {
            distribution.icon?.let { config.iconResource = it }
            distribution.bundleId?.let { config.bundleIdentifier = it }
        }
        config.verbose = extension.verbose

        if (config.outDir.exists()) {
            logger.log(LogLevel.INFO, "Deleting old output")
            config.outDir.deleteRecursively()
        }

        logger.log(LogLevel.INFO, "Preparing output")
        outputDirectory.mkdirs()

        Packr().pack(config)
        logger.log(LogLevel.INFO, "Pack completed")

        if (extension.openOnDone) {
            when {
                !Desktop.isDesktopSupported() ->
                    logger.log(LogLevel.INFO, "Desktop is not supported, ignoring `openOnDone`")
                else -> Desktop.getDesktop().run {
                    when {
                        !isSupported(Desktop.Action.OPEN) ->
                            logger.log(LogLevel.INFO, "Opening folder is not supported, ignoring `openOnDone`")
                        else -> open(outputDirectory)
                    }
                }
            }
        }
    }

    private fun File.isJar(): Boolean = extension == "jar"
}