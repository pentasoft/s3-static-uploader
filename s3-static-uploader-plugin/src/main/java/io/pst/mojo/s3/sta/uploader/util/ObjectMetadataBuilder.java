package io.pst.mojo.s3.sta.uploader.util;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import javax.activation.MimetypesFileTypeMap;

import com.amazonaws.services.s3.model.ObjectMetadata;

import io.pst.mojo.s3.sta.uploader.config.ManagedFile;

public class ObjectMetadataBuilder {

    private static final MimetypesFileTypeMap mimeMap = new MimetypesFileTypeMap();
    
    private final ManagedFile managedFile;
    private final File encodedFile;
    private final ObjectMetadata objectMetadata;
    
    public ObjectMetadataBuilder(ManagedFile managedFile, File encodedFile) {
        this.managedFile = managedFile;
        this.encodedFile = encodedFile;
        this.objectMetadata = new ObjectMetadata();
    }
    
    public ObjectMetadata buildMetadata() {
        assignContentLength();
        assignLastModified();
        assignContentEncoding();
        assignContentType();
        assignCacheControl();
        assignContentDisposition();
        assignContentLanguage();
        assignExpires();
        assignWebsiteRedirectLocation();
        
        return objectMetadata;
    }
    
    private void assignContentLength() {
        objectMetadata.setContentLength(encodedFile.length());
    }
    
    private void assignLastModified() {
        objectMetadata.setLastModified(new Date(encodedFile.lastModified()));
    }
    
    private void assignContentEncoding() {
        if (!encodedFile.equals(new File(managedFile.getFilename()))) {
            objectMetadata.setContentEncoding(managedFile.getMetadata().getContentEncoding());
        }
    }
    
    private void assignContentType() {
        String contentType;
        if (!isEmptyContentType(managedFile.getMetadata().getContentType())) {
            contentType = managedFile.getMetadata().getContentType();
        } else {
            contentType = mimeMap.getContentType(managedFile.getFilename());
        }
        objectMetadata.setContentType(contentType);
    }
    
    private void assignCacheControl() {
        if (managedFile.getMetadata().getCacheControl() != null) {
            objectMetadata.setCacheControl(managedFile.getMetadata().getCacheControl());
        }
    }
    
    private void assignContentDisposition() {
        if (managedFile.getMetadata().getContentDisposition() != null) {
            objectMetadata.setContentDisposition(managedFile.getMetadata().getContentDisposition());
        }
    }

    private void assignContentLanguage() {
        if (managedFile.getMetadata().getContentLanguage() != null) {
            objectMetadata.setHeader("Content-Disposition", managedFile.getMetadata().getContentLanguage());
        }
    }

    private void assignExpires() {
        if (managedFile.getMetadata().getSecondsToExpire() != 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, managedFile.getMetadata().getSecondsToExpire());
            objectMetadata.setHttpExpiresDate(calendar.getTime());
        }
    }
    
    private void assignWebsiteRedirectLocation() {
        if (managedFile.getMetadata().getWebsiteRedirectLocation() != null) {
            objectMetadata.setHeader("x-amz-website-redirect-location", managedFile.getMetadata().getWebsiteRedirectLocation());
        }
    }

    private boolean isEmptyContentType(String contentType) {
        return (contentType == null || "".equals(contentType)) ? true : false;
    }
}
