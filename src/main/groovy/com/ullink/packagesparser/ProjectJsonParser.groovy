package com.ullink.packagesparser

import groovy.json.JsonSlurper

class ProjectJsonParser implements NugetParser {
    @Override
    Collection getDependencies(File file) {
        def dependencies = []
        def projectJson = new JsonSlurper().parse(file)
        def projectJsonFile = new File(file.parentFile, "project.lock.json")
        def projectLockJson = projectJsonFile.exists() ? new JsonSlurper().parse(projectJsonFile) : null
        projectJson.dependencies.each { packageElement ->
            if (!isBuildDependency(packageElement))
                dependencies.add({ dependency(id: packageElement.key, version: getResolvedVersion(packageElement, projectLockJson)) })
        }
        return dependencies;
    }

    boolean isBuildDependency(Map.Entry entry) {
        return entry.value instanceof Map && entry.value.type == "build"
    }

    String getVersion(Map.Entry entry) {
        return entry.value instanceof Map ? entry.value.version : entry.value
    }

    Boolean isBracket(String character) {
        return character == '[' || character == '('|| character == ']' || character == ')'
    }

    String getResolvedVersion(Map.Entry entry, Object projectLockJson) {
        def versionString = getVersion(entry)
        def starIndex = versionString.indexOf('*')
        // In case we don't have floating version, we return the version
        // extracted from the json
        if (starIndex == -1)
            return versionString
        // we need to take the currently resolved version from the lock file
        // because the current project is built against this one so it needs this version
        def dependencyFound = projectLockJson.libraries.find { it.key.startsWith(entry.key + '/') }
        def versionResolved = dependencyFound.key.split('/')[1]
        def commaIndex = versionString.indexOf(',')
        // No range version so we return the build version
        if (commaIndex == -1)
        {
            if (isBracket(versionString[0]))
                return "${versionString[0]}${versionResolved}${versionString[-1]}"
            return versionResolved
        }
        def versionSplit = versionString.split(',')
        if (commaIndex > starIndex)
        {
            return "${versionString[0]}${versionResolved},${versionSplit[1]}"
        }
        return "${versionSplit[0]},${versionResolved}${versionString[-1]}"
    }
}
