package com.ullink

import groovy.util.slurpersupport.GPathResult
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
            getNuspecFile().write(nuspecXml, 'utf-8')
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
        def final packageConfigFileName = 'packages.config'

        GPathResult root = new XmlSlurper(false, false).parseText(nuspecString)

        def defaultMetadata = []
        def setDefaultMetadata = { String node, value ->
            if (root.metadata[node].isEmpty()) {
                defaultMetadata.add( { delegate."$node" value } )
            }
        }

        setDefaultMetadata ('id', project.name)
        setDefaultMetadata ('version', project.version)
        setDefaultMetadata ('description', project.description ? project.description : project.name)

        def appendAndCreateParentIfNeeded = {
            String parentNodeName, List children ->
                if(!children.isEmpty()) {
                    if (root."$parentNodeName".isEmpty()) {
                        root << { "$parentNodeName" children }
                    } else {
                        root."$parentNodeName" << children
                    }
                }
        }

        if (msbuildTaskExists) {
            project.logger.debug("Msbuild plugin detected. Will add defaults from it.")
            def mainProject = project.msbuild.mainProject

            if(root.files.file.isEmpty()) {
                project.logger.debug("No files already defined in the NuGet spec, will add the ones from the msbuild task.")
                def defaultFiles = []
                project.msbuild.mainProject.dotnetArtifacts.each {
                    artifact ->
                        def fwkFolderVersion = mainProject.properties.TargetFrameworkVersion.toString().replace('v', '').replace('.', '')
                        defaultFiles.add({ file(src: artifact.toString(), target: 'lib/net' + fwkFolderVersion) })
                }
                appendAndCreateParentIfNeeded('files', defaultFiles)
            }

            def packageConfigFile = new File(
                    new File(mainProject.projectFile).parentFile,
                    packageConfigFileName)
            if (packageConfigFile.exists()) {
                project.logger.debug("Adding dependencies from packages.config")
                def defaultDependencies = []
                def packages = new XmlParser().parse(packageConfigFile)
                packages.package
                        .findAll { !it.@developmentDependency.toString().toBoolean() }
                        .each {
                    packageElement ->
                        defaultDependencies.add({
                            dependency(id: packageElement.@id, version: packageElement.@version)
                        })
                }
                setDefaultMetadata('dependencies', defaultDependencies)
            }
        }

        appendAndCreateParentIfNeeded('metadata', defaultMetadata)

        project.logger.info("Generated NuGetSpec file with ${root.files.file.size()} files " +
                "and ${root.dependencies.dependecy.size()} dependencies")
        XmlUtil.serialize (root)
    }
}
