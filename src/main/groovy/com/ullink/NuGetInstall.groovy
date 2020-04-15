package com.ullink

import org.gradle.api.tasks.*;

class NuGetInstall extends BaseNuGet {

    @Optional
    @Input
    String packageId
    @Optional
    @InputFile
    File packagesConfigFile
    @Input
    def sources = [] as Set
    @Optional
    @OutputDirectory
    File outputDirectory
    @Optional
    @Input
    def version
    @Input
    def includeVersionInPath = true
    @Input
    def prerelease = false
    @Input
    def noCache = false
    @Input
    def requireConsent = false
    @Optional
    @InputDirectory
    File solutionDirectory
    @Optional
    @Input
    def conflictAction
    @Optional
    @InputFile
    File configFile

    NuGetInstall() {
        super('install')
    }

    void setPackagesConfigFile(String path) {
        packagesConfigFile = project.file(path)
    }

    void setOutputDirectory(String path) {
        outputDirectory = project.file(path)
    }

    void setSolutionDirectory(String path) {
        solutionDirectory = project.file(path)
    }

    void setConfigFile(String path) {
        configFile = project.file(path)
    }

    @Override
    void exec() {
        if (packageId) args packageId
        if (packagesConfigFile) args packagesConfigFile

        if (!sources.isEmpty()) args '-Source', sources.join(';')
        if (outputDirectory) args '-OutputDirectory', outputDirectory
        if (version) args '-Version', version
        if (!includeVersionInPath) args '-ExcludeVersion'
        if (prerelease) args '-Prerelease'
        if (noCache) args '-NoCache'
        if (requireConsent) args '-RequireConsent'
        if (solutionDirectory) args '-SolutionDirectory', solutionDirectory
        if (conflictAction) args '-FileConflictAction', conflictAction
        if (configFile) args '-ConfigFile', configFile

        super.exec()
    }
}
