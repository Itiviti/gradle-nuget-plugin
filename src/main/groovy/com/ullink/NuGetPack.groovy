package com.ullink

import groovy.util.XmlSlurper
import groovy.util.slurpersupport.GPathResult
import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile;

class NuGetPack extends BaseNuGet {

    def packageVersion
    def nuspecFile
    Closure nuspec
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
            def specSources = spec.files?.file?.collect { it.@src.text() }
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
        def version = packageVersion ?: spec.metadata.version ?: project.version
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
        nuspec = closure
    }

    Closure getNuspecCustom() {
        nuspec
    }

    GPathResult getNuspec() {
        new XmlSlurper().parse(getNuSpecFile())
    }

    // Because Nuget pack handle csproj or nuspec file we should be able to use it in plugin
    File getNuspecOrCsproj() {
        if (csprojPath) {
            return project.file(csprojPath)
        }
        getNuSpecFile()
    }

    File getNuSpecFile() {
        if (!this.nuspecFile || !project.file(this.nuspecFile).exists()) {
            this.nuspecFile = generateNuspecFile()
        }
        project.file(this.nuspecFile)
    }

    File getPackageFile() {
        def spec = getNuspec()
        def version = spec.metadata.version ?: project.version
        new File(getDestinationDir(), spec.metadata.id.toString() + '.' + version + '.nupkg')
    }

    File generateNuspecFile() {
        def nuspecFile = new File(temporaryDir, project.name + '.nuspec')
        def nuspec = supplementDefaultValueOnNuspec generateNuspec()
        nuspecFile.withWriter('UTF-8') { writer ->
            XmlUtil.serialize (nuspec, writer)
        }
        nuspecFile
    }

    String generateNuspec() {
        def builder = new StreamingMarkupBuilder()
        builder.bind {
            'package' (xmlns: 'http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd') {
                if (nuspecCustom) {
                    nuspecCustom.resolveStrategy = DELEGATE_FIRST
                    nuspecCustom.delegate = delegate
                    nuspecCustom.call()
                }
            }
        }.toString()
    }

    GPathResult supplementDefaultValueOnNuspec(String nuspecString) {

        def root = new XmlSlurper(false, false).parseText(nuspecString)

        def defaultValues = {}
        def applyDefaultValue = { String node, String value ->
            if (root.metadata[node].isEmpty()) {
                defaultValues <<= { delegate."$node" value }
            }
        }
        applyDefaultValue ('id', project.name)
        applyDefaultValue ('version', project.version)
        applyDefaultValue ('description', project.description)

        if (root.metadata.isEmpty()) {
            root.appendNode { metadata defaultValues }
        } else {
            root.metadata.appendNode defaultValues
        }
        root
    }
}
