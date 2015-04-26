package com.ullink

import org.gradle.api.tasks.Exec

import static org.apache.tools.ant.taskdefs.condition.Os.*

public class BaseNuGet extends Exec {
    private static final String NUGET_EXE = 'NuGet.exe'
    protected File localNuget = new File(temporaryDir, NUGET_EXE)
    
    String verbosity

    public BaseNuGet() {
        if (!localNuget.exists()) {
            new URL('https://nuget.org/nuget.exe').withInputStream { i ->
                localNuget.withOutputStream{ it << i }
            }
        }
    }

    protected BaseNuGet(String command) {
        this()
        args command
    }

    @Override
    void exec() {
        if (isFamily(FAMILY_WINDOWS)) {
            executable localNuget
        } else {
            executable "mono"
            setArgs([localNuget.path, *getArgs()])
        }

        args '-NonInteractive'
        args '-Verbosity', (verbosity ?: getNugetVerbosity())

        super.exec()
    }

    private String getNugetVerbosity() {
        if (logger.debugEnabled) return 'detailed'
        if (logger.infoEnabled) return 'normal'
        return 'quiet'
    }
}
