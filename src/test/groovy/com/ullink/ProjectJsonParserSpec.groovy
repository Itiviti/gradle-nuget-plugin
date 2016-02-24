package com.ullink

import com.ullink.packagesparser.ProjectJsonParser
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import spock.lang.Specification


class ProjectJsonParserSpec extends Specification {
    def "getResolvedVersion returns expected version"() {
        expect:
        new ProjectJsonParser().getResolvedVersion(entry, projectLockJson) == result

        where:
        entry                              | projectLockJson                   || result
        new MapEntry("foo", "1.2.3")       | null                              || "1.2.3"
        new MapEntry("foo", "1.2.3.4")     | null                              || "1.2.3.4"
        new MapEntry("foo", "1.2.*")       | createDependencies("foo/1.2.3")   || "1.2.3"
        new MapEntry("foo", "1.2.3.*")     | createDependencies("foo/1.2.3.4") || "1.2.3.4"
        new MapEntry("foo", "[1.2.3.4]")   | null                              || "[1.2.3.4]"
        new MapEntry("foo", "(,1.0]")      | null                              || "(,1.0]"
        new MapEntry("foo", "(1.*,]")      | createDependencies("foo/1.2")     || "(1.2,]"
        new MapEntry("foo", "(,1.*]")      | createDependencies("foo/1.2")     || "(,1.2]"
        new MapEntry("foo", "[1.*,2.0)")   | createDependencies("foo/1.5")     || "[1.5,2.0)"
        new MapEntry("foo", "[1.0,2.*)")   | createDependencies("foo/2.5")     || "[1.0,2.5)"
    }

    def createDependencies(String... params) {
        def versions = params.collectEntries { entry -> [entry, [:]] }
        def builder = new JsonBuilder(["libraries": versions])

        return new JsonSlurper().parseText(builder.toString())
    }
}