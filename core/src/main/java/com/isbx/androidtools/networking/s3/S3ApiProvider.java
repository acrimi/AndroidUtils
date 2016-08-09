package com.isbx.androidtools.networking.s3;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by alexs_000 on 5/11/2016.
 */
public class S3ApiProvider implements S3CredentialsProvider {
    private String apiUrl;

    public S3ApiProvider(String apiUrl) {
        this.apiUrl = apiUrl;
    }

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