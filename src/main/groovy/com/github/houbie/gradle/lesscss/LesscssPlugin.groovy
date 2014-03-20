package com.github.houbie.gradle.lesscss

import org.gradle.api.Plugin
import org.gradle.api.Project

class LesscssPlugin implements Plugin<Project> {

    @Override
    void apply(final Project project) {
        project.extensions.create(LessCssExtension.NAME, LessCssExtension)

        project.task(LesscTask.NAME, type: LesscTask, group: 'Build', description: 'Compile LESS to CSS') {
            LessCssExtension extension = project.extensions.findByName(LessCssExtension.NAME)
            conventionMapping.options = { extension.options }
            conventionMapping.engine = { extension.engine }
            conventionMapping.lesscExecutable = { extension.lesscExecutable }
            conventionMapping.customJavaScript = { extension.customJavaScript }
            conventionMapping.encoding = { extension.encoding }
            conventionMapping.destinationDir = { extension.destinationDir }
        }

        project.task(LesscDaemonTask.NAME, type: LesscDaemonTask, group: 'Build', description: 'Start the LESS to CSS compiler daemon')
    }
}
