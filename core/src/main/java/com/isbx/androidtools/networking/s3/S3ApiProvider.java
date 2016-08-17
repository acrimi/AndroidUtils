package com.isbx.androidtools.networking.s3;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * An implementation of {@link S3CredentialsProvider} that retrieves pre-signed S3 credentials from
 * a remote API server via HTTP GET requests. The API endpoint provided is expected to give a JSON
 * response that matches the structure of {@link S3Credentials} exactly.
 */
public class S3ApiProvider implements S3CredentialsProvider {
    private String apiUrl;

    /**
     * Creates a new S3ApiProvider instance that will retrieve S3 credentials from the specified url
     * endpoint.
     *
     * @param apiUrl The API url to make requests to for new credentials
     */
    public S3ApiProvider(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    /**
     * Makes a GET request to this provider's API url for new S3 credentials, attempting to parse
     * the response as JSON into an {@link S3Credentials} instance.
     *
     * @return An {@link S3Credentials} representing the response received from the remote server,
     *         or {@code null} if the request failed
     */
    @Override
    public S3Credentials getCredentials() {
        HttpURLConnection urlConnection = null;
        S3Credentials credentials = null;

        try {
            URL url = new URL(apiUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            credentials = new Gson().fromJson(new InputStreamReader(in), S3Credentials.class);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return credentials;
    }
}