package io.pst.mojo.s3.sta.uploader.config;

import java.util.Arrays;
import java.util.List;

import com.amazonaws.services.s3.model.CannedAccessControlList;

public class ParametersValidator {

    private static final String CONTENT_ENCODING_PLAIN = "plain";
    private static final String CONTENT_ENCODING_GZIP = "gzip";    
    private static final List<String> CONTENT_ENCODING_OPTIONS = Arrays.asList(new String[]{CONTENT_ENCODING_PLAIN,CONTENT_ENCODING_GZIP});
    
    private final List<Include> includes;
    private final List<Metadata> metadatas;
    
    public ParametersValidator(List<Include> includes, List<Metadata> metadatas) {
        this.includes = includes;
        this.metadatas = metadatas;
    }
    
    public void validate() {
        validateIncludes();
        validateMetadatas();
    }
    
    private void validateIncludes() {
        boolean existsDefaultMetadata = existsDefaultMetadata();
        for (Include include: includes) {
            if (!existsDefaultMetadata && isEmptyMetadataId(include.getBind().getMetadataId())) {
                throw new IllegalStateException("The include " + include.getBind().getPattern() + " does not define a metadataId and there isn't a default one");
            }
        }
    }
    
    private void validateMetadatas() {
        for (Metadata metadata : metadatas) {
            validateContentEncoding(metadata);
            validateSecondsToExpire(metadata);
            validateCannedAcl(metadata);
        }
    }
    
    private boolean existsDefaultMetadata() {
        boolean existsDefaultMetadata = false;
        for (Metadata metadata : metadatas) {
            if (metadata.isDefault()) {
                existsDefaultMetadata = true;
            }
        }
        return existsDefaultMetadata;
    }
    
    private boolean isEmptyMetadataId(String metadataId) {
        return (metadataId == null || "".equals(metadataId)) ? true : false;
    }
    
    private void validateContentEncoding(Metadata metadata) {
        if (!CONTENT_ENCODING_OPTIONS.contains(metadata.getContentEncoding())) {
            throw new IllegalStateException("The metadata " + metadata.getId() + " has an invalid contentType");        
        }
    }
    
    private void validateSecondsToExpire(Metadata metadata) {
        if (metadata.getSecondsToExpire() < 0) {
            throw new IllegalStateException("The metadata " + metadata.getId() + " has an invalid secondsToExpire");
        }
    }
    
    private void validateCannedAcl(Metadata metadata) {
        try {
            @SuppressWarnings("unused")
            CannedAccessControlList acl = CannedAccessControlList.valueOf(metadata.getCannedAcl());
        } catch(Exception e) {
            throw new IllegalStateException("The metadata " + metadata.getId() + " has an invalid cannedAcl");        
        }
    }
}