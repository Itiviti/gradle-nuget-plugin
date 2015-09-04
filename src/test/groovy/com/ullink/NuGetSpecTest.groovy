package com.ullink

import org.junit.Before

import static org.junit.Assert.*
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
    public void generateNuspec_OverridDefaultValue() {
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
                    version: '4.5'
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
}