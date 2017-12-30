package com.ullink

class NuGetSources extends BaseNuGet {
    enum Operation{
        add, remove, enable, disable
    }
    enum Verbosity{
        normal, quiet, detailed
    }
    NuGetSources() {
        super('sources')
    }

    Operation operation
    Verbosity verbosity
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
        if (verbosity) args '-Verbosity', verbosity
        if (storePaswordInClearText) args '-StorePaswordInClearText'
        super.exec()
    }

}
