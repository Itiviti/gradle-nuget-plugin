package com.ullink

import static org.junit.Assert.*
import groovy.xml.MarkupBuilder
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test


class NuGetPluginTest {
    
    @Test
    public void nugetPluginAddsNuGetTaskToProject() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'nuget'
        assertTrue(project.tasks.nugetPack instanceof NuGetPack)
        assertTrue(project.tasks.nugetPush instanceof NuGetPush)
    }
	
	@Test
	public void nugetTaskExecute() {
		Project project = ProjectBuilder.builder().build()
		project.apply plugin: 'nuget'
		project.task('nuget', type: BaseNuGet) {
			command = 'help'
		}
		project.tasks.nuget.execute()
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
