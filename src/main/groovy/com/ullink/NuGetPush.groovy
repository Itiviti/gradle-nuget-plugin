package com.ullink

import java.util.List;

import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.StopActionException
import org.gradle.api.tasks.TaskAction

class NuGetPush extends BaseNuGet {
	def nupkgFile
	def Symbols
	String serverUrl
	String apiKey
	String timeout

    NuGetPush() {
		super('push')
    }

	@Override
	void verifyCommand() {
		// TODO verify push, how ?
	}

    @Override
    List<String> extraCommands() {
		def commandLineArgs = [nupkgFile]
		if (serverUrl)
		{
			commandLineArgs += "-Source"
			commandLineArgs += serverUrl
		}
		if (apiKey)
		{
			commandLineArgs += "-ApiKey"
			commandLineArgs += apiKey
		}
		if (timeout)
		{
			commandLineArgs += "-Timeout"
			commandLineArgs += timeout
		}
		return commandLineArgs
    }
}
