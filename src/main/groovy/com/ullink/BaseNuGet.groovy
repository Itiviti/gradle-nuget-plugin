package com.ullink

import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Console;
import org.gradle.api.tasks.Input;
import java.nio.file.Paths

import static org.apache.tools.ant.taskdefs.condition.Os.*

class BaseNuGet extends Exec {
    private static final String NUGET_EXE = 'NuGet.exe'

    @Console
    String verbosity

    @Input
    String nugetExePath

    BaseNuGet() {
    }

    private File getNugetHome() {
        def env = System.getenv()
        def nugetHome = env['NUGET_HOME']
        if (nugetHome != null) {
            return new File(nugetHome)
        } else {
            def nugetCacheFolder = Paths.get(
                    project.gradle.gradleUserHomeDir.absolutePath,
                    'caches',
                    'nuget',
                    project.extensions.nuget.version.toString())

            return nugetCacheFolder.toFile()
        }
    }

    protected BaseNuGet(String command) {
        this()
        args command
    }

    @Override
    void exec() {
        File localNuget = getNugetExeLocalPath()

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

    private File getNugetExeLocalPath() {
        File localNuget

        if (nugetExePath != null && !nugetExePath.empty && !nugetExePath.startsWith("http")) {
            localNuget = new File(nugetExePath)

            if (localNuget.exists()) {
                return localNuget
            }

            throw new IllegalStateException("Unable to find nuget by path $nugetExePath (please check property 'nugetExePath')")
        }

        def folder = getNugetHome()
        localNuget = new File(folder, NUGET_EXE)

        if (!localNuget.exists()) {
            if (!folder.isDirectory())
                folder.mkdirs()

            def nugetUrl = getNugetDownloadLink()

            project.logger.info "Downloading NuGet from $nugetUrl ..."

            new URL(nugetUrl).withInputStream {
                inputStream ->
                    localNuget.withOutputStream { outputStream ->
                        outputStream << inputStream
                    }
            }
        }
        localNuget
    }

    private String getNugetDownloadLink() {
        if (nugetExePath != null && !nugetExePath.empty && nugetExePath.startsWith("http")) {
            project.logger.debug("Nuget url path is resolved from property 'nugetExePath'")

            return nugetExePath
        }

        def exeName = project.extensions.nuget.version < '3.4.4' ? 'nuget.exe' : 'NuGet.exe'

        return "https://dist.nuget.org/win-x86-commandline/v${project.extensions.nuget.version}/${exeName}"
    }

    private String getNugetVerbosity() {
        if (logger.debugEnabled) return 'detailed'
        if (logger.infoEnabled) return 'normal'
        return 'quiet'
    }
}
