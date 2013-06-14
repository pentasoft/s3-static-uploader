package io.pst.mojo.s3.sta.uploader;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import io.pst.mojo.s3.sta.uploader.config.Include;
import io.pst.mojo.s3.sta.uploader.config.ManagedFile;
import io.pst.mojo.s3.sta.uploader.config.Metadata;
import io.pst.mojo.s3.sta.uploader.config.ParametersValidator;
import io.pst.mojo.s3.sta.uploader.util.IncludedFilesListBuilder;
import io.pst.mojo.s3.sta.uploader.util.S3Uploader;

/**
 * @prefix s3-static-uploader
 * @requiresProject true
 * @requiresOnline true
 * @goal upload
 * @phase prepare-package
 * @description Uploads static site content to AWS S3
 */
public class S3StaticUploaderMojo extends AbstractMojo {

    public static final String S3_URL = "s3.amazonaws.com";
    
    /**
     * @parameter property="accessKey"
     * @required
     */
    private String accessKey;
    
    /**
     * @parameter property="secretKey"
     * @required
     */
    private String secretKey;
    
    /**
     * @parameter property="bucketName"
     * @required 
     */
    private String bucketName;

    /**
     * List of {@link Include} objects matching files to include.  
     *
     * @parameter
     */
    private List<Include> includes;
    
    /**
     * List of expressions matching files to exclude. Could be
     * regular expressions using the expression %regex[]. 
     *
     * @parameter
     */
    private List<String> excludes;
    
    /**
     * List of {@link Metadata) objects defining metadata for files
     * to include. 
     *
     * @parameter
     */
    private List<Metadata> metadatas;
    
    /**
     * The directory where the webapp is built.
     *
     * @parameter default-value="${project.build.directory}/${project.build.finalName}"
     * @required
     */
    private File outputDirectory;
    
    /**
     * Single directory for extra files to include in the WAR
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
     * Determines if the plugin will update Last-Modified and Expires headers
     * for those remote objects which remain unchanged but contain metadata
     * with expired timestamps. This is useful if you want to enlarge cache
     * lifetime for unchanged objects when you deploy new versions of the site.
     * 
     * @parameter default-value= false
     */
    private boolean refreshExpiredObjects;
    
    
    private S3Uploader uploader;
    
    public void execute() throws MojoExecutionException {
        logParameters();
        validateParameters();
        uploader = new S3Uploader(accessKey, secretKey, bucketName, inputDirectory, tmpDirectory, refreshExpiredObjects);
        uploader.setLog(getLog());
        List<ManagedFile> managedFiles = getManagedFiles();
        processManagedFiles(managedFiles); 
    }
    
    private void logParameters() {
        getLog().info("tmpDirectory " + tmpDirectory.getPath());
        getLog().info("inputDirectory " + inputDirectory.getPath());
        getLog().info("outputDirectory " + outputDirectory.getPath());
        getLog().info("includes " + includes);
        getLog().info("excludes " + excludes);
        getLog().info("metadatas " + metadatas);
    }
    
    private void validateParameters() throws MojoExecutionException {
        try {
        ParametersValidator validator = new ParametersValidator(includes, metadatas);
            validator.validate();
        } catch (Exception e) {
            throw new MojoExecutionException("error found validating configuration", e);            
        }
    }
    
    private List<ManagedFile> getManagedFiles() throws MojoExecutionException {
        try {
            getLog().info( "determining files that should be uploaded" );
            getLog().info("");
            IncludedFilesListBuilder includedFilesListBuilder = new IncludedFilesListBuilder(inputDirectory, includes, excludes, metadatas);
            return includedFilesListBuilder.build();
            
        } catch (IOException e) {
            throw new MojoExecutionException("cannot determine the files to be processed", e);
        }
    }
    
    private void processManagedFiles(List<ManagedFile> managedFiles) throws MojoExecutionException {
        for (ManagedFile managedFile: managedFiles) {
            processManagedFile(managedFile);
        }
    }

    private void processManagedFile(ManagedFile managedFile) throws MojoExecutionException {
        getLog().info("start processing file " + managedFile.getFilename() + " with metadata " + managedFile.getMetadata().toString());     
        try {
            uploader.uploadManagedFile(managedFile);
        }
        catch (Exception e) {
            throw new MojoExecutionException("cannot process file " + managedFile.getFilename(), e);
        }
        getLog().info("finnish processing file " + managedFile.getFilename());
        getLog().info("");
    }
}
