package com.ullink

import org.gradle.api.Task

class MSBuildTaskBuilder {

    private final Task msbuildTask
    private final Map<String, Object> mainProjectProperties = [:]
    private final mainProject = new Object()
    private final List<File> artifacts = []

    public MSBuildTaskBuilder() {
        msbuildTask = [
                getName: { 'msbuild' }
        ] as Task
        mainProject.metaClass.getProperties = { mainProjectProperties }
        mainProject.metaClass.getDotnetArtifacts = { artifacts }
        msbuildTask.metaClass.getMainProject = { mainProject }
        msbuildTask.metaClass.parseProject = true
    }

    public Task build() {
        msbuildTask
    }

    public MSBuildTaskBuilder withAssemblyName(String assemblyName) {
        mainProjectProperties['AssemblyName'] = assemblyName
        this
    }

    public MSBuildTaskBuilder withFrameworkVersion(String version) {
        mainProjectProperties['TargetFrameworkVersion'] = version
        this
    }

    public MSBuildTaskBuilder withArtifact(String artifactPath) {
        artifacts.add(new File(artifactPath))
        this
    }

    public MSBuildTaskBuilder withProjectFile(String path) {
        mainProject.metaClass.getProjectFile = { path }
        this
    }

    public MSBuildTaskBuilder withProjectFile(File file) {
        withProjectFile(file.path)
    }
}
