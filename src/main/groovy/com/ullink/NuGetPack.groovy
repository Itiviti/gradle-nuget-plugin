package com.ullink

import groovy.util.XmlSlurper
import groovy.util.slurpersupport.GPathResult
import groovy.xml.XmlUtil
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.Task

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

        super.exec()
    }

    Task getDependentNugetSpec() {
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
        if (nuspecFile) {
            return new XmlSlurper().parse(project.file(nuspecFile))
        }
        def nugetSpec = getDependentNugetSpec()
        if (nugetSpec) {
            return new XmlSlurper(false, false).parseText(nugetSpec.generateNuspec())
        }
    }

    File getNuspecFile() {
        if (nuspecFile) {
            return project.file(this.nuspecFile)
        }
        def nugetSpec = getDependentNugetSpec()
        if (nugetSpec && nugetSpec.nuspecFile.exists()) {
            return nugetSpec.nuspecFile
        }
    }

    File getPackageFile() {
        def spec = getNuspec()
        def version = spec.metadata.version ?: project.version
        new File(getDestinationDir(), spec.metadata.id.toString() + '.' + version + '.nupkg')
    }
}
