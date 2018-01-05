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
        assertTrue(project.tasks.nugetSpec instanceof NuGetSpec)
        assertTrue(project.tasks.nugetSources instanceof NuGetSources)
    }

    @Test
    public void nugetPackWorks() {

        project.tasks.clean.execute()

        File nuspec = new File(project.tasks.nugetPack.temporaryDir, 'foo.nuspec')
        nuspec.text = '''<?xml version='1.0'?>
<package xmlns='http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd'>
  <metadata>
    <id>foo</id>
    <authors>Nobody</authors>
    <version>1.2.3</version>
    <description>fooDescription</description>
  </metadata>
  <files>
    <file src='foo.txt' />
  </files>
</package>'''

        File fooFile = new File(project.tasks.nugetPack.temporaryDir, 'foo.txt')
        fooFile.text = "Bar"

        project.nugetPack {
            basePath = project.tasks.nugetPack.temporaryDir
            nuspecFile = nuspec
        }

        project.tasks.nugetPack.execute()
        assertTrue(project.tasks.nugetPack.packageFile.exists())
    }
}
