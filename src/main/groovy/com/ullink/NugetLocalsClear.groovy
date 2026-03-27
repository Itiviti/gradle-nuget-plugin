package com.ullink

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class NugetLocalsClear extends BaseNuGet {
    @Input
    def all = false

    NugetLocalsClear() {
        super('locals')
    }

    @TaskAction
    @Override
    void exec() {
        prepare()
        args '-clear'
        if(all) args 'all'
        super.exec()
    }

}
