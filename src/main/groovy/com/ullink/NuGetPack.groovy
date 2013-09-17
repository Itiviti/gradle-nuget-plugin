package com.ullink

import groovy.util.slurpersupport.GPathResult;

import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.StopActionException
import org.gradle.api.tasks.TaskAction
import org.gradle.api.GradleException

class NuGetPack extends BaseNuGet {
	def nuspecFile
	def destinationDir
	def generateSymbols
	def csprojPath
	Closure nuspec

    NuGetPack() {
		super('pack')
		conventionMapping.map('destinationDir', { project.convention.plugins.base.distsDir } )

		// TODO inputs/outputs
    }

	@Override
    List<String> extraCommands() {
		def commandLineArgs = []
		commandLineArgs += getNuspecOrCsproj()

		def destDir = project.file(getDestinationDir())
		if (!destDir.exists()) {
			destDir.mkdirs()
		}
		commandLineArgs += '-OutputDirectory'
		commandLineArgs += destDir

		def spec = getNuspec()
		def version = spec.metadata.version ?: project.version

		if (version) {
			commandLineArgs += '-Version'
			commandLineArgs += version
		}

		if (generateSymbols) {
			commandLineArgs += '-Symbols'
		}
		commandLineArgs
    }

	@Override
	void verifyCommand() {
		if (!getPackageFile().isFile()) {
			throw new GradleException('NuGet package creation failed, check its output')
		}
	}

	void nuspec(Closure closure) {
		nuspec = closure
	}

	Closure getNuspecCustom() {
		nuspec
	}

	GPathResult getNuspec() {
		new XmlSlurper().parse(getNuSpecFile())
	}

	// Because Nuget pack handle csproj or nuspec file we should be able to use it in plugin
	File getNuspecOrCsproj() {
		if (csprojPath) {
			return project.file(csprojPath)
		}
		getNuSpecFile()
	}

	File getNuSpecFile() {
		if (!this.nuspecFile) {
			this.nuspecFile = generateNuspecFile()
		}
		project.file(this.nuspecFile)
	}

	File getPackageFile() {
		def spec = getNuspec()
		def version = spec.metadata.version ?: project.version
		new File(getDestinationDir(), spec.metadata.id.toString() + '.' + version + '.nupkg')
	}

	File generateNuspecFile() {
		File nuspecFile = new File(temporaryDir, project.name + '.nuspec')
		nuspecFile.withWriter("UTF-8") { writer ->
			def builder = new groovy.xml.MarkupBuilder(writer)
			builder.mkp.xmlDeclaration(version:'1.0')
			builder.'package'(xmlns: 'http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd') {
				if (nuspecCustom) {
					nuspecCustom.resolveStrategy = Closure.DELEGATE_FIRST
					nuspecCustom.delegate = delegate
					nuspecCustom.call()
				} else {
					// default content ?
					metadata() {
						id project.name
						version project.version
						description project.description
					}
					files() {
						// ...
					}
				}
			}
		}
		nuspecFile
	}
}
