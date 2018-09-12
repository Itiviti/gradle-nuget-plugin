/*************************************************************************
 * ULLINK CONFIDENTIAL INFORMATION
 * _______________________________
 *
 * All Rights Reserved.
 *
 * NOTICE: This file and its content are the property of Ullink. The
 * information included has been classified as Confidential and may
 * not be copied, modified, distributed, or otherwise disseminated, in
 * whole or part, without the express written permission of Ullink.
 ************************************************************************/
package com.ullink.packageparser

import com.ullink.packagesparser.PackageReferenceParser
import groovy.xml.MarkupBuilder
import spock.lang.Specification

class PackageReferenceParserSpec extends Specification {
    def 'Extract dependencies from PackageReference from csproj'() {
        given:
        def csproj = new File(getClass().getResource('packagereference.csproj').toURI())

        when:
        def result = new PackageReferenceParser().getDependencies(csproj)

        then:
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)
        xml.dependencies() {
            result.each {
                it.resolveStrategy = DELEGATE_FIRST
                it.delegate = delegate
                it.call()
            }
        }
        writer.toString() == '''<dependencies>
  <dependency id='Microsoft.AspNetCore.Mvc.Abstractions' version='1.1.2' />
  <dependency id='Microsoft.AspNetCore.Mvc.Core' version='1.1.2' />
  <dependency id='Microsoft.Extensions.Caching.Abstractions' version='1.1.1' />
  <dependency id='Microsoft.Extensions.Configuration.Binder' version='1.1.1' />
  <dependency id='Newtonsoft.Json' version='9.0.1' />
  <dependency id='StyleCop.Analyzers' version='1.0.0' />
  <dependency id='NuGet.Versioning' version='3.6.0' include='build' exclude='none' />
  <dependency id='System.Xml.XDocument' version='4.3.0' />
</dependencies>'''
    }
}