# Gradle NuGet Plugin

This plugin allows to execute NuGet.exe from a gradle build.
It also supports pack & push commands through built-in tasks nugetPack & nugetPush.

## nugetPack

You can see this plugin being used for real on [il-repack](https://github.com/gluck/il-repack) project.
(together with msbuild one)

Sample usage:

    buildscript {
        repositories {
          mavenCentral()
        }
    
        dependencies {
            classpath "com.ullink.gradle:gradle-nuget-plugin:1.1"
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

# License

All these plugins are licensed under the [Creative Commons — CC0 1.0 Universal](http://creativecommons.org/publicdomain/zero/1.0/) license with no warranty (expressed or implied) for any purpose.