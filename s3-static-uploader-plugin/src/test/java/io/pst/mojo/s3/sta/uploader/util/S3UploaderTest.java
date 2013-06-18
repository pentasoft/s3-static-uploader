package io.pst.mojo.s3.sta.uploader.util;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;

import io.pst.mojo.s3.sta.uploader.config.ManagedFile;
import io.pst.mojo.s3.sta.uploader.config.StaticMetadataFactory;

public class S3UploaderTest {

    private File tempFolder;
    private File inputDirectory;
    private AmazonS3Client client;
    private Log log;
    private List<ManagedFileContentEncoder> contentEncoders;
    private ManagedFile managedFile;
    private S3Uploader uploader;
    
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    @Before
    public void setUp() {
        tempFolder = folder.newFolder("myTemp");
        inputDirectory = new File("src/test/resources");
        client = mock(AmazonS3Client.class);
        log = mock(Log.class);
        contentEncoders = getContentEncodersList();
        managedFile = new ManagedFile("src/test/resources/Images/1.jpg", StaticMetadataFactory.createStatic());
    }

    @Test
    public void testsUploadNonExistingFile() throws Exception {
        uploader = new S3Uploader(client, log, contentEncoders, "theBucket", inputDirectory, tempFolder, false);
        
        when(client.getObjectMetadata("theBucket", "Images/1.jpg")).thenThrow(new AmazonServiceException("No object metadata found"));
        
        uploader.uploadManagedFile(managedFile);
        
        verify(client).putObject(eq("theBucket"), eq("Images/1.jpg"), any(FileInputStream.class), any(ObjectMetadata.class));
    }
    
    @Test
    public void testsSkipCoincidentFile() throws Exception {
        uploader = new S3Uploader(client, log, contentEncoders, "theBucket", inputDirectory, tempFolder, false);
        ObjectMetadata objectMetadata = buildObjectMetadataCoincidentFile();
        
        when(client.getObjectMetadata("theBucket", "Images/1.jpg")).thenReturn(objectMetadata);
        
        uploader.uploadManagedFile(managedFile);
        
        verify(client, never()).putObject(eq("theBucket"), eq("Images/1.jpg"), any(FileInputStream.class), any(ObjectMetadata.class));
        verify(client, never()).copyObject(any(CopyObjectRequest.class));
    }
    
    @Test
    public void testsRefreshExpiredObject() throws Exception {
        uploader = new S3Uploader(client, log, contentEncoders, "theBucket", inputDirectory, tempFolder, true);
        ObjectMetadata objectMetadata = buildObjectMetadataExpiredFile();
        
        when(client.getObjectMetadata("theBucket", "Images/1.jpg")).thenReturn(objectMetadata);
        
        uploader.uploadManagedFile(managedFile);
        
        verify(client).copyObject(any(CopyObjectRequest.class));
    }

    private List<ManagedFileContentEncoder> getContentEncodersList() {
        List<ManagedFileContentEncoder> contentEncoders = new ArrayList<ManagedFileContentEncoder>();
        contentEncoders.add(new ManagedFileContentEncoderGZipImpl(tempFolder));
        contentEncoders.add(new ManagedFileContentEncoderPlainImpl());
        return contentEncoders;
    }
    
    private ObjectMetadata buildObjectMetadataCoincidentFile() throws Exception {
        File encodedFile = encodeManagedFile(managedFile);
        ObjectMetadata objectMetadata = new ObjectMetadata();
        // ETag only, as this is the property used for comparing files
        objectMetadata.setHeader("ETag", calculateETag(encodedFile));
        return objectMetadata;
    }

    private ObjectMetadata buildObjectMetadataExpiredFile() throws Exception {
        File encodedFile = encodeManagedFile(managedFile);
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setHeader("ETag", calculateETag(encodedFile));
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -1);
        objectMetadata.setHttpExpiresDate(calendar.getTime());
        return objectMetadata;
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
    
    private String calculateETag(File file) throws Exception {
        return Hex.encodeHexString(DigestUtils.md5(new FileInputStream(file)));
    }    
}
