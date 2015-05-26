# Gradle NuGet Plugin [![Build status](https://ci.appveyor.com/api/projects/status/ua9pbginenbf1b1u/branch/master?svg=true)](https://ci.appveyor.com/project/gluck/gradle-nuget-plugin/branch/master) [![Build Status](https://travis-ci.org/Ullink/gradle-nuget-plugin.svg?branch=master)](https://travis-ci.org/Ullink/gradle-nuget-plugin)

This plugin allows to execute NuGet.exe from a gradle build.
It also supports pack & push commands through built-in tasks, nugetPack, nugetPush & nugetRestore.

## nugetPack

You can see this plugin being used for real on [il-repack](https://github.com/gluck/il-repack) project.
(together with msbuild one)

Sample usage:

    buildscript {
        repositories {
          mavenCentral()
        }
    
        dependencies {
            classpath "com.ullink.gradle:gradle-nuget-plugin:2.5"
        }
    }
    
    apply plugin:'nuget'

    nuget {
    	// this Closure will be applied to the nuspec XMLBuilder
		nuspec {
			metadata() {
				id archivesBaseName
				delegate.version version
				title 'project title'
				authors 'Francois Valdy'
				delegate.description '''some looong description...'''
				// ...
			}
			delegate.files() {
				delegate.file(src: 'somefile', target: 'tools')
			}
		}
    }
	
## nugetRestore

    Nuget restore is used to retrieve missing packages before starting the build

    - Sample usage:

    nugetRestore {
        projectFolder = path\to\project
		restoreFolder = location\for\package\restore
    }

    Where
     - projectFolder - could either contain the .sln file or the repositories.config file
     - restoreFolder - used only if a folder with repositories.config is used

# See also

[Gradle Msbuild plugin](https://github.com/Ullink/gradle-msbuild-plugin) - Allows to build VS projects & solutions.

[Gradle NUnit plugin](https://github.com/Ullink/gradle-nunit-plugin) - Allows to execute NUnit tests from CI (used with this plugin to build the projects prior to UT execution)

[Gradle OpenCover plugin](https://github.com/Ullink/gradle-opencover-plugin) - Allows to execute the UTs through OpenCover for coverage reports.

You can see these 4 plugins in use on [ILRepack](https://github.com/gluck/il-repack) project ([build.gradle](https://github.com/gluck/il-repack/blob/master/build.gradle)).

# License

All these plugins are licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) with no warranty (expressed or implied) for any purpose.
