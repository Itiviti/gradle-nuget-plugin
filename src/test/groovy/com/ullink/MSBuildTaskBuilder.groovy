package com.ullink

import org.gradle.api.Project

class MSBuildTaskBuilder {
    private final Map<String, Object> mainProjectProperties = [:]
    private final Project project
    private final mainProject = new Object()
    private final List<File> artifacts = []

    MSBuildTaskBuilder(Project project) {
        this.project = project
    }

    def build() {
        mainProject.metaClass.dotnetArtifacts = artifacts
        mainProject.metaClass.properties = mainProjectProperties
        project.tasks.register('msbuild') {
            it.metaClass.mainProject = mainProject
            it.metaClass.parseProject = true
        }
    }

    MSBuildTaskBuilder withAssemblyName(String assemblyName) {
        mainProjectProperties['AssemblyName'] = assemblyName
        this
    }

    MSBuildTaskBuilder withFrameworkVersion(String version) {
        mainProjectProperties['TargetFrameworkVersion'] = version
        this
    }

    MSBuildTaskBuilder withArtifact(String artifactPath) {
        artifacts.add(new File(artifactPath))
        this
    }

    MSBuildTaskBuilder withProjectFile(String path) {
        mainProject.metaClass.getProjectFile = { path }
        this
    }

    MSBuildTaskBuilder withProjectFile(File file) {
        withProjectFile(file.path)
    }

    MSBuildTaskBuilder withMainProjectProperty(String name, String value) {
        mainProjectProperties[name] = value
        this
    }
}
