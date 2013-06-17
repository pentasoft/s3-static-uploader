package io.pst.mojo.s3.sta.uploader.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.pst.mojo.s3.sta.uploader.config.Bind;
import io.pst.mojo.s3.sta.uploader.config.Include;
import io.pst.mojo.s3.sta.uploader.config.ManagedFile;
import io.pst.mojo.s3.sta.uploader.config.Metadata;

public class ConstrainedFilesListBuilder extends AbstractFilesListBuilder {

    public ConstrainedFilesListBuilder(File inputDirectory, List<String> excludes, List<Metadata> metadatas) {
        super(inputDirectory, excludes, metadatas);
    }
    
    public List<ManagedFile> build(Include include) throws IOException {
        List<ManagedFile> managedFiles;
        if (include.getContraints() == null) {
            managedFiles = Collections.emptyList();
        }
        else {
            for (Bind constraint : include.getContraints()) {
                List<String> constrainedFileNames = getMatchingFilesNames(constraint.getPattern());
                for (String fileName : constrainedFileNames) {
                    ManagedFile managedFile = new ManagedFile(fileName, getMetadata(constraint));
                    fileMap.put(fileName, managedFile);
                }
            }
            managedFiles = new ArrayList<ManagedFile>(fileMap.values());
        }
        return managedFiles;
    }
}
