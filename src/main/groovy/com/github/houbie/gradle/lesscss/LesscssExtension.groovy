package com.github.houbie.gradle.lesscss

import com.github.houbie.lesscss.Options

class LessCssExtension {
    static final String NAME = 'lessCss'

    Options options = new Options()

    String engine

    String lesscExecutable

    String customJavaScript

    String encoding

    def destinationDir = 'web-app/css'
}
