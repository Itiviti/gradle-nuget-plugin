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
