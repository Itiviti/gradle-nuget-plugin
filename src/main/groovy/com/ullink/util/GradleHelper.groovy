package com.ullink.util

import org.gradle.api.Project
import org.gradle.api.UnknownTaskException

class GradleHelper {
    def static String getPropertyFromTask(Project project, String property, String task) {
        try {
            def theTask = project.tasks.getByName(task)
            return theTask.property(property)
        } catch (UnknownTaskException | MissingPropertyException e ) {
            return null
        }
    }
}
