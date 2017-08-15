package com.ullink

class NuGetInstall extends BaseNuGet {

    def packageId
    def packagesConfigFile

    def sources = [] as Set
    def outputDirectory
    def version
    def includeVersionInPath = true
    def prerelease = false
    def noCache = false
    def requireConsent = false
    def solutionDirectory
    def conflictAction
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
