package io.pst.mojo.s3.sta.uploader.config;

public class Bind {

    /**
     * List of expressions matching files to include. Could be
     * regular expressions using the expression %regex[].
     * 
     * @parameter
     * @required
     */
    private String pattern;
    
    /**
     * The id of the metadata which will be applied to all the 
     * files matching the pattern parameter.
     * @parameter
     * @required
     */
    private String metadataId;

    public String getPattern() {
        return pattern;
    }

    public String getMetadataId() {
        return metadataId;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("bind [")
                .append(" pattern=").append(pattern)
                .append(" metadataId=").append(metadataId)
                .append(" ]");
        return builder.toString();
    }
}
