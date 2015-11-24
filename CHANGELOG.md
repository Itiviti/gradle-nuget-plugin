Plugin changelog
====================

2.13
-------
* Fix copyright system © in nuget spec.

2.12
-------
* Add the 'nuget-base' plugin, so we can use common NuGet configurations without having to have the default tasks created

2.11
-------
* handled packages with developmentDependency set to true and ignore them as a dependencies
* title is not provided anymore ('nuget pack' take the id value if the title is not provided)
* The default nuspec files are provided only if it's not provided by nuspec task settings

2.10
-------

* title default value is the project name
* description is the project name if no description is provided
* files are taken from artifacts of msbuild plugin and the target
folder from target framework in csproj
* take dependencies from packages.config
* nuget version is now configurable, and default has been bumped to 3.2.0

2.9
-------

* nugetPush input package now defaults to nugetPack generated one (if any)
* nuspec generation now defaults id, version and description in nuspec generation
* extract task nugetSpec from nugetPack to generate nuspec
* support Map for defining nuspec in nugetSpec
