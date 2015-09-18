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
                    title 'fooTitle'
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
                <title>fooTitle</title>
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
                    title: 'fooTitle',
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
                <title>fooTitle</title>
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
                <title>foo</title>
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
                    title: 'barTitle'
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
                <title>barTitle</title>
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

        withMSBuildTask(project, 'bar', new File('c:\\folder\\bin\\bar.dll'))

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
                <title>foo</title>
            </metadata>
            <files>
                <file src='c:\\folder\\bin\\bar.dll' target='lib/net35' />
            </files>
        </package>'''
        assertXMLEqual (expected, project.tasks.nugetSpec.generateNuspec())
    }

    private static withMSBuildTask(Project project, String assemblyName, File artifact) {
        def msbuildTask = [
                getName: { 'msbuild' }
        ] as Task
        msbuildTask.metaClass.getMainProject = {
            def mainProject = new Object()
            mainProject.metaClass.getProperties = {
                [
                        'AssemblyName'          : assemblyName,
                        'TargetFrameworkVersion': 'v3.5'
                ]
            }

            mainProject.metaClass.getDotnetArtifacts = { [ artifact ] }
            mainProject
        }

        project.tasks.add(msbuildTask)
    }
}