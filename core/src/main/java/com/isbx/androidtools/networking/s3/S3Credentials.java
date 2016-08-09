package com.isbx.androidtools.networking.s3;

/**
 * Created by alexs_000 on 5/11/2016.
 */
public class S3Credentials {
    private String expirationDate;
    private String uniqueFilePrefix;
    private String AWSAccessKeyId;
    private String policy;
    private String signature;
    private String bucket;

    public String getAWSAccessKeyId() {
        return AWSAccessKeyId;
    }

    public void setAWSAccessKeyId(String AWSAccessKeyId) {
        this.AWSAccessKeyId = AWSAccessKeyId;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getUniqueFilePrefix() {
        return uniqueFilePrefix;
    }

    public void setUniqueFilePrefix(String uniqueFilePrefix) {
        this.uniqueFilePrefix = uniqueFilePrefix;
    }
}
