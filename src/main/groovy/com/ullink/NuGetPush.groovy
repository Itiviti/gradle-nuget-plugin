package com.ullink


import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional

class NuGetPush extends BaseNuGet {

    @Optional
    @InputFile
    File nupkgFile
    @Optional
    @Input
    def serverUrl
    @Optional
    @Input
    def apiKey
    @Optional
    @InputFile
    def configFile

    NuGetPush() {
        super('push')

        // Force always execute
        outputs.upToDateWhen { false }
    }

    void setNupkgFile(String path) {
        nupkgFile = project.file(path)
    }

    void setApiKey(String key) {
        apiKey = key
    }

    void setConfigFile(String path) {
        configFile = project.file(path)
    }

    @Optional
    @InputFile
    File getNugetPackOutputFile() {
        if (dependentNuGetPack)
           dependentNuGetPack.packageFile
    }

    @Internal
    NuGetPack getDependentNuGetPack() {
        dependsOn.find { it instanceof NuGetPack } as NuGetPack
    }

    @Override
    void exec() {
        args nupkgFile ?: nugetPackOutputFile

        if (serverUrl) args '-Source', serverUrl
        if (apiKey) args '-ApiKey', apiKey
        if (configFile) args '-ConfigFile', configFile

        super.exec()
    }
}
