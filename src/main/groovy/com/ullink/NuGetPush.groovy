package com.ullink

import java.util.List;

import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.StopActionException
import org.gradle.api.tasks.TaskAction

class NuGetPush extends BaseNuGet {
	def nupkgFile
	
    NuGetPush() {
		super('push')
    }
    
	@Override
	void verifyCommand() {
		// TODO verify push, how ?
	}
	
    @Override
    List<String> extraCommands() {
		[ nupkgFile ]
    }
}
