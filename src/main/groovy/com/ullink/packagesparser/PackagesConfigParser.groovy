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
                    dependency(id: packageElement.@id, version: packageElement.@version)
                })
        }
        return defaultDependencies;
    }
}
