package com.isbx.androidtools.networking.s3;

/**
 * An interface for generating credentials for AWS requests. Implementations of this interface
 * should provide a mechanism for create {@link S3Credentials} instances that are valid and signed.
 */
public interface S3CredentialsProvider {
    /**
     * Returns a new instance of {@link S3Credentials} to be used to make authenticated requests to
     * AWS.
     *
     * @return A valid {@link S3Credentials} object
     */
    S3Credentials getCredentials();
}
