package org.quanqi.gradle.android.upload

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.ApplicationVariant
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
import org.gradle.api.Task
import org.gradle.api.plugins.PluginContainer

/**
 * Created by cindy on 12/14/15.
 */
class AndroidMavenPublishPlugin implements Plugin<Project> {

    public static final String GROUP_NAME = 'publish'

    static boolean checkPlugins(Project project) {
        PluginContainer plugins = project.getPlugins()
        return plugins.hasPlugin(AppPlugin) || plugins.hasPlugin(LibraryPlugin)
    }

    @Override
    void apply(Project project) {
        if (!checkPlugins(project)) {
            throw new ProjectConfigurationException("The android plugin must be applied to the project", null)
        }

        project.extensions.create(AndroidMavenPublishExtension.NAME, AndroidMavenPublishExtension.class)

        AppExtension android = project.android
        def uploadAllTask = project.tasks.create("publishToMaven", Task)
        uploadAllTask.group = GROUP_NAME
        uploadAllTask.description = 'publish all apk variant'

        android.applicationVariants.all { ApplicationVariant variant ->

            AndroidMavenPublishTask task = project.tasks.create("publish${variant.name.capitalize()}ToMaven",
                    AndroidMavenPublishTask)
            task.group = GROUP_NAME
            task.description = "publish ${variant.name} to maven"
            task.variant = variant
            uploadAllTask.dependsOn(task)
        }
    }
}
