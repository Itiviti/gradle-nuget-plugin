package com.ullink.packagesparser

class PackagesConfigParser implements NugetParser {
    @Override
    Collection getDependencies(File file) {
        def defaultDependencies = []
        def packages = new XmlParser().parse(file)
        packages.package
                .findAll { !it.@developmentDependency.toString().toBoolean() }
                .each {
            packageElement ->
                defaultDependencies.add({
                    dependency(id: packageElement.@id, version: getVersion(packageElement))
                })
        }
        return defaultDependencies;
    }

    String getVersion(Object element) {
        if (element.@allowedVersions)
            return element.@allowedVersions
        return  element.@version
    }
}
