package com.ullink

import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.StopActionException
import org.gradle.api.tasks.TaskAction

//http://docs.nuget.org/docs/reference/Command-Line-Reference#Restore_command
class NuGetRestore extends BaseNuGet {
    def projectFolder
    def packagesDirectory

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
            if (!file.exists())
            {
                logger.info("NuGet repositories.config not found at path: ${file}, running default package restore.");
                return commandLineArgs;
            }

            commandLineArgs += file
            if (packagesDirectory) {
                commandLineArgs += "-PackagesDirectory"
                commandLineArgs += packagesDirectory
            }
        }

        return commandLineArgs
    }
}
