package io.logbee.gradle.scalapb

import org.gradle.api.internal.file.DefaultSourceDirectorySet
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.internal.file.collections.DefaultDirectoryFileTreeFactory

public class ScalaPBSourceDirectorySet extends DefaultSourceDirectorySet {

    public ScalaPBSourceDirectorySet(String name, FileResolver fileResolver) {
        super(name, String.format("%s ScalaPB source", name), fileResolver, new DefaultDirectoryFileTreeFactory())
        srcDir("src/${name}/proto")
        include("**/*.proto")
    }
}
