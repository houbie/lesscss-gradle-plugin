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

import groovy.io.FileType
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class SourceDirsTaskSpec extends Specification {
    File projectDir = new File('.')
    Project project

    def setup() {
        project = ProjectBuilder.builder().withProjectDir(projectDir).build()
        project.apply plugin: 'lesscss'
    }

    def sourceDir() {
        project.lessc {
            sourceDir 'src/test/resources'
            sourceDir 'src/test/groovy'
            sourceDir 'src/test/resources', new File('src/main/groovy')
            sourceDir 'doesNotExist'
        }

        expect:
        project.tasks.findByName('lessc').sourceDirs.absoluteFile ==
                [new File('src/test/resources'), new File('src/test/groovy'), new File('src/test/resources'), new File('src/main/groovy'), new File('doesNotExist')].absoluteFile
    }

    def setSourceDir() {
        project.lessc {
            sourceDir = 'src/test/resources'
        }

        expect:
        project.tasks.findByName('lessc').sourceDirs.absoluteFile == [new File('src/test/resources')].absoluteFile
    }

    def 'sources without includes'() {
        def dir = 'src/test/resources/less'
        def fileCount = 0
        new File(dir).eachFileRecurse(FileType.FILES) { fileCount++ }

        project.lessc {
            sourceDir dir
        }

        expect:
        project.tasks.findByName('lessc').source.iterator().toList().size() == fileCount
    }

    def 'sources with includes and excludes'() {
        project.lessc {
            sourceDir 'src/test/resources/less', 'src/test/resources/customizedless', 'doesNotExist'

            include 'basic.*', 'import1/**/import*.less'
            exclude '*.css', '*.map'
        }

        expect:
        project.tasks.findByName('lessc').source.collect {
            it.absoluteFile
        } == [new File('src/test/resources/less/basic.less'),
                new File('src/test/resources/less/import1/imported1.less'),
                new File('src/test/resources/less/import1/import2/imported2.less'),
                new File('src/test/resources/customizedless/import1/imported1.less'),].absoluteFile
    }
}
