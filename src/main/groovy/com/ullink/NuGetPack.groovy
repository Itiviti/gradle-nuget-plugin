package com.ullink

import com.ullink.util.GradleHelper
import groovy.util.slurpersupport.GPathResult
import org.apache.commons.io.FilenameUtils
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.*

class NuGetPack extends BaseNuGet {
    File nuspecFile

    @Optional
    @InputFile
    File csprojPath

    @OutputDirectory
    File destinationDir = new File(project.buildDir, project.convention.plugins.base.distsDirName)
    @InputFile
    File basePath
    @Input
    def packageVersion
    @Optional
    @Input
    def exclude
    @Input
    def generateSymbols = false
    @Input
    def tool = false
    @Input
    def build = false
    @Input
    def defaultExcludes = true
    @Input
    def packageAnalysis = true
    @Input
    def includeReferencedProjects = false
    @Input
    def includeEmptyDirectories = true
    @Input
    def properties = [:]
    @Optional
    @Input
    def minClientVersion
    @Optional
    @Input
    def msBuildVersion

    NuGetPack() {
        super('pack')
        // Force always execute
        outputs.upToDateWhen { false }

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

    void setDestinationDir(String path) {
        destinationDir = project.file(path)
    }

    void setNuspecFile(String path) {
        nuspecFile = project.file(path)
    }

    void setCsprojPath(String path) {
        csprojPath = project.file(path)
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

        def version = getFinalPackageVersion(spec)
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

    @Internal
    NuGetSpec getDependentNuGetSpec() {
        dependsOn.find { it instanceof NuGetSpec } as NuGetSpec
    }

    // Because Nuget pack handle csproj or nuspec file we should be able to use it in plugin
    @InputFile
    File getNuspecOrCsproj() {
        csprojPath ? csprojPath : getNuspecFile()
    }

    @Internal
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

    @Internal
    File getNuspecFile() {
        if (nuspecFile) {
            return nuspecFile
        }
        if (dependentNuGetSpec) {
            return dependentNuGetSpec.nuspecFile
        }
    }

    @OutputFile
    File getPackageFile() {
        def spec = getNuspec()
        def version = getFinalPackageVersion(spec)
        def id = spec?.metadata?.id?.toString() ?: getIdFromMsbuildTask()
        new File(getDestinationDir(), id + '.' + version + '.nupkg')
    }

    private String getFinalPackageVersion(spec) {
        return packageVersion ?: spec?.metadata?.version ?: project.version
    }

    @Input
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
