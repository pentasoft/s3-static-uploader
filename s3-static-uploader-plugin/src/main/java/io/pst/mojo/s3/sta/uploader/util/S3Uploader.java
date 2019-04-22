package io.pst.mojo.s3.sta.uploader.util;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static java.util.Objects.nonNull;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.maven.plugin.logging.Log;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;

import io.pst.mojo.s3.sta.uploader.config.ManagedFile;

public class S3Uploader {

    private final AmazonS3Client client;
    private final Log log;
    private final String bucketName;
    private final File inputDirectory;
    private final List<ManagedFileContentEncoder> contentEncoders;
    private final boolean refreshExpiredObjects;
    private final SimpleDateFormat httpDateFormat;
    
    public S3Uploader(AmazonS3Client client, Log log, List<ManagedFileContentEncoder> contentEncoders, String bucketName, File inputDirectory, File tmpDirectory, boolean refreshExpiredObjects) {
        this.client = client;
        this.log = log;
        this.bucketName = bucketName;
        this.inputDirectory = inputDirectory;
        this.contentEncoders = contentEncoders;
        this.refreshExpiredObjects = refreshExpiredObjects;
        this.httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        this.httpDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public void uploadManagedFile(ManagedFile managedFile) throws Exception {
        File encodedFile = encodeManagedFile(managedFile);
        String remoteFileName = getRemoteFileName(managedFile);
        ObjectMetadata remoteMetadata =  retrieveObjectMetadata(remoteFileName);
        ObjectMetadataBuilder objectMetadataBuilder = new ObjectMetadataBuilder(managedFile, encodedFile);
        ObjectMetadata objectMetadata = objectMetadataBuilder.buildMetadata();
        
        if (!isLocalFileSameAsRemote(encodedFile, remoteMetadata)) {
            log.info("uploading file " + managedFile.getFilename() + " to " + bucketName);
            client.putObject(bucketName, remoteFileName, new FileInputStream(encodedFile), objectMetadata);
            setObjectAcl(managedFile, remoteFileName);
        } else {
            if (refreshExpiredObjects && isMetadataExpired(remoteMetadata)) {
                log.info("refreshing metadata for file " + managedFile.getFilename());
                client.copyObject(buildCopyObjectRequest(remoteFileName, objectMetadata));
                setObjectAcl(managedFile, remoteFileName);
            } else {
                log.info("the object " + remoteFileName + " stored at " + bucketName + " does not require update");
            }
        }
    }
    
    private String getRemoteFileName(ManagedFile managedFile) {
        File file = new File(managedFile.getFilename());
        return transformFileNameSlashesToS3(removeBasePath(file));        
    }

    private boolean isLocalFileSameAsRemote(File localFile, ObjectMetadata remoteFileMetadata) throws Exception { 
        if (remoteFileMetadata != null && 
                remoteFileMetadata.getETag().equals(calculateETag(localFile))) {
            return true;
        }
        return false;
    }
    
    private String transformFileNameSlashesToS3(String fileName) {
        return fileName.replace("\\", "/");
    }
    
    private String removeBasePath(File file) {
        return file.getPath().substring(inputDirectory.getPath().length() + 1);
    }
    
    private ObjectMetadata retrieveObjectMetadata(String remoteFileName) {
        log.info("retrieving metadata for " + remoteFileName);
        ObjectMetadata objectMetadata = null;
        try {
            objectMetadata = client.getObjectMetadata(bucketName, remoteFileName);
            logObjectMetadata(remoteFileName, objectMetadata);
        } catch (AmazonServiceException e) {
            log.info("  no object metadata found");
        }
        return objectMetadata;
    }

    private String calculateETag(File file) throws Exception {
        return Hex.encodeHexString(DigestUtils.md5(new FileInputStream(file)));
    }
    
    private File encodeManagedFile(ManagedFile managedFile) throws Exception {
        File encodedFile = null;
        for (ManagedFileContentEncoder contentEncoder : contentEncoders) {
            if (contentEncoder.isContentEncodingSupported(managedFile.getMetadata().getContentEncoding())) {
                log.info("contentEncoding file " + managedFile.getFilename());                
                encodedFile = contentEncoder.encode(managedFile);
            }
        }
        return encodedFile;
    }
    
    private void setObjectAcl(ManagedFile managedFile, String remoteFileName) {
        CannedAccessControlList acl = CannedAccessControlList.valueOf(managedFile.getMetadata().getCannedAcl()); 
        client.setObjectAcl(bucketName, remoteFileName, acl);
    }
    
    private boolean isMetadataExpired(ObjectMetadata objectMetadata) {
        return null != objectMetadata.getHttpExpiresDate()
            || objectMetadata.getHttpExpiresDate().before(new Date());
    }
    
    private void logObjectMetadata(String remoteFileName, ObjectMetadata objectMetadata) {
        log.info( "  ETag: " + objectMetadata.getETag());
        log.info( "  ContentType: " + objectMetadata.getContentType());
        log.info( "  CacheControl: " + objectMetadata.getCacheControl());
        log.info( "  ContentEncoding: " + objectMetadata.getContentEncoding());
        log.info( "  ContentLength: " + objectMetadata.getContentLength());
        log.info( "  Expires: " + (objectMetadata.getHttpExpiresDate() == null ? "unknown" : 
                                    httpDateFormat.format(objectMetadata.getHttpExpiresDate())));
        log.info( "  LastModified: " + objectMetadata.getLastModified());        
    }
    
    private CopyObjectRequest buildCopyObjectRequest(String remoteFileName, ObjectMetadata objectMetadata) {
        return new CopyObjectRequest(bucketName, remoteFileName, bucketName, remoteFileName)
            .withNewObjectMetadata(objectMetadata);        
    }
}
