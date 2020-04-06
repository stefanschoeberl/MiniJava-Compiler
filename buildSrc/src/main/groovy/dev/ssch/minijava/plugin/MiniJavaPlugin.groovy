package dev.ssch.minijava.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class MiniJavaPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.configurations {
            minijava
        }
    }
}
