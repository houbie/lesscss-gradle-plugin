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
