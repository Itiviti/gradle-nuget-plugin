package com.ullink

import org.gradle.api.tasks.Delete

class NugetClearLocalPackagesFolder extends Delete {

    NugetClearLocalPackagesFolder() {
        def packageFolder = project.file('packages')
        setDelete(packageFolder)
        outputs.upToDateWhen { !packageFolder.exists() }
    }
}
