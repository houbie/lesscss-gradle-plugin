package com.github.houbie.gradle.lesscss

import com.github.houbie.lesscss.Options
import com.github.houbie.lesscss.builder.CompilationTask
import com.github.houbie.lesscss.builder.CompilationUnit
import com.github.houbie.lesscss.engine.LessCompilationEngineFactory
import com.github.houbie.lesscss.resourcereader.FileSystemResourceReader
import org.gradle.api.GradleException
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

    def destinationDir

    @OutputDirectory
    File getDestinationDir() {
        project.file(destinationDir)
    }

    @Input
    @Optional
    Closure preCompileClosure

    void preCompile(Closure preCompileClosure) {
        if (preCompileClosure && preCompileClosure.parameterTypes.size() != 2) {
            throw new GradleException("The preCompile closure of the lessc task needs to accept 2 parameters:" +
                    " a org.gradle.api.file.FileTreeElement (the source file) and a com.github.houbie.lesscss.builder.CompilationUnit (config that can be modified)")
        }
        this.preCompileClosure = preCompileClosure
    }

    @TaskAction
    def run() {
        copyResources()
        compile()
    }

    void copyResources() {
        source.visit { FileVisitDetails visitDetail ->
            if (visitDetail.directory) {
                visitDetail.relativePath.getFile(getDestinationDir()).mkdir()
            } else {
                if (!isLess(visitDetail)) {
                    logger.debug("Copying less resource ${visitDetail.file.absolutePath} to ${getDestinationDir().absolutePath}")
                    visitDetail.copyTo(visitDetail.relativePath.getFile(getDestinationDir()))
                }
            }
        }
    }

    void compile() {
        logger.debug("execute less CompilationTask")
        createCompilationTask().execute()
    }

    CompilationTask createCompilationTask() {
        logger.debug("create less CompilationTask")
        def lessEngine = LessCompilationEngineFactory.create(engine, lesscExecutable)
        Reader customJavaScriptReader = customJavaScript ? new StringReader(customJavaScript) : null
        def compilationTask = new CompilationTask(lessEngine, (Reader) customJavaScriptReader, cacheDir);
        compilationTask.setCompilationUnits(createCompilationUnits())
        return compilationTask
    }

    File getCacheDir() {
        new File(project.buildDir, 'lessc')
    }

    Set<CompilationUnit> createCompilationUnits() {
        def result = []
        def resourceReader = new FileSystemResourceReader(encoding, sourceDirs as File[])
        source.visit { FileVisitDetails visitDetail ->
            if (!visitDetail.directory && isLess(visitDetail)) {
                def relativePathToCss = visitDetail.relativePath.replaceLastName(visitDetail.name.replace(".less", ".css"))
                def dest = relativePathToCss.getFile(getDestinationDir())
                def relativePathToSourceMap = visitDetail.relativePath.replaceLastName(visitDetail.name.replace(".less", ".map"))
                def sourceMapDest = relativePathToSourceMap.getFile(getDestinationDir())
                def src = visitDetail.relativePath.getPathString()
                logger.debug("Creating less CompilationUnit src: $src, dest $dest")
                CompilationUnit compilationUnit = new CompilationUnit(src, dest, new Options(options), resourceReader, sourceMapDest)
                if (preCompileClosure) {
                    preCompileClosure(visitDetail, compilationUnit)
                }
                result << compilationUnit
            }
        }
        return result as Set
    }

    boolean isLess(resource) {
        resource.name.endsWith(".less")
    }
}
