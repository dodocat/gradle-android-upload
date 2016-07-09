package org.quanqi.gradle.android.upload

import com.squareup.okhttp.*
import org.gradle.api.GradleException
import org.gradle.tooling.BuildException

import java.util.concurrent.TimeUnit
import java.util.logging.Logger

/**
 * Post a single file to maven.
 */
class MavenDeploy {

    /**
     * Nexus api endpoint
     *
     * e.g. http://localhost:8081/nexus/service/local/artifact/maven/content
     */
    String url

    String user
    String password

    /**
     * g
     */
    String repository;

    /**
     * a
     */
    String group

    /**
     * v
     */
    String artifact

    /**
     * p
     */
    String version

    /**
     * c
     */
    String packaging

    /**
     * e
     */
    String extension

    /**
     * r
     */
    String classifier

    File file

    void deploy(Logger logger, OkHttpClient httpClient) {
        logger.info(String.format('uploading %1$s to %2$s at %3$s', file.name, repository, url))

        MultipartBuilder multiPartBuilder = new MultipartBuilder()
                .addFormDataPart('r', repository)
                .addFormDataPart('g', group)
                .addFormDataPart('a', artifact)
                .addFormDataPart('v', version)
                .addFormDataPart('p', packaging)
                .addFormDataPart('e', extension)
                .addFormDataPart('c', classifier)
                .addFormDataPart('hasPom', 'false')
                .addFormDataPart('file', file.name, RequestBody.create(MediaType.parse("maven/$packaging"), file))

        Response response
        try {
            Request.Builder requestBuilder = new Request.Builder()
                    .url(url)
                    .post(multiPartBuilder.build());

            if (user && password) {
                requestBuilder.addHeader("Authorization", Credentials.basic(user, password));
            }

            response = httpClient.newCall(requestBuilder.build()).execute()
            if (response.code() >= 400) {
                throw new GradleException(String.format('error %1$d while uploading `%2$s\'; HTTP response: %3$s',
                        response.code(), file.name, response.body().string()))
            }
            logger.info(String.format('upload of `%s\' successful', file.name))
        }
        finally {
            if (response != null) {
                response.body().close()
            }
        }
    }
}
