package com.github.houbie.gradle.lesscss

import org.gradle.api.file.FileTree
import org.gradle.api.file.FileTreeElement
import org.gradle.api.internal.ConventionTask
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.api.tasks.util.PatternSet

/**
 * A {@code SourceDirsTask} performs some operation on files within source directories.
 */
class SourceDirsTask extends ConventionTask implements PatternFilterable {
    private final List sourceDirs = []
    private final PatternFilterable patternSet = new PatternSet()

    /**
     * Returns the source for this task, after the include and exclude patterns have been applied to the source directories. Ignores source files which do not exist.
     *
     * @return The source.
     */
    @InputFiles
    @SkipWhenEmpty
    FileTree getSource() {
        project.files(sourceDirs ?: null).getAsFileTree().matching(patternSet)
    }

    List<File> getSourceDirs() {
        sourceDirs.collect { project.file(it) }

    }

    /**
     * Sets the source directory for this task. The given dir object is evaluated as per {@link org.gradle.api.Project#files(Object ...)}.
     *
     * @param sourceDir The source.
     */
    void setSourceDir(Object sourceDir) {
        sourceDirs.clear()
        sourceDirs << sourceDir
        inputs.sourceDir(sourceDir)
    }

    /**
     * Adds some source directories to this task. The given dir objects will be evaluated as per {@link org.gradle.api.Project#files(Object ...)}.
     *
     * @param sourceDirs The source to add
     * @return this
     */
    SourceDirsTask sourceDir(Object... sourceDirs) {
        for (sourceDir in sourceDirs) {
            this.sourceDirs << sourceDir
            inputs.sourceDir(sourceDir)
        }
        return this
    }

    /**
     * {@inheritDoc}
     */
    SourceDirsTask include(String... includes) {
        patternSet.include(includes)
        return this
    }

    /**
     * {@inheritDoc}
     */
    SourceDirsTask include(Iterable<String> includes) {
        patternSet.include(includes)
        return this
    }

    /**
     * {@inheritDoc}
     */
    SourceDirsTask include(Spec<FileTreeElement> includeSpec) {
        patternSet.include(includeSpec)
        return this
    }

    /**
     * {@inheritDoc}
     */
    SourceDirsTask include(Closure includeSpec) {
        patternSet.include(includeSpec)
        return this
    }

    /**
     * {@inheritDoc}
     */
    SourceDirsTask exclude(String... excludes) {
        patternSet.exclude(excludes)
        return this
    }

    /**
     * {@inheritDoc}
     */
    SourceDirsTask exclude(Iterable<String> excludes) {
        patternSet.exclude(excludes)
        return this
    }

    /**
     * {@inheritDoc}
     */
    SourceDirsTask exclude(Spec<FileTreeElement> excludeSpec) {
        patternSet.exclude(excludeSpec)
        return this
    }

    /**
     * {@inheritDoc}
     */
    SourceDirsTask exclude(Closure excludeSpec) {
        patternSet.exclude(excludeSpec)
        return this
    }

    /**
     * {@inheritDoc}
     */
    Set<String> getIncludes() {
        return patternSet.includes
    }

    /**
     * {@inheritDoc}
     */
    SourceDirsTask setIncludes(Iterable<String> includes) {
        patternSet.includes = includes
        return this
    }

    /**
     * {@inheritDoc}
     */
    Set<String> getExcludes() {
        return patternSet.excludes
    }

    /**
     * {@inheritDoc}
     */
    SourceDirsTask setExcludes(Iterable<String> excludes) {
        patternSet.excludes = excludes
        return this
    }
}