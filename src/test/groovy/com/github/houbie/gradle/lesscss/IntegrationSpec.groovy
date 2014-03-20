package com.github.houbie.gradle.lesscss

import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import spock.lang.Specification

class IntegrationSpec extends Specification {

    File projectDir = new File('build/tmp/integrationTestProject')
    File lessDir = new File('src/test/resources/less')
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
        result.text == new File(lessDir, 'import.css').text

        when:
        sleep(1000)
        customImported1Less.text = customImported1Less.text + '#imported1 {color: black;}'
        projectConnection.newBuild().forTasks("dependsOnLessc").run();

        then:
        result.text == new File(customizedless, 'import.css')
    }
}
