package com.github.houbie.gradle.lesscss

import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.gradle.tooling.internal.consumer.DefaultGradleConnector
import spock.lang.Specification

import static java.util.concurrent.TimeUnit.SECONDS

class IntegrationSpec extends Specification {

    File projectDir = new File('build/tmp/integrationTestProject')
    File customizedless = new File(projectDir, 'customizedless')
    ProjectConnection projectConnection

    def setup() {
        AntBuilder ant = new AntBuilder()
        ant.delete(dir: projectDir)
        assert !projectDir.exists()
        ant.copy(todir: projectDir) {
            fileset(dir: 'src/test/resources')
        }

        projectConnection = ((DefaultGradleConnector) GradleConnector.newConnector()).daemonMaxIdleTime(5, SECONDS)
                .forProjectDirectory(projectDir).connect();
    }

    def cleanup() {
        projectConnection.close()
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

    def 'lessc daemon'() {
        File customImported1Less = new File(customizedless, 'import1/imported1.less')
        File result = new File(projectDir, 'out/import.css')
        def done = false
        def buildFinished = false
        def stopDaemon = false

        //the lesscDaemon task stops when it has read a byte from stdin
        InputStream stdin = new InputStream() {
            @Override
            int read() throws IOException {
                done = waitFor { stopDaemon }
                return -1
            }
        }

        Thread.startDaemon {
            projectConnection.newBuild().forTasks('lesscDaemon').setStandardInput(stdin).run();
            buildFinished = true
        }

        expect:
        waitFor { result.exists() && result.text == new File(customizedless, 'import.css').text }

        when:
        sleep(1000)
        customImported1Less.text = customImported1Less.text.replace('pink', 'deeppink')

        then:
        waitFor { result.text == new File(customizedless, 'import.css').text.replace('pink', 'deeppink') }

        when:
        stopDaemon = true

        then:
        waitFor { buildFinished }
        done
    }

    def 'lessc daemon with errors'() {
        File customImported1Less = new File(customizedless, 'import1/imported1.less')
        File result = new File(projectDir, 'out/import.css')
        ByteArrayOutputStream stdout = new ByteArrayOutputStream()
        def done = false
        def buildFinished = false
        def stopDaemon = false

        //the lesscDaemon task stops when it has read a byte from stdin
        InputStream stdin = new InputStream() {
            @Override
            int read() throws IOException {
                done = waitFor { stopDaemon }
                return -1
            }
        }

        Thread.startDaemon {
            projectConnection.newBuild().forTasks('lesscDaemon').setStandardInput(stdin).setStandardOutput(stdout).run();
            buildFinished = true
        }

        expect:
        waitFor { result.exists() && result.text == new File(customizedless, 'import.css').text }

        when:
        sleep(1000)
        customImported1Less.text = customImported1Less.text.replace('pink', '};')
        sleep(1000)
        stopDaemon = true

        then:
        waitFor { buildFinished }
        done
        stdout.toString().contains("less parse exception: missing opening `{`")
    }

    private def waitFor(condition) {
        for (i in 0..50) {
            sleep(100)
            if (condition()) {
                return true
            }
        }
        return false
    }
}
