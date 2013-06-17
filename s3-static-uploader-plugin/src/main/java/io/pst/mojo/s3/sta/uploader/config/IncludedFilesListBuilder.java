package io.pst.mojo.s3.sta.uploader.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class IncludedFilesListBuilder extends AbstractFilesListBuilder {

    private final List<Include> includes;
    private final ConstrainedFilesListBuilder constrainedFilesListBuilder;
    
    public IncludedFilesListBuilder(File inputDirectory, List<Include> includes, List<String> excludes, List<Metadata> metadatas) {
        super(inputDirectory, excludes, metadatas);
        this.includes = includes;
        this.constrainedFilesListBuilder = new ConstrainedFilesListBuilder(inputDirectory, excludes, metadatas);
    }
    
    public List<ManagedFile> build() throws IOException {
        for (Include include : includes) {
            collectIncludedFiles(include);
            replaceConstrainedFiles(include); 
        }
        return new ArrayList<ManagedFile>(fileMap.values());
    }
    
    private void collectIncludedFiles(Include include) throws IOException {
        List<String> includedFileNames = getMatchingFilesNames(include.getBind().getPattern());
        for (String fileName : includedFileNames) {
            ManagedFile managedFile = new ManagedFile(fileName, getMetadata(include.getBind()));
            fileMap.put(fileName, managedFile);
        }            
    }
    
    private void replaceConstrainedFiles(Include include) throws IOException {
        List<ManagedFile> constrainedFiles = constrainedFilesListBuilder.build(include);
        for (ManagedFile managedFile : constrainedFiles) {
            fileMap.put(managedFile.getFilename(), managedFile);
        }
    }
}
