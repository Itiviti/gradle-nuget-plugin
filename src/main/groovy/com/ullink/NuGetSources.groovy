package com.ullink

import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional

class NuGetSources extends BaseNuGet {
    
    enum Operation{
        add, remove, enable, disable, list, update
    }

    @Input
    Operation operation
    @Optional
    @Input
    def sourceName
    @Optional
    @Input
    def sourceUrl
    @Optional
    @Input
    def username
    @Optional
    @Input
    def password
    @Optional
    @InputFile
    File configFile
    @Input
    def storePasswordInClearText = false

    NuGetSources() {
        super('sources')
    }

    void setConfigFile(String path) {
        configFile = project.file(path)
    }

    @Override
    void exec() {
        if(!operation){
            throw new GradleException('Operation not specified for NuGetSources task.')
        }
        if(operation != Operation.list && !sourceName){
            throw new GradleException('SourceName not specified for NuGetSources task.')
        }

        args operation
        if (sourceName) args '-Name', sourceName
        if (sourceUrl) args '-Source', sourceUrl        
        if (username) args '-UserName', username
        if (password) args '-Password', password
        if (configFile) args '-ConfigFile', configFile
        if (storePasswordInClearText) args '-StorePasswordInClearText'
        super.exec()
    }
}
