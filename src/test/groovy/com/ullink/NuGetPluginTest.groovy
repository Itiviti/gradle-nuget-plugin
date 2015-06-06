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

    @Test
    public void nugetPackWorks() {
        project.nugetPack {
            basePath = project.tasks.nugetPack.temporaryDir
            nuspec {
                metadata() {
                    id 'empty-package'
                    version '1.2.3'
                    authors 'Nobody'
                    delegate.description('Here to assert nugetPack works')
                    language 'en-US'
                    projectUrl 'https://github.com/Ullink/gradle-nuget-plugin'
                }
                files() {
                    file(src: 'foo.txt')
                }
            }
        }
        project.tasks.clean.execute()
        File fooFile = new File(project.tasks.nugetPack.temporaryDir, 'foo.txt');
        fooFile.text = "Bar";
        project.tasks.nugetPack.execute()
        assertTrue(project.tasks.nugetPack.packageFile.exists())
    }
}
