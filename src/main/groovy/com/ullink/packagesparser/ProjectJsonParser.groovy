package com.ullink.packagesparser

import groovy.json.JsonSlurper

class ProjectJsonParser implements NugetParser {
    @Override
    Collection getDependencies(File file) {
        def dependencies = []
        def projectJson = new JsonSlurper().parse(file)
        projectJson.dependencies.each { packageElement ->
            if (!isBuildDependency(packageElement))
                dependencies.add({ dependency(id: packageElement.key, version: getVersion(packageElement)) })
        }
        return dependencies;
    }

    boolean isBuildDependency(Map.Entry entry) {
        return entry.value instanceof Map && entry.value.type == "build"
    }

    String getVersion(Map.Entry entry) {
        return entry.value instanceof Map ? entry.value.version : entry.value
    }
}
