package com.github.houbie.gradle.lesscss

import com.github.houbie.lesscss.Options
import com.github.houbie.lesscss.builder.CompilationTask
import com.github.houbie.lesscss.builder.CompilationUnit
import com.github.houbie.lesscss.engine.LessCompilationEngineFactory
import com.github.houbie.lesscss.resourcereader.FileSystemResourceReader
import org.gradle.api.PathValidation
import org.gradle.api.file.FileTree
import org.gradle.api.file.FileVisitDetails
import org.gradle.api.internal.file.collections.SimpleFileCollection
import org.gradle.api.tasks.*

class LesscTask extends SourceTask {
    static final String NAME = 'lessc'

    @Input
    Options options = new Options()

    @Input
    String engine

    @Input
    String lesscExecutable

    @Input
    String customJavaScript

    @Input
    String encoding

    @Input
    def sourceLocations = []

    @Input
    def includePaths = []

    @OutputDirectory
    def dest

    File getDest() {
        project.file(dest)
    }

    Collection<File> getIncludePaths() {
        includePaths.collect { project.file(it, PathValidation.DIRECTORY) }
    }

    @Override
    @InputFiles
    @SkipWhenEmpty
    public FileTree getSource() {
        super.getSource() + resolveSourceLocations()
    }

    FileTree resolveSourceLocations() {
        List<File> files = []
        for (location in sourceLocations) {
            for (File path in getIncludePaths()) {
                File file = new File(path, location.toString())
                if (file.exists()) {
                    files << file
                    break
                }
            }
        }
        return new SimpleFileCollection(files).getAsFileTree()
    }

    @TaskAction
    def run() {
        copyResources()
        compile()
    }

    protected void copyResources() {
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

    protected void compile() {
        createCompilationTask().execute()
    }

    protected CompilationTask createCompilationTask() {
        def lessEngine = LessCompilationEngineFactory.create(engine, lesscExecutable)
        Reader customJavaScriptReader = customJavaScript ? new StringReader(customJavaScript) : null
        def compilationTask = new CompilationTask(lessEngine, (Reader) customJavaScriptReader, new File(project.buildDir, 'lessc'));
        compilationTask.setCompilationUnits(createCompilationUnits())
        return compilationTask
    }

    protected Set<CompilationUnit> createCompilationUnits() {
        def result = []
        source.visit { FileVisitDetails visitDetail ->
            if (!visitDetail.directory && isLess(visitDetail)) {
                def relativePathToCss = visitDetail.relativePath.replaceLastName(visitDetail.name.replace(".less", ".css"))
                result << createCompilationUnit(visitDetail.file, relativePathToCss.getFile(getDest()))
            }
        }
        return result as Set
    }

    protected CompilationUnit createCompilationUnit(File source, File destination) {
        def paths = includePaths << source.parentFile
        def resourceReader = new FileSystemResourceReader(encoding, paths as File[])
        return new CompilationUnit(source.path, destination, options, resourceReader)
    }

    boolean isLess(resource) {
        resource.name.endsWith(".less")
    }

}
