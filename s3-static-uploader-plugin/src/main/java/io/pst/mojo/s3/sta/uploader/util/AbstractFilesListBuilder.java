package io.pst.mojo.s3.sta.uploader.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.util.FileUtils;

import io.pst.mojo.s3.sta.uploader.config.Bind;
import io.pst.mojo.s3.sta.uploader.config.ManagedFile;
import io.pst.mojo.s3.sta.uploader.config.Metadata;

public abstract class AbstractFilesListBuilder {

    protected final File inputDirectory;
    protected final List<String> excludes;
    protected final Map<String, Metadata> metadataMap;
    protected final Map<String, ManagedFile> fileMap;    
    
    protected AbstractFilesListBuilder(File inputDirectory, List<String> excludes, List<Metadata> metadatas) {
        this.inputDirectory = inputDirectory;
        this.excludes = excludes;
        this.metadataMap = buildMetadataMap(metadatas);
        this.fileMap = new HashMap<String, ManagedFile>();
    }
    
    protected List<String> getMatchingFilesNames(String pattern) throws IOException {
        return FileUtils.getFileNames(inputDirectory, pattern, convertToString(excludes), true, false);
    }
    
    protected Metadata getMetadata(Bind bind) {
        Metadata metadata = metadataMap.get(bind.getMetadataId());
        if (metadata == null) {
            metadata = getDefaultMetadata();
            if (metadata == null) {
                throw new IllegalArgumentException("The metadata with Id " + bind.getMetadataId() + " is undefined.");
            }
        }
        return metadata;
    }

    private Map<String, Metadata> buildMetadataMap(List<Metadata> metadatas) {
        Map<String, Metadata> map = new HashMap<String, Metadata>(metadatas.size());
        for (Metadata metadata : metadatas) {
            map.put(metadata.getId(), metadata);
        }
        return map;
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
    
    private Metadata getDefaultMetadata() {
        Metadata retMetadata = null;
        for (Metadata metadata : metadataMap.values()) {
            if (metadata.isDefault()) {
                retMetadata=  metadata;
            }
        }
        return retMetadata;
    }
}
