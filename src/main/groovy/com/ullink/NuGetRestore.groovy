package com.ullink

class NuGetRestore extends BaseNuGet {
    def projectFolder
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

        if (projectFolder) {
            def file = new File(projectFolder, "repositories.config");
            if (!file.exists()) {
                logger.info("NuGet repositories.config not found at path: ${file}, running default package restore.");
            } else {
                commandLineArgs += file
            }
        }
        if (packagesDirectory) {
            commandLineArgs += "-SolutionDirectory"
            commandLineArgs += packagesDirectory
        }
        if (solutionDirectory) {
            commandLineArgs += "-PackagesDirectory"
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
