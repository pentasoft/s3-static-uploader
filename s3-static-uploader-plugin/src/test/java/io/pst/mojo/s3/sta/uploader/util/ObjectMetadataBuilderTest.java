package io.pst.mojo.s3.sta.uploader.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.amazonaws.services.s3.model.ObjectMetadata;

import io.pst.mojo.s3.sta.uploader.config.ManagedFile;
import io.pst.mojo.s3.sta.uploader.config.StaticMetadataFactory;

public class ObjectMetadataBuilderTest {

    private ManagedFile managedFile;
    private ManagedFileContentEncoder encoder;
    private File encodedFile;
    private ObjectMetadataBuilder builder;
    private File tempFolder;
    
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    @Before
    public void setUp() {
        tempFolder = folder.newFolder("myTemp");
    }
    
    @Test
    public void testsObjectMetadataGuessedContentType() throws Exception {
        managedFile = new ManagedFile("src/test/resources/Images/1.jpg", StaticMetadataFactory.createStatic());
        encoder = new ManagedFileContentEncoderGZipImpl(tempFolder);
        encodedFile = encoder.encode(managedFile);
        builder = new ObjectMetadataBuilder(managedFile, encodedFile);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, managedFile.getMetadata().getSecondsToExpire());

        ObjectMetadata builtObjectMetadata = builder.buildMetadata();
        
        assertThat(builtObjectMetadata.getContentLength(), is(encodedFile.length()));
        assertThat(builtObjectMetadata.getLastModified(), is(new Date(encodedFile.lastModified())));
        assertThat(builtObjectMetadata.getContentEncoding(), is("gzip"));
        assertThat(builtObjectMetadata.getContentType(), is("image/jpeg"));
        assertThat(builtObjectMetadata.getCacheControl(), is("public, max-age=31536000"));
        assertThat(builtObjectMetadata.getHttpExpiresDate(), is(calendar.getTime()));
    }
    
    @Test
    public void testsObjectMetadataSpecifiedContentType() throws Exception {
        managedFile = new ManagedFile("src/test/resources/index", StaticMetadataFactory.createVolatileNaked());
        encoder = new ManagedFileContentEncoderGZipImpl(tempFolder);
        encodedFile = encoder.encode(managedFile);
        builder = new ObjectMetadataBuilder(managedFile, encodedFile);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, managedFile.getMetadata().getSecondsToExpire());

        ObjectMetadata builtObjectMetadata = builder.buildMetadata();
        
        assertThat(builtObjectMetadata.getContentType(), is("text/html"));
    }
}
