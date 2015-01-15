package com.ullink

class NuGetRestore extends BaseNuGet {
    def packagesDirectory
    def solutionDirectory
    def packagesConfigFile
    def solutionFile

    NuGetRestore() {
        super('restore')
    }

    @Override
    void verifyCommand() {
    }

    @Override
    List<String> extraCommands() {
        def commandLineArgs = new ArrayList<String>()

        if (packagesDirectory) {
            commandLineArgs += "-PackagesDirectory"
            commandLineArgs += packagesDirectory
        }
        if (solutionDirectory) {
            commandLineArgs += "-SolutionDirectory"
            commandLineArgs += solutionDirectory
        }
        if (packagesConfigFile) {
            commandLineArgs += packagesConfigFile
        }
        if (solutionFile) {
            commandLineArgs += solutionFile
        }

        return commandLineArgs
    }
}
