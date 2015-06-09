package com.ullink

class NuGetPush extends BaseNuGet {

    def nupkgFile

    def serverUrl
    def apiKey
    def timeout
    def configFile

    NuGetPush() {
        super('push')
        project.afterEvaluate {
            if (nupkgFile) {
                def tasks = project.tasks.withType(NuGetPack.class).findAll { it.packageFile == nupkgFile }
                if (tasks.size() == 1)
                    dependsOn tasks[0]
            }
        }
    }

    @Override
    void exec() {
        if (nupkgFile) args nupkgFile

        if (serverUrl) args '-Source', serverUrl
        if (apiKey) args '-ApiKey', apiKey
        if (timeout) args '-Timeout', timeout
        if (configFile) args '-ConfigFile', configFile

        super.exec()
    }
}
