package com.github.houbie.gradle.lesscss

import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import spock.lang.Specification

class IntegrationSpec extends Specification {

    File projectDir = new File('build/tmp/integrationTestProject')
    File customizedless = new File(projectDir, 'customizedless')
    ProjectConnection projectConnection

    def setup() {
        AntBuilder ant = new AntBuilder()
        ant.delete(dir: projectDir)
        ant.copy(todir: projectDir) {
            fileset(dir: 'src/test/resources')
        }

        projectConnection = GradleConnector.newConnector()
                .forProjectDirectory(projectDir)
                .connect();
    }


    def 'changed import results in recompile'() {
        File customImported1Less = new File(customizedless, 'import1/imported1.less')
        File result = new File(projectDir, 'out/import.css')

        when:
        projectConnection.newBuild().forTasks('dependsOnLessc').run();

        then:
        result.text == new File(customizedless, 'import.css').text

        when:
        sleep(1000)
        customImported1Less.text = customImported1Less.text.replace('pink', 'deeppink')
        projectConnection.newBuild().forTasks("dependsOnLessc").run();

        then:
        result.text == new File(customizedless, 'import.css').text.replace('pink', 'deeppink')
    }

    def 'task is up-to-date when ran without changes'() {
        File result = new File(projectDir, 'out/import.css')
        ByteArrayOutputStream stdout = new ByteArrayOutputStream()

        when:
        projectConnection.newBuild().forTasks('dependsOnLessc').run();

        then:
        result.text == new File(customizedless, 'import.css').text

        when:
        sleep(1000)
        projectConnection.newBuild().forTasks("dependsOnLessc").setStandardOutput(stdout).run();

        then:
        stdout.toString().contains(':lessc UP-TO-DATE\n:dependsOnLessc UP-TO-DATE')
    }
}
