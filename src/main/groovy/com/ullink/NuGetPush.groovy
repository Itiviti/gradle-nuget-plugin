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
        def tasks = project.tasks.withType(NuGetPack.class)
        if (tasks.size() == 1)
           tasks.first().packageFile
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
