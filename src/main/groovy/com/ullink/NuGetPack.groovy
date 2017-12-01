package com.ullink

import com.ullink.util.GradleHelper
import groovy.util.slurpersupport.GPathResult
import org.apache.commons.io.FilenameUtils
import org.gradle.api.plugins.BasePlugin

class NuGetPack extends BaseNuGet {

    def nuspecFile
    def csprojPath

    def destinationDir = project.convention.plugins.base.distsDir
    def basePath
    def exclude
    def generateSymbols = false
    def tool = false
    def build = false
    def defaultExcludes = true
    def packageAnalysis = true
    def includeReferencedProjects = false
    def includeEmptyDirectories = true
    def properties = [:]
    def minClientVersion
    def msBuildVersion

    NuGetPack() {
        super('pack')
        project.afterEvaluate {
            def spec = getNuspec()
            def specSources = spec?.files?.file?.collect { it.@src.text() }
            if (specSources && specSources.any()) {
                project.tasks.matching {
                    it.class.name.startsWith("com.ullink.Msbuild") && it.projects.values().any { specSources.contains it.properties.TargetPath }
                }.each {
                    dependsOn it
                }
            }
        }
    }

    @Override
    void exec() {
        args getNuspecOrCsproj()
        def spec = getNuspec()

        def destDir = project.file(getDestinationDir())
        if (!destDir.exists()) {
            destDir.mkdirs()
        }
        args '-OutputDirectory', destDir

        if (basePath) args '-BasePath', basePath

        def version = spec?.metadata?.version ?: project.version
        if (version) args '-Version', version

        if (exclude) args '-Exclude', exclude
        if (generateSymbols) args '-Symbols'
        if (tool) args '-Tool'
        if (build) args '-Build'
        if (!defaultExcludes) args '-NoDefaultExcludes'
        if (!packageAnalysis) args '-NoPackageAnalysis'
        if (includeReferencedProjects) args '-IncludeReferencedProjects'
        if (!includeEmptyDirectories) args '-ExcludeEmptyDirectories'
        if (!properties.isEmpty()) args '-Properties', properties.collect({ k, v -> "$k=$v" }).join(';')
        if (minClientVersion) args '-MinClientVersion', minClientVersion
        if (!msBuildVersion) msBuildVersion = GradleHelper.getPropertyFromTask(project, 'version', 'msbuild')
        if (msBuildVersion) args '-MsBuildVersion', msBuildVersion
        super.exec()
    }

    void nuspec(Closure closure) {
        if (dependentNuGetSpec) {
            dependentNuGetSpec.nuspec closure
        } else {
            def nuGetSpec = project.task("nugetSpec_$name", type: NuGetSpec)
            nuGetSpec.with {
                group = BasePlugin.BUILD_GROUP
                description = "Generates nuspec file for task $name."
                nuspec closure
            }

            dependsOn nuGetSpec
        }
    }

    NuGetSpec getDependentNuGetSpec() {
        dependsOn.find { it instanceof NuGetSpec }
    }

    // Because Nuget pack handle csproj or nuspec file we should be able to use it in plugin
    File getNuspecOrCsproj() {
        if (csprojPath) {
            return project.file(csprojPath)
        }
        getNuspecFile()
    }

    GPathResult getNuspec() {
        def nuspecFile = getNuspecFile()
        if (nuspecFile?.exists()) {
            return new XmlSlurper(false, false).parse(project.file(nuspecFile))
        }
        if (dependentNuGetSpec) {
            def generatedNuspec = dependentNuGetSpec.generateNuspec()
            if (generatedNuspec) {
                return new XmlSlurper(false, false).parseText(generatedNuspec)
            }
        }
    }

    File getNuspecFile() {
        if (nuspecFile) {
            return project.file(this.nuspecFile)
        }
        if (dependentNuGetSpec) {
            return dependentNuGetSpec.nuspecFile
        }
    }

    File getPackageFile() {
        def spec = getNuspec()
        def version = spec?.metadata?.version ?: project.version
        def id = spec?.metadata?.id?.toString() ?: getIdFromMsbuildTask()
        new File(getDestinationDir(), id + '.' + version + '.nupkg')
    }

    String getIdFromMsbuildTask() {
        def isInputProject = { csprojPath.equalsIgnoreCase(it.projectFile) }
        def msbuildTask = project.tasks.find {
            it.class.name.startsWith("com.ullink.Msbuild") && it.projects.values().any(isInputProject)
        }
        if (msbuildTask != null) {
            FilenameUtils.removeExtension(msbuildTask.projects.values().find(isInputProject).dotnetAssemblyFile.name)
        }
    }
}
