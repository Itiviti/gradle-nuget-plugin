package com.ullink.packagesparser

import com.ullink.BaseNuGet

class PackageReferenceParser implements NugetParser {

    // Limitation: does not resolve the conditional package references into groups

    @Override
    Collection getDependencies(File file) {
        def defaultDependencies = []

        def project = BaseNuGet.createXmlSlurper(false, true).parse(file)

        project.'**'
            .findAll { it.name() == 'PackageReference' && getAttributeOrNodeText(it, 'PrivateAssets') != 'all' }
            .each {
                def reference = it
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

    private static String getAttributeOrNodeText(def parentNode, String key) {
        def node = parentNode."${key}".toString().trim()
        if (node) {
            return node
        }
        return parentNode["@${key}"]
    }

}
