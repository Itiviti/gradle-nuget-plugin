package com.ullink

class NuGetSources extends BaseNuGet {
    enum Operation{
        add, remove, enable, disable
    }

    NuGetSources() {
        super('sources')
    }

    Operation operation
    def name
    def source
    def username
    def password
    def configFile
    bool storePaswordInClearText

    @Override
    void exec() {
        args operation
        if (name) args '-Name', name
        if (source) args '-Source',source        
        if (username) args '-UserName', name
        if (password) args '-Passsword', name
        if (configFile) args '-ConfigFile', name
        if (storePaswordInClearText) args '-StorePaswordInClearText'
        super.exec()
    }

}
