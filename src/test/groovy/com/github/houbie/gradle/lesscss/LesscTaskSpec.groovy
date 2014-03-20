package com.github.houbie.gradle.lesscss

import com.github.houbie.lesscss.LessParseException
import com.github.houbie.lesscss.Options
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class LesscTaskSpec extends Specification {
    File projectDir = new File('build/tmp/testProject')
    String projectRelativeLessDir = '../../../src/test/resources/less'
    File lessDir = new File('src/test/resources/less')
    Project project

    def setup() {
        projectDir.mkdirs()
        project = ProjectBuilder.builder().withProjectDir(projectDir).build()
        project.apply plugin: 'lesscss'
        project.delete('out')
    }

    def 'lessc task defaults'() {
        def lesscTask = project.tasks.findByName('lessc')

        expect:
        lesscTask != null
        lesscTask.description == 'Compile LESS to CSS'
        lesscTask.options == new Options()
        lesscTask.engine == null
        lesscTask.lesscExecutable == null
        lesscTask.customJavaScript == null
        lesscTask.encoding == null
    }

    def 'configured lessc task'() {
        project.lessc {
            options.rootpath = 'myRootpath'
            engine = 'myEngine'
            lesscExecutable = 'myLesscExecutable'
            customJavaScript = 'myCustomJs'
            encoding = 'myEncoding'
            destinationDir = 'out'
        }

        def lesscTask = project.tasks.findByName('lessc')

        expect:
        lesscTask.options == new Options(rootpath: 'myRootpath')
        lesscTask.engine == 'myEngine'
        lesscTask.lesscExecutable == 'myLesscExecutable'
        lesscTask.customJavaScript == 'myCustomJs'
        lesscTask.encoding == 'myEncoding'
        lesscTask.destinationDir.absoluteFile == new File(projectDir, 'out').absoluteFile
    }

    def 'create custom lessc task'() {
        project.task(type: LesscTask, 'customLessc') {
            options.rootpath = 'customRootpath'
        }

        def customLesscTask = project.tasks.findByName('customLessc')

        expect:
        customLesscTask.options == new Options(rootpath: 'customRootpath')
        customLesscTask.engine == null
        customLesscTask.lesscExecutable == null
        customLesscTask.customJavaScript == null
        customLesscTask.encoding == null
    }

    def 'compile less files'() {
        project.lessc {
            destinationDir = project.file('out')
            sourceDir projectRelativeLessDir
            include 'import.less', 'basic.less'
            include '*resource.*'
        }

        project.tasks.findByName('lessc').run()

        expect:
        new File(projectDir, 'out').list().sort() == ['basic-resource.txt', 'basic.css', 'import.css']
        new File(projectDir, 'out/basic.css').text == new File(lessDir, 'basic.css').text
        new File(projectDir, 'out/import.css').text == new File(lessDir, 'import.css').text
    }

    def 'compile broken less'() {
        project.lessc {
            destinationDir = project.file('out')
            sourceDir = projectRelativeLessDir
            include 'broken.less'
        }

        when:
        project.tasks.findByName('lessc').run()

        then:
        LessParseException e = thrown()
        e.message == "less parse exception: missing closing `}`\n" +
                "in broken.less at line 1\n" +
                "extract\n" +
                "#broken less {"
    }

    def 'compile and minify'() {
        project.lessc {
            options.minify = true
            destinationDir = project.file('out')
            sourceDir = projectRelativeLessDir
            include 'minify.less'
        }

        when:
        project.tasks.findByName('lessc').run()

        then:
        new File(projectDir, 'out/minify.css').text == new File(lessDir, 'minify.css').text
    }
}
