package com.github.houbie.gradle.lesscss

import com.github.houbie.lesscss.Options
import com.github.houbie.lesscss.builder.CompilationTask
import com.github.houbie.lesscss.builder.CompilationUnit
import com.github.houbie.lesscss.engine.LessCompilationEngineFactory
import com.github.houbie.lesscss.resourcereader.FileSystemResourceReader
import org.gradle.api.file.FileVisitDetails
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class LesscTask extends SourceDirsTask {
    static final String NAME = 'lessc'

    @Input
    @Optional
    Options options = new Options()

    @Input
    @Optional
    String engine

    @Input
    @Optional
    String lesscExecutable

    @Input
    @Optional
    String customJavaScript

    @Input
    @Optional
    String encoding

    @OutputDirectory
    def destinationDir

    @TaskAction
    def run() {
        copyResources()
        compile()
    }

    void copyResources() {
        source.visit { FileVisitDetails visitDetail ->
            if (visitDetail.directory) {
                visitDetail.relativePath.getFile(getDest()).mkdir()
            } else {
                if (!isLess(visitDetail)) {
                    logger.debug("Copying less resource ${visitDetail.file.absolutePath} to ${getDest().absolutePath}")
                    visitDetail.copyTo(visitDetail.relativePath.getFile(getDest()))
                }
            }
        }
    }

    void compile() {
        createCompilationTask().execute()
    }

    CompilationTask createCompilationTask() {
        def lessEngine = LessCompilationEngineFactory.create(getEngine(), getLesscExecutable())
        Reader customJavaScriptReader = customJavaScript ? new StringReader(getCustomJavaScript()) : null
        def compilationTask = new CompilationTask(lessEngine, (Reader) customJavaScriptReader, getCacheDir());
        compilationTask.setCompilationUnits(createCompilationUnits())
        return compilationTask
    }

    File getCacheDir() {
        new File(project.buildDir, 'lessc')
    }

    Set<CompilationUnit> createCompilationUnits() {
        def result = []
        def resourceReader = new FileSystemResourceReader(getEncoding(), sourceDirs as File[])
        source.visit { FileVisitDetails visitDetail ->
            if (!visitDetail.directory && isLess(visitDetail)) {
                def relativePathToCss = visitDetail.relativePath.replaceLastName(visitDetail.name.replace(".less", ".css"))
                def css = relativePathToCss.getFile(getDest())
                result << new CompilationUnit(visitDetail.relativePath.getPathString(), css, getOptions(), resourceReader)
            }
        }
        return result as Set
    }

    File getDest() {
        return project.file(getDestinationDir())
    }

    boolean isLess(resource) {
        resource.name.endsWith(".less")
    }
}
