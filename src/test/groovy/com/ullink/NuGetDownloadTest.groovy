package com.ullink

import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import java.nio.file.Paths
import java.util.zip.CRC32

@RunWith(JUnit4.class)
class NuGetDownloadTest extends GroovyTestCase  {

    private File cachesFolder
    private File tempFolder

    // this file fill simulate nuget.exe, which was installed locally
    // this will be small file
    private File simulatedNugetExecutablePath

    private Project project

    @Before
    void init() {

        project = ProjectBuilder.builder().build()

        cachesFolder = Paths.get(
                project.gradle.gradleUserHomeDir.absolutePath,
                'caches',
                'nuget').toFile()

        tempFolder = Paths.get(
                project.gradle.gradleUserHomeDir.absolutePath,
                'temp').toFile()

        simulatedNugetExecutablePath = Paths.get(
                tempFolder.absolutePath,
                'nuget.exe').toFile()

        if (cachesFolder.exists()) {
            cachesFolder.deleteDir()
        }

        if (!tempFolder.exists()) {
            tempFolder.mkdir()
        }

        project.apply plugin: 'nuget'
    }

    @After
    void cleanup() {
        if (tempFolder != null && tempFolder.exists()) {
            tempFolder.deleteDir()
        }
    }

    @Test
    void shouldBeAbleToDownloadNugetWithoutAnyParameters() {
        // When
        executeSomeNugetTask()

        // Then
        Assert.assertTrue(nugetExecutableExistsInCache())
    }

    @Test
    void shouldThrowErrorOnInvalidHttpPath() {
        // When
        project.nugetPack {
            nugetExePath = "http://localhost:99999"
        }

        // Then
        def error = shouldFail {
            executeSomeNugetTask()
        }

        Assert.assertFalse(nugetExecutableExistsInCache())
    }

    /**
     * Idea of this test:
     * 1. Download nuget.exe to the some location. This simulates situation, when nuget.exe was already installed on CI
     * 2. Ask plugin just to use existing nuget.exe file from the predefined location
     * 3. Verify that:
     *    * Task was executed without exceptions (e.g. some nuget file was used)
     *    * Nuget caches folder does not have nuget file (e.g. task used some other nuget file)
     */
    @Test
    void shouldBeAbleToCopyPreviouslyDownloadedNugetFile() {
        // Given
        downloadNugetExeToTemp("https://dist.nuget.org/win-x86-commandline/v3.3.0/nuget.exe")

        // When
        project.nugetPack {
            nugetExePath = simulatedNugetExecutablePath.absolutePath
        }

        // Then
        executeSomeNugetTask()

        def cachedNugetFile = ensureSingleNuGetExeExists()

        Assert.assertNull(cachedNugetFile)
    }

    /**
     * Idea of this test:
     * 1. Ask plugin to download nuget.exe from the custom location
     * 2. Download the same file manually
     * 3. Compare checksums of files
     */
    @Test
    void shouldDownloadNugetFromCustomUrl() {
        // Given
        def customUrl = "https://dist.nuget.org/win-x86-commandline/v3.3.0/nuget.exe"

        // When
        project.nugetPack {
            nugetExePath = customUrl
        }

        // Then
        executeSomeNugetTask()

        def cachedNugetFile = ensureSingleNuGetExeExists()

        Assert.assertNotNull(cachedNugetFile)

        downloadNugetExeToTemp("https://dist.nuget.org/win-x86-commandline/v3.3.0/nuget.exe")

        assertFileAreIdential(cachedNugetFile, simulatedNugetExecutablePath)
    }

    private static void assertFileAreIdential(File expected, File actual){

        def expectedChecksum = FileUtils.checksum(expected, new CRC32()).value
        def actualChecksum = FileUtils.checksum(actual, new CRC32()).value

        Assert.assertEquals(expectedChecksum, actualChecksum)
    }

    private void downloadNugetExeToTemp(String remoteUrl){

        def nonStandardNugetUrl = new URL(remoteUrl)

        FileUtils.copyURLToFile(nonStandardNugetUrl, simulatedNugetExecutablePath)

    }

    private Boolean nugetExecutableExistsInCache() {
        return ensureSingleNuGetExeExists() != null
    }

    private File ensureSingleNuGetExeExists() {
        if (!cachesFolder.exists()) {
            return null
        }

        String[] filePatters = ["exe"]

        def allExecutables = FileUtils.listFiles(cachesFolder, filePatters, true)

        def allNugetExecutables = allExecutables.findAll {
            "nuget.exe".equalsIgnoreCase(it.name)
        }

        if (allNugetExecutables.size() == 1) {
            return allNugetExecutables[0]
        }

        if (allNugetExecutables.empty) {
            return null
        }

        throw new IllegalStateException("Too many nuget executables were downloaded: ${allExecutables.join()}")
    }

    private void executeSomeNugetTask() {
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

        project.tasks.nugetPack.exec()
    }
}
