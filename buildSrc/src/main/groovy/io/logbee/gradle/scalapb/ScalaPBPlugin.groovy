package io.logbee.gradle.scalapb

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.tasks.SourceSet
import org.gradle.plugins.ide.idea.IdeaPlugin

import javax.inject.Inject

class ScalaPBPlugin implements Plugin<Project> {

    private FileResolver fileResolver

    @Inject
    ScalaPBPlugin(FileResolver fileResolver) {
        this.fileResolver = fileResolver
    }

    void apply(Project project) {

        project.extensions.create("scalapb", ScalaPBPluginExtension.class)

        project.getSourceSets().all { sourceSet ->
            sourceSet.extensions.create('proto', ScalaPBSourceDirectorySet, sourceSet.name, fileResolver)
        }

        project.getSourceSets().each { sourceSet ->
            Task compileScala = project.tasks.getByName(sourceSet.getTaskName("compile", "Scala"))
            Task processResourcesTask = project.tasks.getByName(sourceSet.getTaskName('process', 'resources'))
            File outputBaseDir = project.file("${project.buildDir}/generated/source/scalapb/${sourceSet.name}/scala")
            Set<File> protoDirs = sourceSet.proto.srcDirs.findAll{dir -> dir.exists()}
            Set<File> protoFiles = protoDirs.collect{dir -> project.fileTree(dir).files}.flatten()
            Task scalapb = project.tasks.create(scalapbTaskName(sourceSet), ScalaPBGenerateTask) {
                it.description = "Compiles protobuf '${sourceSet.name}' sources and generates scala code.'"
                it.group = "generate"
                it.protoDirs = !protoDirs.isEmpty() && !protoFiles.isEmpty() ? protoDirs : []
                it.protoFiles = !protoDirs.isEmpty() && !protoFiles.isEmpty() ? protoFiles : []
                it.outputBaseDir = outputBaseDir
            }

            compileScala.dependsOn scalapb

            processResourcesTask.from(project.tasks.getByName(scalapbTaskName(sourceSet)).inputs.files) {
                include '**/*.proto'
            }

            sourceSet.scala.srcDirs += outputBaseDir

            if (!sourceSet.proto.srcDirs.isEmpty()) {
                project.plugins.withType(IdeaPlugin) { idea ->
                    if (sourceSet.name.contains('main')) {
                        project.extensions["idea"].module.sourceDirs += sourceSet.proto.srcDirs
                    }
                    else if (sourceSet.name.contains('test')) {
                        project.extensions["idea"].module.testSourceDirs += sourceSet.proto.srcDirs
                    }
                }
            }
        }
    }

    String scalapbTaskName(SourceSet sourceSet) {
        return sourceSet.getTaskName("generate", "ScalaPB")
    }
}