package com.ullink

import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.StopActionException
import org.gradle.api.tasks.TaskAction

class NuGetRestore extends BaseNuGet {
    def projectFolder
    def restoreFolder

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
            if (restoreFolder) {
                commandLineArgs += "-RestoreFolder"
                commandLineArgs += restoreFolder
            }
        }

        return commandLineArgs
    }
}
