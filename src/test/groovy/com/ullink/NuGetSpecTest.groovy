package com.ullink
import org.custommonkey.xmlunit.XMLUnit
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual

class NuGetSpecTest {
    @Before
    public void init() {
        XMLUnit.setIgnoreWhitespace(true)
    }

    private Project newNugetProject() {
        Project project = ProjectBuilder.builder().withName('foo').build()
        project.with {
            description = 'fooDescription'
            version = '2.1'
            apply plugin: 'nuget'
        }
        project
    }

    Project newNugetWithMsbuildProject() {
        def project = newNugetProject()
        def msbuildTask = new MSBuildTaskBuilder()
                .withAssemblyName('bar')
                .withFrameworkVersion('v3.5')
                .withArtifact('folder/bin/bar.dll')
                .withProjectFile('folder/does not exist')
                .build()
        project.tasks.add(msbuildTask)
        project
    }

    @Test
    public void generateNuspec_Closure() {
        def project = newNugetProject()

        project.nugetSpec {
            nuspec {
                metadata {
                    id 'foo'
                    delegate.description 'fooDescription'
                    frameworkAssemblies {
                        frameworkAssembly (assemblyName: "System.Web", targetFramework: "net40")
                    }
                }
                files {
                    file ( src: 'bar', target: 'barTarget' )
                    file ( src: 'baz', target: 'bazTarget' )
                }
            }
        }
        def expected =
        '''
        <package xmlns="http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd">
            <metadata>
                <id>foo</id>
                <version>2.1</version>
                <description>fooDescription</description>
                <frameworkAssemblies>
                    <frameworkAssembly assemblyName='System.Web' targetFramework='net40' />
                </frameworkAssemblies>
            </metadata>
            <files>
                <file src='bar' target='barTarget' />
                <file src='baz' target='bazTarget' />
            </files>
        </package>'''

        assertXMLEqual (expected, project.tasks.nugetSpec.generateNuspec())
    }

    @Test
    public void generateNuspec_Map() {
        def project = newNugetProject()

        project.nugetSpec {
            nuspec = [
                metadata: [
                    id: 'foo',
                    description: 'fooDescription',
                    frameworkAssemblies: {
                        frameworkAssembly (assemblyName: "System.Web", targetFramework: "net40")
                    }
                ],
                files: [
                    { file ( src: 'bar', target: 'barTarget' ) },
                    { file ( src: 'baz', target: 'bazTarget' ) }
                ]
            ]
        }
        def expected =
        '''
        <package xmlns="http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd">
            <metadata>
                <id>foo</id>
                <version>2.1</version>
                <description>fooDescription</description>
                <frameworkAssemblies>
                    <frameworkAssembly assemblyName='System.Web' targetFramework='net40' />
                </frameworkAssemblies>
            </metadata>
            <files>
                <file src='bar' target='barTarget' />
                <file src='baz' target='bazTarget' />
            </files>
        </package>'''
        assertXMLEqual (expected, project.tasks.nugetSpec.generateNuspec())
    }

    @Test
    public void generateNuspec_DefaultValue() {
        def project = newNugetProject()

        project.nugetSpec {
            nuspec { }
        }

        def expected =
        '''
        <package xmlns="http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd">
            <metadata>
                <id>foo</id>
                <version>2.1</version>
                <description>fooDescription</description>
            </metadata>
        </package>'''
        assertXMLEqual (expected, project.tasks.nugetSpec.generateNuspec())
    }

    @Test
    public void generateNuspec_OverrideDefaultValue() {
        def project = newNugetProject()

        project.nugetSpec {
            nuspec = [
                metadata: [
                    id: 'bar',
                    description: 'barDescription',
                    version: '4.5',
                ]
            ]
        }

        def expected =
        '''
        <package xmlns="http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd">
            <metadata>
                <id>bar</id>
                <version>4.5</version>
                <description>barDescription</description>
            </metadata>
        </package>'''
        assertXMLEqual (expected, project.tasks.nugetSpec.generateNuspec())
    }

    @Test
    public void generateNuspec_explicitEmptyFilesListClosure() {
        def project = newNugetWithMsbuildProject()

        project.nugetSpec {
            nuspec {
                files {
                }
            }
        }

        def expected =
                '''
        <package xmlns="http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd">
            <metadata>
                <id>foo</id>
                <version>2.1</version>
                <description>fooDescription</description>
            </metadata>
            <files>
                <file src='folder\\bin\\bar.dll' target='lib/net35' />
            </files>
        </package>'''.replace('\\', File.separator)
        assertXMLEqual (expected, project.tasks.nugetSpec.generateNuspec())
    }

    @Test
    public void generateNuspec_explicitEmptyFilesListMap() {
        def project = newNugetWithMsbuildProject()

        project.nugetSpec {
            nuspec {
                files: []
            }
        }

        def expected =
                '''
        <package xmlns="http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd">
            <metadata>
                <id>foo</id>
                <version>2.1</version>
                <description>fooDescription</description>
            </metadata>
            <files>
                <file src='folder\\bin\\bar.dll' target='lib/net35' />
            </files>
        </package>'''.replace('\\', File.separator)
        assertXMLEqual (expected, project.tasks.nugetSpec.generateNuspec())
    }

    @Test
    public void generateNuspec_defaultFilesFromCsproj() {
        def project = newNugetWithMsbuildProject()

        project.nugetSpec {
            nuspec { }
        }

        def expected =
                '''
        <package xmlns="http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd">
            <metadata>
                <id>foo</id>
                <version>2.1</version>
                <description>fooDescription</description>
            </metadata>
            <files>
                <file src='folder\\bin\\bar.dll' target='lib/net35' />
            </files>
        </package>'''.replace('\\', File.separator)
        assertXMLEqual (expected, project.tasks.nugetSpec.generateNuspec())
    }

    @Test
    public void generateNuspec_noDefaultFilesFromCsprojIfParseProjectIsFalse() {
        def project = newNugetWithMsbuildProject()
        project.tasks.msbuild.metaClass.parseProject = false

        project.nugetSpec {
            nuspec { }
        }

        def expected =
                '''
        <package xmlns="http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd">
            <metadata>
                <id>foo</id>
                <version>2.1</version>
                <description>fooDescription</description>
            </metadata>
        </package>'''.replace('\\', File.separator)
        assertXMLEqual (expected, project.tasks.nugetSpec.generateNuspec())
    }

    @Test
    public void generateNuspec_withoutDefaultFilesAsTheyAreAlreadyProvided() {
        def project = newNugetWithMsbuildProject()

        project.nugetSpec {
            nuspec {
                files {
                    file(src: 'anotherLib.dll', target: 'lib/net45')
                }
            }
        }

        def expected =
                '''
        <package xmlns="http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd">
            <metadata>
                <id>foo</id>
                <version>2.1</version>
                <description>fooDescription</description>
            </metadata>
            <files>
                <file src="anotherLib.dll" target="lib/net45" />
            </files>
        </package>'''
        assertXMLEqual (expected, project.tasks.nugetSpec.generateNuspec())
    }

    @Test
    public void generateNuspec_defaultDependenciesFromPackageConfig() {
        def project = newNugetProject()

        project.nugetSpec {
            nuspec {}
        }

        File.createTempDir().with { projectFolder ->
            deleteOnExit()

            def msbuildTask = new MSBuildTaskBuilder()
                    .withAssemblyName('bar')
                    .withProjectFile(new File(projectFolder.path, 'bar.csproj'))
                    .build()
            project.tasks.add(msbuildTask)

            File packageConfig = new File(projectFolder, 'packages.config')
            packageConfig.createNewFile()
            packageConfig.write(
                    '''<?xml version="1.0" encoding="utf-8"?>
                        <packages>
                            <package id="depBar" version="0.2.3.4" targetFramework="net35" />
                            <package id="depFoo" version="100.5.6" targetFramework="net35" />
                            <package id="depBar2" version="1.2.3.4" developmentDependency="true" targetFramework="net35" />
                            <package id="depFoo2" version="10.5.7" developmentDependency="false" targetFramework="net35" />
                        </packages>'''
            )

            def expected =
                    '''
                    <package xmlns="http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd">
                        <metadata>
                            <id>foo</id>
                            <version>2.1</version>
                            <description>fooDescription</description>
                            <dependencies>
                                <dependency id="depBar" version="0.2.3.4" />
                                <dependency id="depFoo" version="100.5.6" />
                                <dependency id="depFoo2" version="10.5.7" />
                            </dependencies>
                        </metadata>
                    </package>'''
            assertXMLEqual(expected, project.tasks.nugetSpec.generateNuspec())
        }
    }

    @Test
    public void generateNuspec_defaultDependenciesFromPackageConfig_versionRanged() {
        def project = newNugetProject()

        project.nugetSpec {
            nuspec {}
        }

        File.createTempDir().with { projectFolder ->
            deleteOnExit()

            def msbuildTask = new MSBuildTaskBuilder()
                    .withAssemblyName('bar')
                    .withProjectFile(new File(projectFolder.path, 'bar.csproj'))
                    .build()
            project.tasks.add(msbuildTask)

            File packageConfig = new File(projectFolder, 'packages.config')
            packageConfig.createNewFile()
            packageConfig.write(
                    '''<?xml version="1.0" encoding="utf-8"?>
                        <packages>
                            <package id="depBar" version="0.2.3.4" allowedVersions="(0.2.3.4,)" targetFramework="net35" />
                            <package id="depFoo" version="100.5.6" allowedVersions="[100.5.6]" targetFramework="net35" />
                            <package id="depBar2" version="1.2.3.4" developmentDependency="true" targetFramework="net35" />
                            <package id="depFoo2" version="10.5.7" allowedVersions="[10.5.7, 10.6]" developmentDependency="false" targetFramework="net35" />
                        </packages>'''
            )

            def expected =
                    '''
                    <package xmlns="http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd">
                        <metadata>
                            <id>foo</id>
                            <version>2.1</version>
                            <description>fooDescription</description>
                            <dependencies>
                                <dependency id="depBar" version="(0.2.3.4,)" />
                                <dependency id="depFoo" version="[100.5.6]" />
                                <dependency id="depFoo2" version="[10.5.7, 10.6]" />
                            </dependencies>
                        </metadata>
                    </package>'''
            assertXMLEqual(expected, project.tasks.nugetSpec.generateNuspec())
        }
    }

    @Test
    public void generateNuspec_defaultDependenciesFromProjectJson() {
        def project = newNugetProject()

        project.nugetSpec {
            nuspec {}
        }

        File.createTempDir().with { projectFolder ->
            deleteOnExit()

            def msbuildTask = new MSBuildTaskBuilder()
                    .withAssemblyName('bar')
                    .withProjectFile(new File(projectFolder.path, 'bar.csproj'))
                    .build()
            project.tasks.add(msbuildTask)

            File projectJson = new File(projectFolder, 'project.json')
            projectJson.createNewFile()
            projectJson.write(
                    '''{
                          "dependencies": {
                            "depBar": "0.2.3.4",
                            "depFoo": "100.5.6",
                            "depBar2": {"version": "1.2.3.4", "type":"build"},
                            "depFoo2": {"version": "10.5.7", "type":"default"}
                          },
                          "frameworks": {
                            "net35": {}
                          },
                          "runtimes": {
                            "win": {}
                          }
                        }'''
            )

            def expected =
                    '''
                    <package xmlns="http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd">
                        <metadata>
                            <id>foo</id>
                            <version>2.1</version>
                            <description>fooDescription</description>
                            <dependencies>
                                <dependency id="depBar" version="0.2.3.4" />
                                <dependency id="depFoo" version="100.5.6" />
                                <dependency id="depFoo2" version="10.5.7" />
                            </dependencies>
                        </metadata>
                    </package>'''
            def nuspecGenerated = project.tasks.nugetSpec.generateNuspec()
            assertXMLEqual(expected, nuspecGenerated)
        }
    }

    @Test
    public void generateNuspec_defaultDependenciesFromProjectJson_versionRanged() {
        def project = newNugetProject()

        project.nugetSpec {
            nuspec {}
        }

        File.createTempDir().with { projectFolder ->
            deleteOnExit()

            def msbuildTask = new MSBuildTaskBuilder()
                    .withAssemblyName('bar')
                    .withProjectFile(new File(projectFolder.path, 'bar.csproj'))
                    .build()
            project.tasks.add(msbuildTask)

            File projectJson = new File(projectFolder, 'project.json')
            projectJson.createNewFile()
            projectJson.write(
                    '''{
                          "dependencies": {
                            "depBar": "[0.2.3.4]",
                            "depFoo": "[100.5.*, 100.6)",
                            "depBar2": {"version": "1.2.3.4", "type":"build"},
                            "depFoo2": {"version": "[10.5.*]", "type":"default"}
                          },
                          "frameworks": {
                            "net35": {}
                          },
                          "runtimes": {
                            "win": {}
                          }
                        }'''
            )
            File projectLockJson = new File(projectFolder, 'project.lock.json')
            projectLockJson.createNewFile()
            projectLockJson.write(
                    '''{
                          "libraries": {
                            "depFoo/100.5.2": {
                            },
                            "depFoo2/10.5.7": {
                            }
                          }
                        }'''
            )

            def expected =
                    '''
                    <package xmlns="http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd">
                        <metadata>
                            <id>foo</id>
                            <version>2.1</version>
                            <description>fooDescription</description>
                            <dependencies>
                                <dependency id="depBar" version="[0.2.3.4]" />
                                <dependency id="depFoo" version="[100.5.2, 100.6)" />
                                <dependency id="depFoo2" version="[10.5.7]" />
                            </dependencies>
                        </metadata>
                    </package>'''
            def nuspecGenerated = project.tasks.nugetSpec.generateNuspec()
            assertXMLEqual(expected, nuspecGenerated)
        }
    }

    @Test
    public void generateNuspec_defaultDependenciesFromProjectJsonAndPackageConfig() {
        def project = newNugetProject()

        project.nugetSpec {
            nuspec {}
        }

        File.createTempDir().with { projectFolder ->
            deleteOnExit()

            def msbuildTask = new MSBuildTaskBuilder()
                    .withAssemblyName('bar')
                    .withProjectFile(new File(projectFolder.path, 'bar.csproj'))
                    .build()
            project.tasks.add(msbuildTask)

            File projectJson = new File(projectFolder, 'project.json')
            projectJson.createNewFile()
            projectJson.write(
                    '''{
                          "dependencies": {
                            "depBar": "0.2.3.4",
                            "depFoo": "100.5.6",
                            "depBar2": {"version": "1.2.3.4", "type":"build"},
                          },
                          "frameworks": {
                            "net35": {}
                          },
                          "runtimes": {
                            "win": {}
                          }
                        }'''
            )
            File packageConfig = new File(projectFolder, 'packages.config')
            packageConfig.createNewFile()
            packageConfig.write(
                    '''<?xml version="1.0" encoding="utf-8"?>
                        <packages>
                            <package id="depFoo2" version="10.5.7" developmentDependency="false" targetFramework="net35" />
                        </packages>'''
            )

            def expected =
                    '''
                    <package xmlns="http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd">
                        <metadata>
                            <id>foo</id>
                            <version>2.1</version>
                            <description>fooDescription</description>
                            <dependencies>
                                <dependency id="depFoo2" version="10.5.7" />
                                <dependency id="depBar" version="0.2.3.4" />
                                <dependency id="depFoo" version="100.5.6" />
                            </dependencies>
                        </metadata>
                    </package>'''
            def nuspecGenerated = project.tasks.nugetSpec.generateNuspec()
            assertXMLEqual(expected, nuspecGenerated)
        }
    }
}
