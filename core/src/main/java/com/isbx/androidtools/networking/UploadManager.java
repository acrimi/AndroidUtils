package com.isbx.androidtools.networking;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.isbx.androidtools.networking.s3.S3Credentials;
import com.isbx.androidtools.networking.s3.S3CredentialsProvider;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import cz.msebera.android.httpclient.Header;

/**
 * Created by alexs_000 on 5/11/2016.
 */
public class UploadManager {

    private static final String S3_URL_FORMAT = "https://%s.s3.amazonaws.com";
    private static final String DEFAULT_ACL = "public-read";
    private static final int DEFAULT_SUCCESS_STATUS = 201;
    private static final String DEFAULT_IMAGE_EXTENSION = "jpg";
    private static final int UPLOAD_TIMEOUT_MS = 30000;

    public static final SuffixRule SUFFIX_INCREMENTAL = new SuffixRule() {
        public String getSuffix(Uri uri, int index) {
            return ""+index;
        }
    };

    public static final SuffixRule SUFFIX_DIMENSIONS =
        new IndexedSuffixRule(new String[]{"original", "large", "medium", "small"});

    private Context context;
    private S3CredentialsProvider credentialsProvider;

    public UploadManager(Context context, S3CredentialsProvider credentialsProvider) {
        this.context = context;
        this.credentialsProvider = credentialsProvider;
    }

    public void uploadImages(Uri[] imageUris, UploadListener listener) {
        ImageUploadTask uploadTask = new ImageUploadTask(context, credentialsProvider, listener);
        uploadTask.execute(imageUris);
    }

    public void uploadImages(Uri[] imageUris, SuffixRule suffixRule, UploadListener listener) {
        ImageUploadTask uploadTask = new ImageUploadTask(context, credentialsProvider, listener);
        uploadTask.suffixRule = suffixRule;
        uploadTask.execute(imageUris);
    }

    public void uploadImage(Uri imageUri, UploadListener listener) {
        ImageUploadTask uploadTask = new ImageUploadTask(context, credentialsProvider, listener);
        uploadTask.execute(imageUri);
    }

    public void uploadImage(Uri imageUri, SuffixRule suffixRule, UploadListener listener) {
        ImageUploadTask uploadTask = new ImageUploadTask(context, credentialsProvider, listener);
        uploadTask.suffixRule = suffixRule;
        uploadTask.execute(imageUri);
    }

    public interface UploadListener {
        public void onProgress(int progress);
        public void onUploadComplete(String[] urls);
        public void onUploadFailed(Throwable error, int failureIndex);
    }


    private static class ImageUploadTask extends AsyncTask<Uri, Integer, String[]> {
        private Context context;
        private S3CredentialsProvider credentialsProvider;
        private UploadListener listener;
        private SuffixRule suffixRule = SUFFIX_INCREMENTAL;

        public ImageUploadTask(Context context, S3CredentialsProvider credentialsProvider, UploadListener listener) {
            this.context = context;
            this.credentialsProvider = credentialsProvider;
            this.listener = listener;
        }

        @Override
        protected String[] doInBackground(Uri... uris) {
            S3Credentials credentials = credentialsProvider.getCredentials();
            if (credentials == null) {
                if (listener != null) {
                    listener.onUploadFailed(new IOException("Failed retrieving S3 credentials from provider"), 0);
                }
                cancel(true);
                return null;
            }

            final String[] result = new String[uris.length];

            for (int i = 0; i < uris.length; i++) {
                if (isCancelled()) {
                    break;
                }

                Uri uri = uris[i];
                InputStream in = null;
                try {
                    in = context.getContentResolver().openInputStream(uri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    if (listener != null) {
                        listener.onUploadFailed(e, i);
                    }
                    cancel(true);
                    break;
                }

                if (in != null) {
                    SyncHttpClient client = new SyncHttpClient();
                    client.setTimeout(UPLOAD_TIMEOUT_MS);
                    RequestParams params = new RequestParams();
                    final String key = credentials.getUniqueFilePrefix()+suffixRule.getSuffix(uri, i)+"."+DEFAULT_IMAGE_EXTENSION;
                    params.setForceMultipartEntityContentType(true);
                    params.put("key", key);
                    params.put("AWSAccessKeyId", credentials.getAWSAccessKeyId());
                    params.put("policy", credentials.getPolicy());
                    params.put("signature", credentials.getSignature());
                    params.put("success_action_status", DEFAULT_SUCCESS_STATUS);
                    params.put("acl", DEFAULT_ACL);
                    params.put("file", in);

                    final int index = i;
                    final String url = String.format(S3_URL_FORMAT, credentials.getBucket());
                    client.post(url, params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            result[index] = url + "/" + key;
                            onProgressUpdate((int) (index /(float) result.length * 100));
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            error.printStackTrace();
                            if (listener != null) {
                                listener.onUploadFailed(error, index);
                            }
                            cancel(true);
                        }
                    });
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(String[] urls) {
            if (listener != null) {
                listener.onUploadComplete(urls);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (listener != null && values.length > 0) {
                listener.onProgress(values[0]);
            }
        }
    }


    public interface SuffixRule {
        String getSuffix(Uri uri, int index);
    }

    public static class IndexedSuffixRule implements SuffixRule {
        private String[] suffixes;

        public IndexedSuffixRule(String[] suffixes) {
            this.suffixes = suffixes;
        }

        @Override
        public String getSuffix(Uri uri, int index) {
            return suffixes[index % suffixes.length];
        }
    }
}
