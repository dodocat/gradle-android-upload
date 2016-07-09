package org.quanqi.gradle.android.upload

import com.android.build.gradle.api.ApplicationVariant
import com.squareup.okhttp.OkHttpClient
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import java.util.concurrent.TimeUnit
import java.util.logging.Logger

/**
 * Created by cindy on 12/18/15.
 */
class AndroidMavenPublishTask extends DefaultTask {

    static Logger sLogger = Logger.getLogger(AndroidMavenPublishTask.class.getName())
    static OkHttpClient sHttpClient = null

    ApplicationVariant variant
    AndroidMavenPublishExtension publishExtension

    public AndroidMavenPublishTask() {
        super()
        this.description = "Upload android apks and mapping file to maven"
    }

    @TaskAction
    def upload() {
        publishExtension = project."${AndroidMavenPublishExtension.NAME}"
        if (sHttpClient == null) {
            sHttpClient = createHttpClient()
        }
        uploadApk()
        uploadMapping()
    }

    def uploadApk() {
        MavenDeploy deploy = createBasicDeployConfiguration()
        deploy.classifier = variant.name
        deploy.extension = 'apk'
        deploy.packaging = 'apk'
        variant.outputs.each {
            if (it.outputFile.path.endsWith('apk')) {
                deploy.file = it.outputFile
                return true
            }
        }

        if (!deploy.file) {
            throw new GradleException(String.format("could not determine APK output file for variant %s", variant.name))
        }

        if (!deploy.file.exists()) {
            throw new GradleException(String.format('output file %1$s for variant %2$s does not exist', deploy.file, variant.name))
        }

        deploy.deploy(sLogger, sHttpClient)
    }

    def uploadMapping() {
        if (!variant.mappingFile || !variant.mappingFile.exists()) {
            sLogger.info("skipping non-existing mapping file for variant " + variant.name)
            return
        }

        MavenDeploy deploy = createBasicDeployConfiguration()
        deploy.classifier = "${variant.name}-mapping"
        deploy.extension = 'txt'
        deploy.packaging = 'txt'
        deploy.file = variant.mappingFile
        deploy.deploy(sLogger, sHttpClient)
    }

    MavenDeploy createBasicDeployConfiguration() {
        MavenDeploy mavenDeploy = new MavenDeploy()
        if (publishExtension.groupId) {
            mavenDeploy.group = publishExtension.groupId
        } else if (project.group) {
            mavenDeploy.group = project.group
        } else {
            throw new GradleException("no `groupId' or `project.group' configured or empty")
        }

        if (publishExtension.artifactId != null) {
            mavenDeploy.artifact = publishExtension.artifactId
        } else {
            mavenDeploy.artifact = project.name
        }

        mavenDeploy.url = publishExtension.url
        if (!publishExtension.repository) {
            throw new GradleException("no `repository' to upload to configured or empty")
        }
        mavenDeploy.repository = publishExtension.repository
        mavenDeploy.user = publishExtension.user
        mavenDeploy.password = publishExtension.password
        mavenDeploy.version = variant.versionName
        return mavenDeploy
    }

    static OkHttpClient createHttpClient() {
        OkHttpClient httpClient = new OkHttpClient();
        httpClient.setConnectTimeout(10, TimeUnit.SECONDS)
        httpClient.setReadTimeout(60, TimeUnit.SECONDS)
        return httpClient
    }
}
