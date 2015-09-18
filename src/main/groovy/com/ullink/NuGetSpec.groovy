package com.ullink

import groovy.xml.XmlUtil
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
                        case Closure:
                            entry.resolveStrategy = DELEGATE_FIRST
                            entry.delegate = delegate
                            entry.call()
                            break
                        case Map.Entry:
                            "$entry.key" { visitor entry.value }
                            break
                        case Map:
                        case Collection:
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
        def final msbuildTaskExists = project.tasks.findByName('msbuild') != null

        def root = new XmlSlurper(false, false).parseText(nuspecString)

        def defaultValues = {}
        def setDefaultMetadata = { String node, String value ->
            if (root.metadata[node].isEmpty()) {
                defaultValues <<= { delegate."$node" value }
            }
        }

        setDefaultMetadata ('id', project.name)
        setDefaultMetadata ('title', project.name)
        setDefaultMetadata ('version', project.version)
        setDefaultMetadata ('description', project.description ? project.description : project.name)

        def appendAndCreateParentIfNeeded = {
            parentNodeName, children ->
                if (root."$parentNodeName".isEmpty()) {
                    root.appendNode { "$parentNodeName" children }
                } else {
                    root."$parentNodeName".appendNode children
                }
        }

        appendAndCreateParentIfNeeded('metadata', defaultValues)

        if (msbuildTaskExists) {
            def defaultFiles = {}
            project.msbuild.mainProject.dotnetArtifacts.each {
                artifact ->
                    def fwkFolderVersion = project.msbuild.mainProject.properties.TargetFrameworkVersion.toString().replace('v', '').replace('.', '')
                    defaultFiles <<= { file(src: artifact.toString(), target: 'lib/net' + fwkFolderVersion) }
            }

            appendAndCreateParentIfNeeded('files', defaultFiles)
        }

        XmlUtil.serialize (root)
    }
}