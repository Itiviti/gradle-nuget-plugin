package com.ullink

import org.gradle.api.tasks.Input


class NugetLocalsClear  extends BaseNuGet{
    @Input
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
