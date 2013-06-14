package io.pst.mojo.s3.sta.uploader.config;

public class Metadata {

    /**
     * The id of this metadata.
     * @parameter
     * @required
     */
    private String id;
    
    /**
     * Cache control directive. The format must be compliant
     * with Cache-Control HTTP header.
     * @parameter
     */
    private String cacheControl;

    /**
     * @parameter
     */
    private String contentDisposition;

    /**
     * Content type value. Should be used for those cases
     * where it is not possible to guess it from file
     * extension.
     * @parameter
     */
    private String contentType;
    
    /**
     * @parameter
     */
    private String contentLanguage;

    /**
     * Number of seconds to add to time when the file is
     * uploaded to S3 for setting Expires metadata.
     * @parameter default-value=0
     */
    private int secondsToExpire;
    
    /**
     * Content encoding. Only plain and gzip are supported
     * currentlt.
     * @parameter default-value="plain"
     */
    private String contentEncoding;

    /**
     * @parameter
     */
    private String websiteRedirectLocation;

    /**
     * The S3 permission to apply to uploaded files. Could be
     * any of the following: AuthenticatedRead, BucketOwnerFullControl,
     * BucketOwnerRead, LogDeliveryWrite, Private, PublicRead, PublicReadWrite.
     * @parameter
     * @required
     */
    private String cannedAcl;
    
    /**
     * Set if this is the default metadata which will be applied
     * to all Binds which don't specify a metadata id.
     * @parameter property="default" default-value=false
     */
    private boolean def;

    public String getId() {
        return id;
    }
    
    public String getCacheControl() {
        return toLowerCase(cacheControl);
    }
    public String getContentDisposition() {
        return toLowerCase(contentDisposition);
    }
    public String getContentType() {
        return toLowerCase(contentType);
    }
    public String getContentLanguage() {
        return toLowerCase(contentLanguage);
    }
    public int getSecondsToExpire() {
        return secondsToExpire;
    }
    public String getContentEncoding() {
        return toLowerCase(contentEncoding);
    }
    public String getWebsiteRedirectLocation() {
        return toLowerCase(websiteRedirectLocation);
    }
    
    public String getCannedAcl() {
        return cannedAcl;
    }
    
    public boolean isDefault() {
        return def;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("metadada [")
                .append(" id=").append(id)
                .append(" cacheControl=").append(cacheControl)
                .append(" contentDisposition=").append(contentDisposition)
                .append(" contentType=").append(contentType)
                .append(" contentLanguage=").append(contentLanguage)
                .append(" secondsToExpire=").append(secondsToExpire)
                .append(" contentEncoding=").append(contentEncoding)
                .append(" websiteRedirectLocation=").append(websiteRedirectLocation)
                .append(" cannedAcl=").append(cannedAcl)
                .append(" ]");
        return builder.toString();
    }
    
    private String toLowerCase(String value) {
        if (value != null) {
            return value.toLowerCase();
        }
        else {
            return null;
        }
    }
}
