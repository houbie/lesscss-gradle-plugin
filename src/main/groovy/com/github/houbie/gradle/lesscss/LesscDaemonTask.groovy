/*
 * Copyright (c) 2014 Houbrechts IT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.houbie.gradle.lesscss

import com.github.houbie.lesscss.builder.CompilationTask
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

class LesscDaemonTask extends DefaultTask {
    static final String NAME = 'lesscDaemon'

    String lesscTaskName = LesscTask.NAME

    long interval = 500

    String engine

    String lesscExecutable

    @TaskAction
    def run() {
        CompilationTask compilationTask = lesscTask.createCompilationTask(engine, lesscExecutable)
        logger.info("starting lessc daemon...")
        compilationTask.startDaemon(interval)

        def msg = "Lessc daemon is running. Press enter to quit..."
        if (System.console()) {
            System.console().readLine(msg)
        } else {
            println msg
            System.in.read()
        }
        compilationTask.stopDaemon()
        logger.info("lessc daemon stopped")
    }

    LesscTask getLesscTask() {
        for (lesscTask in project.getTasksByName(lesscTaskName, false)) {
            if (lesscTask instanceof LesscTask) {
                return lesscTask
            }
        }
        throw new GradleException("Task $lesscTaskName cannot be found and is required to start the lessc daemon")
    }
}
