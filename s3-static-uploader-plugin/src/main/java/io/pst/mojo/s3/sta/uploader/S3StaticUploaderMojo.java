package io.pst.mojo.s3.sta.uploader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.GZIPOutputStream;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.FileUtils;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * @prefix s3-static-uploader
 * @requiresProject true
 * @requiresOnline true
 * @goal upload
 * @phase prepare-package
 * @description Uploads static site content to AWS S3
 */
public class S3StaticUploaderMojo extends AbstractMojo {

    private static final String CONTENT_ENCODING_PLAIN = "plain";

    private static final String CONTENT_ENCODING_GZIP = "gzip";

    @SuppressWarnings("unchecked")
    private static final List<String> CONTENT_ENCODING_OPTIONS = Arrays.asList(new String[]{CONTENT_ENCODING_PLAIN,CONTENT_ENCODING_GZIP});

    private static final int BUFFER_SIZE = 4096;
    
    public static final String S3_URL = "s3.amazonaws.com";
    
    private static final MimetypesFileTypeMap mimeMap = new MimetypesFileTypeMap();
    
    private static final SimpleDateFormat httpDateFormat;
     
    static{
        httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        httpDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
    
    /**
     * @parameter property="accessKey"
     */
    private String accessKey;
    
    /**
     * @parameter property="secretKey"
     */
    private String secretKey;
    
    /**
     * @parameter property="bucketName" 
     */
    private String bucketName;
    
    /**
    * List of regular expressions matching files to exclude. 
    *
    * @parameter
    */
    private List<String> excludes;
    
    /**
    * List of regular expressions matching files to include. 
    *
    * @parameter
    */
    private List<String> includes;
    
    /**
    * The directory where the webapp is built.
    *
    * @parameter default-value="${project.build.directory}/${project.build.finalName}"
    * @required
    */
    private File outputDirectory;
    
    /**
    * Single directory for extra files to include in the WAR. This is where
    * you place your JSP files.
    *
    * @parameter default-value="${basedir}/src/main/webapp"
    * @required
    */
    private File inputDirectory;
    
    /**
    * Directory to encode files before uploading
    *
    * @parameter default-value="${project.build.directory}/temp"
    * @required
    */
    private File tmpDirectory;
    
    /**
    * Content Encoding Type
    *
    * @parameter default-value="gzip"
    * @required
    */
    private String contentEncoding;
    
    /**
     * Mime type for extensionless files
     * 
     * @parameter default-value="text/html"
     * @required 
     */
    private String extensionLessMimeType;
    
    private AmazonS3Client client;
    
    public void execute() throws MojoExecutionException {
        logParameters();
        checkContentEncoding();
        client = createS3Client();
        List<String> fileNames = getFileNamesList();
        processFiles(fileNames); 
    }
    
    private void logParameters() {
        getLog().info("tmpDirectory " + tmpDirectory.getPath());
        getLog().info("inputDirectory " + inputDirectory.getPath());
        getLog().info("outputDirectory " + outputDirectory.getPath());
        getLog().info("includes " + includes);
        getLog().info("excludes " + excludes);
        getLog().info("extesionLessMIMEType " + extensionLessMimeType);
    }
    
    private void checkContentEncoding() throws MojoExecutionException {
        if (!contains(CONTENT_ENCODING_OPTIONS, contentEncoding)) {
            throw new MojoExecutionException("contentEncoding " + contentEncoding + " must be in " + CONTENT_ENCODING_OPTIONS);
        }
        getLog().info("using contentEncoding " + contentEncoding);
    }
    
    private AmazonS3Client createS3Client() {
        return new AmazonS3Client(new BasicAWSCredentials(accessKey,secretKey));
    }
    
    private List<String> getFileNamesList() throws MojoExecutionException {
        try {
            getLog().info( "determining files that should be uploaded" );
            getLog().info("");
            return FileUtils.getFileNames(inputDirectory, convertToString(includes), convertToString(excludes), true, false);
            
        } catch (IOException e) {
            throw new MojoExecutionException("cannot determine the files to be processed", e);
        }
    }
    
    private void processFiles(List<String> fileNames) throws MojoExecutionException {
        for (String fileName: fileNames) {
            processFile(new File(fileName));
        }
    }

    private void processFile(File file) throws MojoExecutionException {
        getLog().info("start processing file " + file.getPath());     
        
        File encodedFile = encodeFile(file);
        String contentType = getContentType(file);
        String s3FileName = transformFileNameSlashesToS3(removeBasePath(file));
        
        if (!isLocalFileSameAsRemoteOne(encodedFile, s3FileName)) {
            uploadFile(encodedFile, s3FileName, contentType);
            
        } else {
            getLog().info("the object " + s3FileName + " stored at " + bucketName + " does not require update");
        }
        
        getLog().info("finnish processing file " + file.getPath());
        getLog().info("");
    }
    
    private File encodeFile(File file) throws MojoExecutionException {
        getLog().info("contentEncoding file " + file.getPath() + " using " + contentEncoding);
        if (!tmpDirectory.exists() && !tmpDirectory.mkdirs()) {
            throw new MojoExecutionException("cannot create directory " + tmpDirectory);
        }
        
        File encodedFile = null;
        if (CONTENT_ENCODING_PLAIN.equalsIgnoreCase(contentEncoding)) {
            encodedFile = file;
        }
        else if (CONTENT_ENCODING_GZIP.equalsIgnoreCase(contentEncoding)) {
            FileInputStream fis = null;
            GZIPOutputStream gzipos = null;
            try {
                byte buffer[] = new byte[BUFFER_SIZE];
                encodedFile = File.createTempFile(file.getName() + "-", ".tmp", tmpDirectory);
                fis = new FileInputStream(file);
                gzipos = new GZIPOutputStream(new FileOutputStream(encodedFile));
                int read = 0;
                do {
                    read = fis.read(buffer, 0, buffer.length);
                    if (read>0)
                        gzipos.write(buffer, 0, read);
                } while (read>=0);
            } catch (Exception e) {
                throw new MojoExecutionException("could not process " + file.getName(), e);
            } finally {
                if (fis!= null)
                    try {
                        fis.close();
                    } catch (IOException e) {
                        throw new MojoExecutionException("could not process " + file.getName(), e);
                    }
                if (gzipos!= null)
                    try {
                        gzipos.close();
                    } catch (IOException e) {
                        throw new MojoExecutionException("could not process "+ encodedFile.getName(), e);
                    }
            }
        }
        return encodedFile;
    }
    
    private String getContentType(File file) throws MojoExecutionException {
        String mimeType = "";
        if (file.isFile()) {
            if (hasExtension(file)) {
                mimeType = mimeMap.getContentType(file);
            }
            else {
                mimeType = extensionLessMimeType;
            }
            return mimeType;
        }
        throw new MojoExecutionException("can't evaluate MIME type for a folder");
    }
    
    private String removeBasePath(File file) {
        return file.getPath().substring(inputDirectory.getPath().length() + 1);
    }
    
    private String transformFileNameSlashesToS3(String fileName) {
        return fileName.replace("\\", "/");
    }

    private boolean isLocalFileSameAsRemoteOne(File localFile, String remoteFileName) 
                                                                    throws MojoExecutionException {

        ObjectMetadata remoteFileMetadata = retrieveObjectMetadata(remoteFileName);
        if (remoteFileMetadata != null && remoteFileMetadata.getETag().equals(calculateETag(localFile))) {
            return true;
        }
        return false;
    }
    
    private void uploadFile(File file, String remoteFileName, String contentType) throws MojoExecutionException {
        getLog().info("uploading file " + file + " to " + bucketName);    
        try {
            getLog().info("content type for " + file.getName() + " is " + contentType);
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(file.length());
            objectMetadata.setHeader("Cache-Control", "public, s-maxage=315360000, max-age=315360000");
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.YEAR, 10);
            objectMetadata.setHeader("Expires", httpDateFormat.format(calendar.getTime()));
            objectMetadata.setLastModified(new Date(file.lastModified()));
            if (!CONTENT_ENCODING_PLAIN.equalsIgnoreCase(contentEncoding)) {
                objectMetadata.setContentEncoding(contentEncoding.toLowerCase());
            }
            objectMetadata.setContentType(contentType);
            client.putObject(bucketName, remoteFileName, new FileInputStream(file), objectMetadata);
            client.setObjectAcl(bucketName, remoteFileName, CannedAccessControlList.PublicRead); 
        } catch (AmazonServiceException e) {
            throw new MojoExecutionException("could not upload file " + file.getName(), e);
        } catch (AmazonClientException e) {
            throw new MojoExecutionException("could not upload file " + file.getName(), e);
        } catch (FileNotFoundException e) {
            getLog().error(e);
        }
    }
    
    private ObjectMetadata retrieveObjectMetadata(String remoteFileName) throws MojoExecutionException {
        getLog().info("retrieving metadata for " + remoteFileName);
        ObjectMetadata objectMetadata = null;

        try {
            objectMetadata = client.getObjectMetadata(bucketName, remoteFileName);
            getLog().info( "  ETag: " + objectMetadata.getETag());
            getLog().info( "  ContentType: " + objectMetadata.getContentType());
            getLog().info( "  CacheControl: " + objectMetadata.getCacheControl());
            getLog().info( "  ContentEncoding: " + objectMetadata.getContentEncoding());
            getLog().info( "  ContentLength: " + objectMetadata.getContentLength());
            getLog().info( "  LastModified: " + objectMetadata.getLastModified());
        } catch (AmazonServiceException e) {
            getLog().info("  no object metadata found");
        } catch (AmazonClientException e) {
            throw new MojoExecutionException("  could not retrieve object metadata",e);
        }
        return objectMetadata;
    }

    private String calculateETag(File file) throws MojoExecutionException {
        String eTag = null;
        try {
            eTag = Hex.encodeHexString(DigestUtils.md5(new FileInputStream(file)));
        } catch (Exception e) {
            throw new MojoExecutionException("could not calculate ETag for " + file.getName(), e);
        } 
        getLog().info("eTag for " + file.getName() + " is " + eTag);
        return eTag;
    }
    
    private boolean contains(List<String> digestOptions, String search) {
        if (search == null) {
            throw new IllegalArgumentException("search cannot be null");
        }
        for (String item: digestOptions) {
            if (search.equalsIgnoreCase(item)) {
                return true;
            }
        }
        return false;
    }
    
    private String convertToString(List<String> list) {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (Iterator<?> iterator = list.iterator(); iterator.hasNext(); i++) {
            if (i > 0)
                builder.append(", ");
            builder.append(iterator.next());
        }
        return builder.toString();
    }
    
    private boolean hasExtension(File file) {
        return file.getName().lastIndexOf('.') > 0;
    }
}
