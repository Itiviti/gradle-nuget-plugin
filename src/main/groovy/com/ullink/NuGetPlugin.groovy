package com.ullink

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin

class NuGetPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.apply plugin: 'nuget-base'

        project.task('nugetRestore', type: NuGetRestore) {
            group = BasePlugin.BUILD_GROUP
            description = 'Restores the configured config file or solution directory.'
        }

        def nugetSpec = project.task('nugetSpec', type: NuGetSpec) {
            group = BasePlugin.BUILD_GROUP
            description = 'Generates the NuGet spec file.'
        }

        def nugetPack = project.task('nugetPack', type: NuGetPack, dependsOn: nugetSpec) {
            group = BasePlugin.BUILD_GROUP
            description = 'Creates the NuGet package with the configured spec file.'
        }

        project.task('nugetPush', type: NuGetPush, dependsOn: nugetPack) {
            group = BasePlugin.UPLOAD_GROUP
            description = 'Pushes the NuGet package to the configured server url.'
        }

        project.task('nugetSources', type: NuGetSources) {
            group = BasePlugin.UPLOAD_GROUP
            description = 'Adds, removes, enables, disables and lists nuget sources (feeds).'
        }
    }
}

