# gradle-nuget-plugin changelog

## 2.21
### Fixed
* NugetPush should only consider NugetPack's output if it is enabled

## 2.20
### Fixed
* Add Input and Output Annotations for Gradle 7 [#77](https://github.com/Itiviti/gradle-nuget-plugin/issues/77)
* Incorrect Input annotations applied

## 2.19
### Changed
* Built with gradle 6.2.2
* Default NuGet version used is 5.5.0

### Removed
* Due to conflict in Gradle 6.1, `timeout` in `nugetPush` is removed [#78](https://github.com/Itiviti/gradle-nuget-plugin/issues/78)
* Usage of deprecated base.getDistsDir [#76](https://github.com/Itiviti/gradle-nuget-plugin/issues/76)

## 2.18
### Changed
* built with gradle 4.7 (above won't work till we migrate out of plugindev)

### Fixed
* Project configuration fails without nugetSources section in build.gradle ([#66](https://github.com/Ullink/gradle-nuget-plugin/pull/66))

## 2.17
### Added
* Ability to download nuget executable from the custom location (this can be used to download nuget.exe from local maven server)
* Ability to use previously downloaded nuget from custom location (this is useful when CI already downloaded nuget.exe)
* Upgrade gradle used to the newest version
* Ability to generate dependencies from PackageReferences in project file

## 2.16
### Added
* Added nuget sources task ([#58](https://github.com/Ullink/gradle-nuget-plugin/pull/58))
* Provide `-MsBuildVersion` param to nuget pack/restore ([#56](https://github.com/Ullink/gradle-nuget-plugin/issues/56)/[#57](https://github.com/Ullink/gradle-nuget-plugin/pull/57))

### Changed
* Default NuGet version used is 4.4.0 ([#59](https://github.com/Ullink/gradle-nuget-plugin/pull/59))

## 2.15
### Fixed
* In `NugetSpec`, `mainProject` is not accessed anymore when if the linked msbuild task has `parseProject=false`.
What this means is that no defaults will be added from the msbuild task.
* The `NuGetPack` will provide a consistent parsed instance (namespace-unaware).

## 2.14
### Changed
* Default NuGet version used is 3.4.3 (Fixes no_proxy behavior).

## 2.13
### Changed
* Default NuGet version used is 3.3.0.
* NuGetRestore task now has a 'sources' parameter just like the NugetInstall task. It enables multiple sources to be set.

### Fixed
* Fix copyright system © in nuget spec.
* Fix getPackageFile() for NuGetPack task when used together with a csproj instead of a nuspec.

## 2.12
### Added
* Add the 'nuget-base' plugin, so we can use common NuGet configurations without having to have the default tasks created

## 2.11
* handled packages with developmentDependency set to true and ignore them as a dependencies
* title is not provided anymore ('nuget pack' take the id value if the title is not provided)
* The default nuspec files are provided only if it's not provided by nuspec task settings

## 2.10
* title default value is the project name
* description is the project name if no description is provided
* files are taken from artifacts of msbuild plugin and the target
folder from target framework in csproj
* take dependencies from packages.config
* nuget version is now configurable, and default has been bumped to 3.2.0

## 2.9
* nugetPush input package now defaults to nugetPack generated one (if any)
* nuspec generation now defaults id, version and description in nuspec generation
* extract task nugetSpec from nugetPack to generate nuspec
* support Map for defining nuspec in nugetSpec
