package com.github.houbie.gradle.lesscss

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class LesscssPluginSpec extends Specification {
    Project project

    def 'apply plugin'() {
        project = ProjectBuilder.builder().build()

        expect:
        project.tasks.findByName(LesscTask.NAME) == null
        project.tasks.findByName(LesscDaemonTask.NAME) == null

        when:
        project.apply plugin: 'lesscss'

        then:
        project.tasks.findByName(LesscTask.NAME) instanceof LesscTask
        project.tasks.findByName(LesscDaemonTask.NAME) instanceof LesscDaemonTask
    }
}
