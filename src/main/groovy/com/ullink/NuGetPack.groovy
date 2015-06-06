package com.ullink

import groovy.util.slurpersupport.GPathResult
import groovy.xml.MarkupBuilder
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile;

class NuGetPack extends BaseNuGet {

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

        // TODO inputs/outputs
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

        def version = spec.metadata.version ?: project.version
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
    @InputFile
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

    @OutputFile
    File getPackageFile() {
        def spec = getNuspec()
        def version = spec.metadata.version ?: project.version
        new File(getDestinationDir(), spec.metadata.id.toString() + '.' + version + '.nupkg')
    }

    File generateNuspecFile() {
        File nuspecFile = new File(temporaryDir, project.name + '.nuspec')
        nuspecFile.withWriter("UTF-8") { writer ->
            def builder = new MarkupBuilder(writer)
            builder.mkp.xmlDeclaration(version:'1.0')
            builder.'package'(xmlns: 'http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd') {
                if (nuspecCustom) {
                    nuspecCustom.resolveStrategy = DELEGATE_FIRST
                    nuspecCustom.delegate = delegate
                    nuspecCustom.call()
                } else {
                    // default content ?
                    metadata() {
                        id project.name
                        version project.version
                        description project.description
                    }
                    files() {
                        // ...
                    }
                }
            }
        }
        nuspecFile
    }
}
