package com.ullink.packagesparser

import com.ullink.BaseNuGet

class PackagesConfigParser implements NugetParser {
    @Override
    Collection getDependencies(File file) {
        def defaultDependencies = []
        def packages = BaseNuGet.createXmlSlurper(false, true).parse(file)
        packages.package
                .findAll { !it.@developmentDependency.toString().toBoolean() }
                .each {
            packageElement ->
                defaultDependencies.add({
                    dependency(id: packageElement.@id, version: getVersion(packageElement))
                })
        }
        return defaultDependencies
    }

    String getVersion(Object element) {
        if (element.@allowedVersions && !element.@allowedVersions.isEmpty())
            return element.@allowedVersions
        return  element.@version
    }
}
