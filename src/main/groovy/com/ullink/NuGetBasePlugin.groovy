package com.ullink

import org.gradle.api.Plugin
import org.gradle.api.Project

class NuGetBasePlugin implements Plugin<Project> {
    void apply(Project project) {
        project.apply plugin: 'base'
        project.extensions.create('nuget', NuGetExtension)
    }
}
