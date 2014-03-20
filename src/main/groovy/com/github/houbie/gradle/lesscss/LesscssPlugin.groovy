package com.github.houbie.gradle.lesscss

import org.gradle.api.Plugin
import org.gradle.api.Project

class LesscssPlugin implements Plugin<Project> {

    @Override
    void apply(final Project project) {
        project.task(LesscTask.NAME, type: LesscTask, group: 'Build', description: 'Compile LESS to CSS')
        project.task(LesscDaemonTask.NAME, type: LesscDaemonTask, group: 'Build', description: 'Start the LESS to CSS compiler daemon')
    }
}
