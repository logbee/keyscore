package io.logbee.gradle.scalapb

class ScalaPBPluginExtension {

    List<String> dependentProtoSources;
    String protocVersion;
    String targetDir;
    String projectProtoSourceDir;
    String extractedIncludeDir;
    Boolean grpc;
    Boolean embeddedProtoc;
}