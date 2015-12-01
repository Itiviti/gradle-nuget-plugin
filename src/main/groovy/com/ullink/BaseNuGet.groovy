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
        def nugetHome = env['NUGET_HOME']
        if(nugetHome != null)
            return new File(nugetHome)
        else
            return new File(new File(new File(project.gradle.gradleUserHomeDir, 'caches'), 'nuget'), project.extensions.nuget.version)
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
            def nugetUrl = "https://dist.nuget.org/win-x86-commandline/v${project.extensions.nuget.version}/nuget.exe"
            project.logger.debug "Downloading NuGet from $nugetUrl ..."
            new URL(nugetUrl).withInputStream { i -> localNuget.withOutputStream{ it << i } }
        }

        project.logger.debug "Using NuGet from path $localNuget.path"
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
