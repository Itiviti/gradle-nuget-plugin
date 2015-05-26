package com.ullink

import org.junit.Before

import static org.junit.Assert.*
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class NuGetPluginTest {

    private Project project

    @Before
    public void init() {
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'nuget'
    }

    @Test
    public void nugetPluginAddsNuGetTasksToProject() {
        assertTrue(project.tasks.nugetPack instanceof NuGetPack)
        assertTrue(project.tasks.nugetPush instanceof NuGetPush)
    }

    @Test
    public void nugetHelpTaskExecute() {
        project.task('nuget', type: BaseNuGet) {
            args 'help'
        }
        project.tasks.nuget.execute()
    }

    @Test
    public void nugetWithCleanWorks() {
        project.task('nuget', type: BaseNuGet) {
            args 'help'
        }
        project.tasks.clean.execute()
        project.tasks.nuget.execute()
    }

    @Test
    public void nugetPackGenerateNuspec() {
        project.nugetPack {
            nuspec {
                metadata() {
                    id 'foo'
                }
            }
        }
        project.tasks.nugetPack.generateNuspecFile()
    }
}
