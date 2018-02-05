package com.ullink


class NugetClear  extends BaseNuGet{
    def all = false

    NugetClear() {
        super('locals -clear')
    }
    
    @Override
    void exec() {
        if(all) args 'all'
        super.exec()
    }

}
