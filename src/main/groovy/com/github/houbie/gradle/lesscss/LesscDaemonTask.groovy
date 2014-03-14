package com.github.houbie.gradle.lesscss

import com.github.houbie.lesscss.builder.CompilationTask
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class LesscDaemonTask extends DefaultTask {
    static final String NAME = 'lesscDaemon'

    @Input
    long interval = 500

    @TaskAction
    def run() {
        CompilationTask compilationTask = project.getTasksByName(LesscTask.NAME, false).createCompilationTask()
        compilationTask.startDaemon(interval)
        System.console().readLine("Lessc daemon is running. Press enter to quit...")
        compilationTask.stopDaemon()
    }
}
