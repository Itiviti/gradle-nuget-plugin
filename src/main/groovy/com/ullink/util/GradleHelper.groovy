package com.ullink.util

import org.gradle.api.Project

class GradleHelper {
    def static String getPropertyFromTask(Project project, String property, String task) {
        def theTask =  project.tasks.findByName(task)
        if (theTask) {
            return theTask.property(property)
        } else {
            return null
        }
    }
}
