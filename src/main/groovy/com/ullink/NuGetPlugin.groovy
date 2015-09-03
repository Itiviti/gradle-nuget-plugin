package com.ullink

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin

class NuGetPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.apply plugin: 'base'

        def nuget = project.task('nugetRestore', type: NuGetRestore)
        nuget.group = BasePlugin.BUILD_GROUP
        nuget.description = 'Executes nuget package restore command.'

        def nugetSpec = project.task('nugetSpec', type: NuGetSpec)
        nugetSpec.group = BasePlugin.BUILD_GROUP
        nugetSpec.description = 'Generates nuspec file.'

        def nugetPack = project.task('nugetPack', type: NuGetPack)
        nugetPack.group = BasePlugin.BUILD_GROUP
        nugetPack.description = 'Executes nuget pack command.'
        nugetPack.dependsOn nugetSpec

        def nugetPush = project.task('nugetPush', type: NuGetPush)
        nugetPush.group = BasePlugin.UPLOAD_GROUP
        nugetPush.description = 'Executes nuget push command.'
        nugetPush.dependsOn nugetPack
    }
}

