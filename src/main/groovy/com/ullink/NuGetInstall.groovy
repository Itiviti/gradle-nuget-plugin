package com.ullink

import org.gradle.api.tasks.*;

class NuGetInstall extends BaseNuGet {

    @Input
    def packageId
    @InputFile
    File packagesConfigFile
    @Input
    def sources = [] as Set
    @OutputDirectory
    File outputDirectory
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
    @InputDirectory
    File solutionDirectory
    @Input
    def conflictAction
    @Input
    def configFile

    NuGetInstall() {
        super('install')
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
