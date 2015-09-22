Plugin changelog
====================

2.10
-------

* title default value is the project name
* description is the project name if no description is provided
* files are taken from artifacts of msbuild plugin and the target
folder from target framework in csproj

2.9
-------

* nugetPush input package now defaults to nugetPack generated one (if any)
* nuspec generation now defaults id, version and description in nuspec generation
* extract task nugetSpec from nugetPack to generate nuspec
* support Map for defining nuspec in nugetSpec
