package com.ullink

import com.ullink.util.GradleHelper
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory

class NuGetRestore extends BaseNuGet {

    @Optional
    @InputFile
    File solutionFile
    @Optional
    @InputFile
    File packagesConfigFile

    @Input
    def sources = [] as Set
    @Input
    def noCache = false
    @Optional
    @InputFile
    File configFile
    @Input
    def requireConsent = false
    @Optional
    @InputDirectory
    File solutionDirectory
    @Input
    def disableParallelProcessing = false
    @Optional
    @Input
    def msBuildVersion
    @Optional
    @Input
    def packagesDirectory

    NuGetRestore() {
        super('restore')
    }

    void setSolutionFile(String path) {
        solutionFile = project.file(path)
    }

    void setPackagesConfigFile(String path) {
        packagesConfigFile = project.file(path)
    }

    void setConfigFile(String path) {
        configFile = project.file(path)
    }

    void setSolutionDirectory(String path) {
        solutionDirectory = project.file(path)
    }

    /**
     * @Deprecated Only provided for backward compatibility. Uses 'sources' instead
     */
    @Deprecated
    def setSource(String source) {
        sources.clear()
        sources.add(source)
    }

    @Override
    void exec() {
        if (packagesConfigFile) args packagesConfigFile
        if (solutionFile) args solutionFile

        if (!sources.isEmpty()) args '-Source', sources.join(';')
        if (noCache) args '-NoCache'
        if (configFile) args '-ConfigFile', configFile
        if (requireConsent) args '-RequireConsent'
        if (packagesDirectory) args '-PackagesDirectory', packagesDirectory
        if (solutionDirectory) args '-SolutionDirectory', solutionDirectory
        if (disableParallelProcessing) args '-DisableParallelProcessing'
        if (!msBuildVersion) msBuildVersion = GradleHelper.getPropertyFromTask(project, 'version', 'msbuild')
        if (msBuildVersion) args '-MsBuildVersion', msBuildVersion

        project.logger.info "Restoring NuGet packages " +
            (sources ? "from $sources" : '') +
            (packagesConfigFile ? "for packages.config ($packagesConfigFile)": '') +
            (solutionFile ? "for solution file ($solutionFile)" : '')
        super.exec()
    }

    @OutputDirectory
    File getPackagesFolder() {
        // https://docs.nuget.org/consume/command-line-reference#restore-command
        // If -PackagesDirectory <packagesDirectory> is specified, <packagesDirectory> is used as the packages directory.
        if (packagesDirectory) {
            return packagesDirectory
        }

        // If -SolutionDirectory <solutionDirectory> is specified, <solutionDirectory>\packages is used as the packages directory.
        // SolutionFile can also be provided.
        // Otherwise use '.\packages'
        def solutionDir = solutionFile ? project.file(solutionFile.getParent()) : solutionDirectory
        return new File(solutionDir ? solutionDir.toString() : '.', 'packages')
    }
}
