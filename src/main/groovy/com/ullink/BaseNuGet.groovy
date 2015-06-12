package com.ullink

import org.gradle.api.tasks.Exec

import static org.apache.tools.ant.taskdefs.condition.Os.*

public class BaseNuGet extends Exec {
    private static final String NUGET_EXE = 'NuGet.exe'

    String verbosity

    public BaseNuGet() {
    }

    private File getNugetHome(){
        def env = System.getenv()
        def nugethome = env['NUGET_HOME']
        if( nugethome != null)
        {
            return new File(nugethome)
        }
        else
        {
            return new File(new File(new File(project.gradle.gradleUserHomeDir, 'caches'), 'nuget'), '2')
        }
    }

    protected BaseNuGet(String command) {
        this()
        args command
    }

    @Override
    void exec() {
        def folder = getNugetHome()
        def localNuget = new File(folder, NUGET_EXE)
        if (!localNuget.exists()) {
            if (!folder.isDirectory())
                folder.mkdirs()
            new URL('https://nuget.org/nuget.exe').withInputStream { i ->
                localNuget.withOutputStream{ it << i }
            }
        }
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
