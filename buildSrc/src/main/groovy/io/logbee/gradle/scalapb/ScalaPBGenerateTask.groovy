package io.logbee.gradle.scalapb

import com.github.os72.protocjar.Protoc
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException
import protocbridge.JvmGenerator
import protocbridge.ProtocBridge
import protocbridge.Target
import protocbridge.frontend.PluginFrontend
import scala.Function1
import scala.collection.JavaConverters
import scala.collection.immutable.Seq
import scalapb.ScalaPbCodeGenerator$

import java.util.zip.ZipFile

class ScalaPBGenerateTask extends DefaultTask {

    @OutputDirectory
    File outputBaseDir

    File extractedProtoDir = project.file("${project.buildDir}/tmp/proto")

    String protocVersion = "-v360"

    @TaskAction
    void generate() {

        try {

            List<String> schemas = inputs.files.collect { it.getCanonicalPath() }
            List<String> includePaths = externalProtoFiles().plus(inputs.files.collect { it.parentFile }).collect { "-I${it.getCanonicalPath()}"} as String[]

            if (outputBaseDir.exists()) {
                outputBaseDir.deleteDir()
                outputBaseDir.mkdirs()
            }

            Target target = new Target(scalapbGenerator, outputBaseDir, emptySeq)
            Integer exit = Integer.valueOf(ProtocBridge.run(protocCommandFunction, toSeq([target]), toSeq([] + includePaths + schemas), PluginFrontend.newInstance()))

            if (exit != 0) {
                throw new ScalaPBGenerationException("ProtocBridge exited with a non zero return value (" + exit + ")! See previous logs for more information.")
            }
        }
        catch (Exception e) {
            throw new TaskExecutionException(this, e)
        }
    }

    Function1<Seq<String>, String> protocCommandFunction = new Function1<Seq<String>, String>() {

        String apply(Seq<String> args) {
            List<String> argsArray = new ArrayList<String>(JavaConverters.asJavaCollection(args))
            return Protoc.runProtoc([protocVersion].plus(argsArray) as String[])
        }
    }

    private List<File> externalProtoFiles() {

        Set<File> extractedProtos = new HashSet<>()
        byte[] buffer = new byte[65536]

        project.configurations.compile.filter {
            it.path.contains("protobuf") ||
            it.path.contains("scalapb")
        }.each { file ->
            String path = file.path
            if ([".zip", ".gzip", ".gz", ".jar"].contains(path.substring(path.lastIndexOf('.'), path.length()))) {
                def fileName = file.getName()
                def baseDir = new File(extractedProtoDir, "${fileName.substring(0, fileName.lastIndexOf('.'))}")
                def zip = new ZipFile(file)
                zip.entries().each { entry ->
                    if (!entry.isDirectory() && entry.name.endsWith('.proto')) {
                        def fOut = new File(baseDir, "${entry.name}")
                        new File(fOut.parent).mkdirs()
                        def output = new FileOutputStream(fOut)
                        def input = zip.getInputStream(entry)
                        def len = 0
                        while ((len = input.read(buffer)) >= 0) {
                            output.write(buffer, 0, len)
                        }
                        output.close()
                        input.close()
                        extractedProtos.add(baseDir)
                    }
                }
                zip.close()
            }
        }

        return extractedProtos.toList()
    }

    private <T> Seq<T> toSeq(List<T> list) {
        JavaConverters.asScalaBuffer(list).toList()
    }

    private Seq emptySeq = toSeq([])

    private scalapbGenerator = new JvmGenerator("scala", ScalaPbCodeGenerator$.MODULE$)
}