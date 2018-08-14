package com.isbx.androidtools.networking.s3;

/**
 * This class contains all of the credential information necessary to make an authenticated upload
 * request to an Amazon S3 bucket. Generally an instance of this class will be returned by an
 * {@link S3CredentialsProvider} which handles the actual authentication process and populates this
 * class with the appropriate information.
 *
 * @see S3CredentialsProvider
 */
public class S3Credentials {
    private String expirationDate;
    private String uniqueFilePrefix;
    private String AWSAccessKeyId;
    private String policy;
    private String signature;
    private String bucket;
    @SerializedName("Content-Type")
    private String contentType;

    /**
     * Returns the public AWS access key to use for authenticating requests with these credentials.
     *
     * @return The AWS access key for these credentials
     */
    public String getAWSAccessKeyId() {
        return AWSAccessKeyId;
    }

    /**
     * Sets the public AWS access key that should be used to authenticate requests using these
     * credentials.
     *
     * @param AWSAccessKeyId A valid AWS access key
     */
    public void setAWSAccessKeyId(String AWSAccessKeyId) {
        this.AWSAccessKeyId = AWSAccessKeyId;
    }

    /**
     * Returns the S3 bucket to make upload requests to with these credentials.
     *
     * @return The name of the Amazon S3 bucket
     */
    public String getBucket() {
        return bucket;
    }

    /**
     * Specifies the S3 bucket to make upload requests to with these credentials.
     *
     * @param bucket The name of the Amazon S3 bucket
     */
    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    /**
     * A String representation of the time at which these credentials will become invalid.
     *
     * @return The expiration date for these credentials as a date string
     */
    public String getExpirationDate() {
        return expirationDate;
    }

    /**
     * Sets time at which these credentials will become invalid.
     *
     * @param expirationDate The expiration date for these credentials
     */
    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    /**
     * A Base64-encoded string representation of the AWS policy object that these credentials
     * represent.
     *
     * <p>
     * For more information on AWS policies, see
     * <a href="http://docs.aws.amazon.com/AmazonS3/latest/dev/using-iam-policies.html">
     * http://docs.aws.amazon.com/AmazonS3/latest/dev/using-iam-policies.html</a>
     * </p>
     *
     * @return The policy object encoded as a Base64 string
     */
    public String getPolicy() {
        return policy;
    }

    /**
     * Sets the AWS policy that these credentials represent as a Base64-encoded string.
     *
     * <p>
     * For more information on AWS policies, see
     * <a href="http://docs.aws.amazon.com/AmazonS3/latest/dev/using-iam-policies.html">
     * http://docs.aws.amazon.com/AmazonS3/latest/dev/using-iam-policies.html</a>
     * </p>
     *
     * @param policy An AWS policy object encoded as a Base64 string
     */
    public void setPolicy(String policy) {
        this.policy = policy;
    }

    /**
     * Returns the signature that should be used to authenticate requests made with these
     * credentials.
     *
     * @return The signature for these credentials
     */
    public String getSignature() {
        return signature;
    }

    /**
     * Sets the signature that should be used authenticate requests using these credentials. This
     * signature should be a hash of the policy for these credentials encrypted with a valid AWS
     * secret key.
     *
     * @param signature An sha1 hash to use to sign requests with these credentials
     */
    public void setSignature(String signature) {
        this.signature = signature;
    }

    /**
     * Returns a prefix that has been requested to be prepended to the keys of any files uploaded
     * using these credentials.
     *
     * @return The file prefix that has been requested for use with these credentials
     */
    public String getUniqueFilePrefix() {
        return uniqueFilePrefix;
    }

    /**
     * Sets an optional prefix that should be prepended to the keys of files uploaded using these
     * credentials.
     *
     * @param uniqueFilePrefix The requested file prefix
     */
    public void setUniqueFilePrefix(String uniqueFilePrefix) {
        this.uniqueFilePrefix = uniqueFilePrefix;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
