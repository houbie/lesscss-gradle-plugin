package com.github.houbie.gradle.lesscss

import com.github.houbie.lesscss.builder.CompilationTask
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

class LesscDaemonTask extends DefaultTask {
    static final String NAME = 'lesscDaemon'

    String lesscTaskName = LesscTask.NAME

    long interval = 500

    @TaskAction
    def run() {
        for (lesscTask in project.getTasksByName(lesscTaskName, false)) {
            if (lesscTask instanceof LesscTask) {
                CompilationTask compilationTask = lesscTask.createCompilationTask()
                logger.info("starting lessc daemon...")
                compilationTask.startDaemon(interval)
                if (System.console()) {
                    System.console().readLine("Lessc daemon is running. Press enter to quit...")
                } else {
                    System.in.read()
                }
                compilationTask.stopDaemon()
                logger.info("lessc daemon stopped")
                return
            }
        }
        throw new GradleException("Task $lesscTaskName cannot be found and is required to start the lessc daemon")
    }
}
