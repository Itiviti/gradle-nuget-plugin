package com.ullink

import org.gradle.api.Task
import org.junit.Before
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test
import org.custommonkey.xmlunit.XMLUnit
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual

class NuGetSpecTest {

    private Project project

    @Before
    public void init() {
        XMLUnit.setIgnoreWhitespace(true)
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'nuget'
    }

    @Test
    public void generateNuspec_Closure() {
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
                <version>unspecified</version>
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
                <version>unspecified</version>
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
        Project project = ProjectBuilder.builder().withName('foo').build()
        project.with {
            description = 'fooDescription'
            version = '2.1'
            apply plugin: 'nuget'
        }

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
        Project project = ProjectBuilder.builder().withName('foo').build()
        project.with {
            description = 'fooDescription'
            version = '2.1'
            apply plugin: 'nuget'
        }

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
    public void generateNuspec_defaultFilesFromCsproj() {
        Project project = ProjectBuilder.builder().withName('foo').build()
        project.with {
            apply plugin: 'nuget'
        }
        def msbuildTask = new MSBuildTaskBuilder()
                .withAssemblyName('bar')
                .withFrameworkVersion('v3.5')
                .withArtifact('folder/bin/bar.dll')
                .withProjectFile('folder/does not exist')
                .build()
        project.tasks.add(msbuildTask)

        project.nugetSpec {
            nuspec { }
        }

        def expected =
                '''
        <package xmlns="http://schemas.microsoft.com/packaging/2011/08/nuspec.xsd">
            <metadata>
                <id>foo</id>
                <version>unspecified</version>
                <description>foo</description>
            </metadata>
            <files>
                <file src='folder\\bin\\bar.dll' target='lib/net35' />
            </files>
        </package>'''.replace('\\', File.separator)
        assertXMLEqual (expected, project.tasks.nugetSpec.generateNuspec())
    }

    @Test
    public void generateNuspec_defaultDependenciesFromPackageConfig() {
        Project project = ProjectBuilder.builder().withName('foo').build()
        project.with {
            apply plugin: 'nuget'
        }

        project.nugetSpec {
            nuspec {}
        }

        File.createTempDir().with { projectFolder ->
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
                            <version>unspecified</version>
                            <description>foo</description>
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
}