package com.ullink.packagesparser


class PackageReferenceParser implements NugetParser {
    @Override
    Collection getDependencies(File file) {
        def defaultDependencies = []

        def project = new XmlParser().parse(file)

        project.ItemGroup.each { node ->
            node.PackageReference.findAll { it.PrivateAssets.text().trim() != 'all' }.each { reference ->
                if (reference.IncludeAssets) {
                    defaultDependencies.add({
                        dependency(id: reference.@Include, version: reference.Version.text(), include: reference.IncludeAssets.text())
                    })
                } else {
                    defaultDependencies.add({
                        dependency(id: reference.@Include, version: reference.Version.text())
                    })
                }
                reference.attributes().include
            }
        }
        return defaultDependencies
    }
}
