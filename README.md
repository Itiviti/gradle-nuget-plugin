# Gradle NuGet Plugin

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
            classpath "com.ullink.gradle:gradle-nuget-plugin:1.3"
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

# License

All these plugins are licensed under the [Creative Commons — CC0 1.0 Universal](http://creativecommons.org/publicdomain/zero/1.0/) license with no warranty (expressed or implied) for any purpose.