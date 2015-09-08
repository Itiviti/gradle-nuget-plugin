package com.ullink

import groovy.util.XmlSlurper
import groovy.util.slurpersupport.GPathResult
import groovy.xml.XmlUtil
import org.gradle.api.plugins.BasePlugin
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
            return new XmlSlurper().parse(project.file(nuspecFile))
        }
        if (dependentNuGetSpec) {
            return new XmlSlurper(false, false).parseText(dependentNuGetSpec.generateNuspec())
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
        def version = spec.metadata.version ?: project.version
        new File(getDestinationDir(), spec.metadata.id.toString() + '.' + version + '.nupkg')
    }
}
