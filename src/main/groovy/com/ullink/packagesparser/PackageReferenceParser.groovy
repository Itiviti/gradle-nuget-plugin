package com.ullink.packagesparser

import groovy.util.slurpersupport.GPathResult


class PackageReferenceParser implements NugetParser {

    // Limitation: does not resolve the conditional package references into groups

    @Override
    Collection getDependencies(File file) {
        def defaultDependencies = []

        def project = new XmlSlurper().parse(file)

        project.'**'
            .findAll { it.name() == 'PackageReference' && getAttributeOrNodeText(it, 'PrivateAssets') != 'all' }
            .each {
                def reference = it as GPathResult
                def includeAssets = getAttributeOrNodeText(reference, 'IncludeAssets')
                def excludeAssets = getAttributeOrNodeText(reference, 'ExcludeAssets')
                def args = [
                    id: getAttributeOrNodeText(reference, 'Include'), version: getAttributeOrNodeText(reference, 'Version')
                ]
                if (includeAssets) {
                    args.include = includeAssets
                }
                if (excludeAssets) {
                    args.exclude = excludeAssets
                }

                defaultDependencies.add({
                    dependency(args)
                })
        }
        return defaultDependencies
    }

    private static String getAttributeOrNodeText(GPathResult parentNode, String key) {
        def node = parentNode."${key}".toString().trim()
        if (node) {
            return node
        }
        return parentNode["@${key}"]
    }

}
