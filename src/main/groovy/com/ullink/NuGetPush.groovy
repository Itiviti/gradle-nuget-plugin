package com.ullink

class NuGetPush extends BaseNuGet {

    def nupkgFile

    def serverUrl
    def apiKey
    def timeout
    def configFile

    NuGetPush() {
        super('push')
    }

    String getNugetPackOutputFile() {
        if (dependentNuGetPack)
           dependentNuGetPack.packageFile
    }

    NuGetPack getDependentNuGetPack() {
        dependsOn.find { it instanceof NuGetPack }
    }

    @Override
    void exec() {
        args nupkgFile ?: nugetPackOutputFile

        if (serverUrl) args '-Source', serverUrl
        if (apiKey) args '-ApiKey', apiKey
        if (timeout) args '-Timeout', timeout
        if (configFile) args '-ConfigFile', configFile

        super.exec()
    }
}
