# Gradle NuGet Plugin [![Build status](https://ci.appveyor.com/api/projects/status/ua9pbginenbf1b1u/branch/master?svg=true)](https://ci.appveyor.com/project/gluck/gradle-nuget-plugin/branch/master) [![Build Status](https://travis-ci.org/Ullink/gradle-nuget-plugin.svg?branch=master)](https://travis-ci.org/Ullink/gradle-nuget-plugin)

This plugin allows to execute NuGet.exe from a gradle build.
It also supports pack & push commands through built-in tasks, nugetPack, nugetPush & nugetRestore.

## nugetPack

You can see this plugin being used for real on [il-repack](https://github.com/gluck/il-repack) project.
(together with msbuild one)

## nugetSpec

The task is to generate nuspec file by custom configuration.

Sample usage:

```groovy
buildscript {
    repositories {
      mavenCentral()
    }

    dependencies {
        classpath "com.ullink.gradle:gradle-nuget-plugin:2.16"
    }
}

apply plugin: 'nuget'

nuget {
    // nuget.exe version to use, defaults to 4.4.0
    // available versions can be found [here](https://dist.nuget.org/index.html)
    version = '4.4.0'

    // Optionally you can set nuget location, which will be used for download:
    nugetExePath = "https://dist.nuget.org/win-x86-commandline/latest/nuget.exe"
    // Or nuget file, which is already downloaded previously:
    nugetExePath = "C:\\Tools\\Nuget\\nuget.exe"
}

nugetSpec {
    // Array, Map and Closure could be used to generate nuspec XML, for details please check NuGetSpecTest 
    nuspec = [
        metadata: [
            title:          'project title',
            authors:        'Francois Valdy',
            // id:          default is project.name
            // version:     default is project.version
            // description: default is project.description
            // ...
        ]
        files: [
            { file (src: 'somefile1', target: 'tools') },
            { file (src: 'somefile2', target: 'tools') }
        ]
    ]
}
```

## nugetRestore

Nuget restore is used to retrieve missing packages before starting the build.

Sample usage:

```groovy
nugetRestore {
    solutionDirectory = path\to\project
    packagesDirectory = location\for\package\restore
}
```

Where
 - solutionDirectory - could either contain the .sln file or the repositories.config file
 - packagesDirectory - used only if a folder with repositories.config is used

## nugetSources

Nuget sources is used to add, remove, update, enable, disable nuget feeds.

Sample usage:

```groovy
nugetSources {
    operation = 'add'
    sourceName = 'localNuGetFeed'
    sourceUrl = 'http://foo.com'
    username = 'optional username'
    password = 'optional password'
    configFile = 'nuget.config'
}
```

Where
 - operation - could be add, remove, update, enable, disable and list
 - sourceName - name of the nuget feed
 - sourceUrl - url of the nuget feed
 - username - optional username for nuget sources that require http basic authentication
 - password - optional password for nuget sources that require http basic authentication
 - configFile - optional NuGet.config file to modify
 
  
# See also

[Gradle Msbuild plugin](https://github.com/Ullink/gradle-msbuild-plugin) - Allows to build VS projects & solutions.

[Gradle NUnit plugin](https://github.com/Ullink/gradle-nunit-plugin) - Allows to execute NUnit tests from CI (used with this plugin to build the projects prior to UT execution)

[Gradle OpenCover plugin](https://github.com/Ullink/gradle-opencover-plugin) - Allows to execute the UTs through OpenCover for coverage reports.

You can see these 4 plugins in use on [ILRepack](https://github.com/gluck/il-repack) project ([build.gradle](https://github.com/gluck/il-repack/blob/master/build.gradle)).

# License

All these plugins are licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) with no warranty (expressed or implied) for any purpose.
