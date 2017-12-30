package com.ullink

class NuGetSources extends BaseNuGet {
    
    enum Operation{
        add, remove, enable, disable
    }

    NuGetSources() {
        super('sources')
        project.afterEvaluate{
            if(!operation){
                throw new GradleException('Operation not specified for NuGetSources task.')
            }
            if(!sourceName){
                throw new GradleException('SourceName not specified for NuGetSources task.')
            }
        }
    }

    Operation operation    
    def sourceName
    def sourceUrl
    def username
    def password
    def configFile
    def storePaswordInClearText = false

    @Override
    void exec() {
        args operation
        if (sourceName) args '-Name', sourceName
        if (sourceUrl) args '-Source',sourceUrl        
        if (username) args '-UserName', name
        if (password) args '-Passsword', name
        if (configFile) args '-ConfigFile', name
        if (storePaswordInClearText) args '-StorePaswordInClearText'
        super.exec()
    }

}
