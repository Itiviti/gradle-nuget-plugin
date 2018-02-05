package com.ullink


class NugetLocalsClear  extends BaseNuGet{
    def all = false

    NugetLocalsClear() {
        super('locals')
    }

    @Override
    void exec() {
        args '-clear'
        if(all) args 'all'
        super.exec()
    }

}
