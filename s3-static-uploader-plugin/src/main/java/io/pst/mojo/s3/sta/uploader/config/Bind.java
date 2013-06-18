package io.pst.mojo.s3.sta.uploader.config;

public class Bind {

    /**
     * List of expressions matching files to include. Could be
     * regular expressions using the expression %regex[].
     * 
     * @parameter
     * @required
     */
    String pattern;
    
    /**
     * The id of the metadata which will be applied to all the 
     * files matching the pattern parameter.
     * @parameter
     * @required
     */
    String metadataId;

    public String getPattern() {
        return pattern;
    }
    
    public String getMetadataId() {
        return metadataId;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
        result = prime * result + ((metadataId == null) ? 0 : metadataId.hashCode());
        return result;        
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Bind)) {
            return false;
        }
        Bind bind = (Bind) o;
        return (pattern == null ? bind.pattern == null : pattern.equals(bind.pattern))
            && (metadataId == null ? bind.metadataId == null : metadataId.equals(bind.metadataId));
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
