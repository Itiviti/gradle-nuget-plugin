package com.ullink

import groovy.util.XmlSlurper
import groovy.util.slurpersupport.GPathResult
import groovy.xml.XmlUtil
import groovy.xml.MarkupBuilder
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.Exec

class NuGetSpec extends Exec {

    def nuspecFile
    def nuspec

    void exec() {
        generateNuspecFile()
    }

    File getTempNuspecFile() {
        new File(temporaryDir, project.name + '.nuspec')
    }

    File getNuspecFile() {
        nuspecFile ?: getTempNuspecFile()
    }

    void generateNuspecFile() {
        def nuspecXml = generateNuspec()
        if (nuspecXml) {
            def file = getNuspecFile()
            file.write nuspecXml
        }
    }

    String generateNuspec() {
        if (nuspec) {
            def sw = new StringWriter()
            new groovy.xml.MarkupBuilder(sw).with {
                def visitor
                visitor = { entry ->
                    switch (entry) {
                        case { entry instanceof Closure }:
                            entry.resolveStrategy = DELEGATE_FIRST
                            entry.delegate = delegate
                            entry.call()
                            break
                        case { entry instanceof Map.Entry }:
                            "$entry.key" { visitor entry.value }
                            break
                        case { entry instanceof Map || entry instanceof Collection }:
                            entry.collect(visitor)
                            break
                        default:
                            mkp.yield(entry)
                            break
                    }
                }
                'package' (xmlns: 'http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd') {
                    visitor nuspec
                }
            }
            supplementDefaultValueOnNuspec sw.toString()
        }
    }

    String supplementDefaultValueOnNuspec(String nuspecString) {

        def root = new XmlSlurper(false, false).parseText(nuspecString)

        def defaultValues = {}
        def applyDefaultValue = { String node, String value ->
            if (root.metadata[node].isEmpty()) {
                defaultValues <<= { delegate."$node" value }
            }
        }
        applyDefaultValue ('id', project.name)
        applyDefaultValue ('version', project.version)
        applyDefaultValue ('description', project.description)

        if (root.metadata.isEmpty()) {
            root.appendNode { metadata defaultValues }
        } else {
            root.metadata.appendNode defaultValues
        }
        XmlUtil.serialize (root)
    }
}