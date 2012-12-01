package com.ullink

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.internal.os.OperatingSystem

class NuGetPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.apply plugin: 'base'
        if (OperatingSystem.current().windows) {
            def nuget = project.task('nugetPack', type: NuGetPack)
			nuget.group = BasePlugin.BUILD_GROUP
			nuget.description = 'Executes nuget pack command.'
			
            nuget = project.task('nugetPush', type: NuGetPush)
			nuget.group = BasePlugin.UPLOAD_GROUP
			nuget.description = 'Executes nuget push command.'
        }
    }
}

