package io.logbee.gradle.scalapb

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.tasks.SourceSet

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
            Task scalapb = project.tasks.create(scalapbTaskName(sourceSet), ScalaPBGenerateTask) {
                it.description = "Compiles protobuf '${sourceSet.name}' sources and generates scala code.'"
                it.inputs.files(sourceSet.proto)
                it.outputBaseDir = outputBaseDir
            }

            compileScala.dependsOn scalapb

            processResourcesTask.from(project.tasks.getByName(scalapbTaskName(sourceSet)).inputs.files) {
                include '**/*.proto'
            }

            sourceSet.scala.srcDirs += outputBaseDir
        }
    }

    String scalapbTaskName(SourceSet sourceSet) {
        return sourceSet.getTaskName("generate", "ScalaPB")
    }
}