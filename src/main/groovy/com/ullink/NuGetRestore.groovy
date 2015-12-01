package com.ullink

class NuGetRestore extends BaseNuGet {

    def solutionFile
    def packagesConfigFile

    def source
    def noCache = false
    def configFile
    def requireConsent = false
    def packagesDirectory
    def solutionDirectory
    def disableParallelProcessing = false

    NuGetRestore() {
        super('restore')
    }

    @Override
    void exec() {
        if (packagesConfigFile) args packagesConfigFile
        if (solutionFile) args solutionFile

        if (source) args '-Source', source
        if (noCache) args '-NoCache'
        if (configFile) args '-ConfigFile', configFile
        if (requireConsent) args '-RequireConsent'
        if (packagesDirectory) args '-PackagesDirectory', packagesDirectory
        if (solutionDirectory) args '-SolutionDirectory', solutionDirectory
        if (disableParallelProcessing) args '-DisableParallelProcessing'

        project.logger.info "Restoring NuGet packages " +
            (source ? "from $source" : '') +
            (packagesConfigFile ? "for packages.config ($packagesConfigFile)": '') +
            (solutionFile ? "for solution file ($solutionFile)" : '')
        super.exec()
    }

    def getPackagesFolder() {
        // https://docs.nuget.org/consume/command-line-reference#restore-command
        // If -PackagesDirectory <packagesDirectory> is specified, <packagesDirectory> is used as the packages directory.
        if (packagesDirectory) {
            return packagesDirectory
        }

        // If -SolutionDirectory <solutionDirectory> is specified, <solutionDirectory>\packages is used as the packages directory.
        // SolutionFile can also be provided.
        // Otherwise use '.\packages'
        def solutionDir = solutionFile ? solutionFile.getParent() : solutionDirectory
        return new File(solutionDir ?: '.', 'packages').absolutePath
    }
}
